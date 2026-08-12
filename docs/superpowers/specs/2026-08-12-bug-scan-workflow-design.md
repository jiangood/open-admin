# Bug 扫描 GitHub Actions Workflow 设计

日期：2026-08-12

## 背景

open-admin 框架仓库需要通过 opencode 定期自动扫描代码，发现逻辑与功能层面的 bug（不含优化/性能/风格）。扫描结果以 GitHub Issues 形式产出，人工决定是否修复。

## 目标

- 定时 + 手动触发扫描
- 用 opencode 单次全扫后端 + 前端代码
- 重点两类问题：
  1. **逻辑 bug**：空指针、事务边界、查询条件错误、状态流转、分页/边界、权限、并发等
  2. **前后端交互 bug**：URL 路径与方法、请求参数名/类型 vs DTO、响应字段名 vs 前端取值、分页参数、权限码、字典值
- 明确排除：优化、性能、代码风格、重构建议
- 产出：为每条 bug 创建 GitHub Issue（打 `bug-scan` 标签），去重（避免每日扫描重复创建）
- 同时上传分析报告 artifact 供查看

## 实现方案

### 文件

`.github/workflows/bug-scan.yml`

### 触发

```yaml
on:
  schedule:
    - cron: '0 3 * * *'
  workflow_dispatch:
    inputs:
      model:
        description: 'opencode 模型（默认免费模型，无需 API Key）'
        required: false
        type: string
        default: 'opencode/deepseek-v4-flash-free'
```

- `concurrency`：防重叠
- `permissions`：`contents: read`，`issues: write`

### Job 步骤

1. `actions/checkout@v5`（默认分支，`fetch-depth: 1`）
2. 安装 opencode：`npm i -g opencode-ai`
3. 获取已有 open Issue 列表（供 AI 去重参考）
   - `gh issue list --state open --label bug-scan --json number,title,body` 输出到 `existing-issues.json`
4. `opencode run -m $MODEL --auto` 单次全扫
   - 扫描范围：后端 `src/main/java` + 前端 `web/src`
   - 要求 opencode 将发现写入 `findings.json`（工作目录下的文件）
   - 提示词约束：
     - 只找逻辑 bug 与前后端交互 bug，不找优化/性能/风格/重构
     - 每个 bug 输出一条结构化记录，字段：`title`、`severity`(high/medium/low)、`category`、`file`、`line`、`description`、`suggestion`
     - 只报告高置信度的确凿 bug，宁缺毋滥
     - **AI 判断去重**：先读取 `existing-issues.json`，对每个候选 bug 由 AI 判断（按问题本质，而非仅标题文本）是否与已有 open Issue 重复；重复则跳过，不写入 findings.json
   - 环境变量 `OPENCODE_MODEL` 控制模型（同 upgrade-repos 模式）
5. 解析 `findings.json` 并创建 Issue
   - 遍历每条记录直接 `gh issue create`，body 含位置（file:line）、原因、修复建议
   - 使用 `GITHUB_TOKEN`（workflow 自动注入，有 issues:write 权限）
   - 创建前可再用 `gh issue list --search` 做一道轻量兜底查重（AI 已判断为主，此步防并发）
6. `actions/upload-artifact@v4` 上传 findings 报告（JSON + 生成的 Markdown 摘要）

### 容错

- findings.json 不存在或为空：打印提示，正常结束（exit 0）
- 单条 Issue 创建失败：`|| true` 跳过，不中断整体
- 设置 `timeout-minutes: 60`，超时防挂起
## 关键决策

- **单次全扫而非矩阵拆分**：用户明确要求一次扫完，聚焦全局视角，避免单模块上下文丢失交互关联
- **opencode 写 findings.json，工作流创建 Issue**：结构化输出便于审计，比会话内直接建 Issue 更可控
- **AI 判断去重**：先获取已有 open Issue 列表，由 AI 按问题本质判断是否重复（非仅标题文本匹配），重复的不写入 findings.json

## 验证方式

- 本地无法直接跑 GitHub Actions，通过 `actionlint` 或 YAML 语法校验
- 手动触发 workflow_dispatch 验证端到端流程
