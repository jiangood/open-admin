---
name: oa-upgrade-docs
description: 在业务项目中同步 open-admin 框架的 skills + docs —— 正式版从 GitHub Release 下载 framework-files.zip，预览版（SNAPSHOT）从对应 tag 的源码包提取，按规则覆盖写入到项目根目录。适用于以 Maven JAR + npm 包方式引入 open-admin 的业务项目。
---

# oa-upgrade-docs — 框架文件同步指南

## 适用范围

当业务项目已通过 Maven JAR（`io.github.jiangood:open-admin`）和 npm 包（`@jiangood/open-admin`）方式集成了 open-admin 框架，需要将框架的 skills（`.opencode/skills/`）与文档（`docs/open-admin/`）同步到项目根目录时使用。

触发时机：
- 升级框架版本后，将新版本的框架文件同步到业务项目
- 首次接入框架时，将框架文件生成到业务项目
- 框架文件缺失 / 被误删时，重新同步

## 前提条件检查

开始之前，必须确认：

1. **网络可用**：需能访问 `https://github.com`。
2. **工具可用**：`curl`，以及 `unzip`（正式版）或 `tar`（预览版）；Windows 下也可用 PowerShell，见下。
3. **业务项目根目录**：通过 `pom.xml` 定位（向上查找最近的 `pom.xml`）。

## 版本通道与文件来源

| 通道 | 版本示例 | tag | 下载地址 | 解包方式 |
|---|---|---|---|---|
| 正式版 | `3.4.0` | `v3.4.0` | `releases/download/v{VERSION}/framework-files.zip` | `unzip` |
| 预览版 | `3.4.0-SNAPSHOT.1` | `snapshot-v3.4.0-1` | `archive/refs/tags/snapshot-v{BASE}-{BUILD}.tar.gz` | `tar --strip-components=1` |

预览版**没有** `framework-files.zip` 资产，不要去找 release；改用 tag 源码包，内容比 ZIP 多（含整个仓库），提取后只需 `.opencode/skills/` 与 `docs/open-admin/`。

## 同步规则

| 目标 | 规则 |
|------|------|
| `<根>/.opencode/skills/` | 仅覆盖框架 skill（`oa-crud`、`oa-upgrade`、`oa-upgrade-docs`），不删除业务本地 skill |
| `<根>/docs/open-admin/` | 全量镜像（删除该目录下来源之外的孤儿文件） |

- 同步按**内容比对**：无变更不写入。
- 提取后的目录结构统一为 `<SRC>/docs/open-admin/**`、`<SRC>/.opencode/skills/**`。

## 执行流程

### 1. 确定目标版本与 tag

读取业务项目当前依赖的框架版本（`pom.xml` 中 `io.github.jiangood:open-admin` 的 `<version>`，或 `package.json` 中 `@jiangood/open-admin` 的版本）。若为升级场景，使用目标版本号；否则使用当前版本。

> 如需同步的版本与业务项目依赖版本不同（例如回退 / 预演新版本），可让开发者显式指定版本号或 tag。

按传入值判断通道并换算 tag：

| 传入 | 通道 | tag |
|---|---|---|
| `3.4.0` / `v3.4.0` | 正式版 | `v3.4.0` |
| `snapshot-v3.4.0-1` | 预览版 | 原样使用 |
| `3.4.0-SNAPSHOT.1` | 预览版 | `snapshot-v3.4.0-1` |
| `3.4.0-SNAPSHOT`（无 build 号） | 预览版 | 询问开发者要哪个构建号；或用 `gh api repos/jiangood/open-admin/tags --paginate -q '.[].name' \| grep '^snapshot-v3.4.0-' \| head -1` 取最新 |

换算规则：`x.y.z-SNAPSHOT.<build>` → `snapshot-v{x.y.z}-{build}`。

### 2. 下载并解包

**正式版**

```bash
# 替换 {VERSION} 为步骤 1 确定的版本号
curl -fL -o /tmp/oa-framework-files.zip \
  "https://github.com/jiangood/open-admin/releases/download/v{VERSION}/framework-files.zip"

rm -rf /tmp/oa-framework-files && mkdir -p /tmp/oa-framework-files
unzip -q /tmp/oa-framework-files.zip -d /tmp/oa-framework-files
```

**预览版**

```bash
# 替换 {TAG} 为步骤 1 换算出的 tag，如 snapshot-v3.3.4-1
rm -rf /tmp/oa-framework-files && mkdir -p /tmp/oa-framework-files
curl -fL --retry 3 -o /tmp/oa-framework-files.tar.gz \
  "https://github.com/jiangood/open-admin/archive/refs/tags/{TAG}.tar.gz"
tar -xzf /tmp/oa-framework-files.tar.gz -C /tmp/oa-framework-files --strip-components=1
```

- 源码包顶层目录为 `open-admin-{TAG}/`，`--strip-components=1` 去掉它，得到与 ZIP 一致的布局
- 解包后必须校验（防止 tag 不存在或结构变化导致把空目录同步过去）：

```bash
test -f /tmp/oa-framework-files/docs/open-admin/guide.md \
  && test -f /tmp/oa-framework-files/.opencode/skills/oa-crud/SKILL.md \
  || { echo "来源目录结构异常，停止同步"; exit 1; }
```

- 下载失败（404）：确认 tag 存在（`gh api repos/jiangood/open-admin/git/ref/tags/{TAG}`），或改用已发布的最近版本 / 正式版

### 3. 同步到项目根目录

以下命令在**业务项目根目录**执行（先确认 `pwd` 包含 `pom.xml`）。

```bash
PROJ_ROOT=$(pwd)
SRC=/tmp/oa-framework-files

# 3a. .opencode/skills/ —— 仅覆盖框架 skill，不删除本地 skill
mkdir -p "$PROJ_ROOT/.opencode/skills"
for skill in oa-crud oa-upgrade oa-upgrade-docs; do
  if [ -d "$SRC/.opencode/skills/$skill" ]; then
    mkdir -p "$PROJ_ROOT/.opencode/skills/$skill"
    cp -r "$SRC/.opencode/skills/$skill/." "$PROJ_ROOT/.opencode/skills/$skill/"
  fi
done

# 3b. docs/open-admin/ —— 全量镜像（删除孤儿文件）
mkdir -p "$PROJ_ROOT/docs/open-admin"
cp -r "$SRC/docs/open-admin/." "$PROJ_ROOT/docs/open-admin/"
# 删除来源之外的孤儿文档（在 docs/open-admin 目录下执行）
cd "$PROJ_ROOT/docs/open-admin" || exit 1
find . -type f | while read -r f; do
  [ -f "$SRC/docs/open-admin/$f" ] || { echo "删除孤儿文档: $f"; rm "$f"; }
done
cd "$PROJ_ROOT" || exit 1
```

Windows 环境可改用 PowerShell（下载用 `Invoke-WebRequest`，预览版用 `tar -xzf`）：

```powershell
$src = "C:\Temp\oa-framework-files"
# 3a. 覆盖框架 skill
Copy-Item "$src\.opencode\skills\*" "$PWD\.opencode\skills\" -Recurse -Force
# 3b. 镜像 docs（含删除孤儿，酌情处理）
Copy-Item "$src\docs\open-admin\*" "$PWD\docs\open-admin\" -Recurse -Force
```

### 4. 清理临时文件

```bash
rm -rf /tmp/oa-framework-files /tmp/oa-framework-files.zip /tmp/oa-framework-files.tar.gz
```

### 5. 验证

```bash
# 确认关键文件存在且为期望版本内容
ls -la .opencode/skills/oa-upgrade-docs/SKILL.md docs/open-admin/guide.md
# 抽查文档内容包含框架版本关键字（可选）
```

## 验证清单

- [ ] 已识别通道并换算出正确的 tag / 版本
- [ ] 解包后已校验 `docs/open-admin/guide.md` 与 `.opencode/skills/oa-crud/SKILL.md` 存在
- [ ] `.opencode/skills/oa-crud|oa-upgrade|oa-upgrade-docs/SKILL.md` 已更新
- [ ] `docs/open-admin/*.md` 已镜像，孤儿文件已删除
- [ ] 无变更文件未被无谓写入

## 故障排查

| 问题 | 处理 |
|------|------|
| ZIP 下载 404 | 正式版版本号错误或 release 未附带 ZIP；确认后重试 |
| tar.gz 下载 404 | 快照 tag 不存在或已被清理；用 `gh api repos/jiangood/open-admin/tags` 查可用 tag |
| 解包后目录为空 / 结构不对 | 检查是否漏了 `--strip-components=1`；重新解包并跑第 2 步校验 |
| `unzip` 不可用 | `apt-get install unzip`，或改用预览版的 `tar` 流程 |
| 业务项目无 pom.xml | 确认在正确的项目根目录执行 |
| 同步后文件无变化 | 符合预期（内容比对，无变更不写入） |

## 参考

- 框架仓库: https://github.com/jiangood/open-admin
- 正式版资产命名约定：`framework-files.zip`（`v{版本}` release 自动附带）
- 预览版无 release 资产：使用 `archive/refs/tags/snapshot-v{版本}-{build}.tar.gz`
