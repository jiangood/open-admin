# open-admin

![Maven Central](https://img.shields.io/maven-central/v/io.github.jiangood/open-admin)
![npm](https://img.shields.io/npm/v/@jiangood/open-admin)

open-admin 是一个后台管理系统框架（脚手架），**业务项目无需从零搭建用户管理、角色权限、数据字典等功能。

## 快速集成

```xml
<dependency>
    <groupId>io.github.jiangood</groupId>
    <artifactId>open-admin</artifactId>
    <version>${open-admin.version}</version>
</dependency>
```

```json
{
  "dependencies": { "@jiangood/open-admin": "版本" }
}
```

添加依赖后，用户管理、角色权限、数据字典、系统参数、Quartz 调度、文件管理、邮件发送、在线用户等功能开箱即用。框架的 skills 与文档按 `docs/framework-sync.json` 清单从 GitHub tag 源码归档同步到项目根目录（详见 [Skills (opencode)](#skills-opencode)）。

## 快速开始

### 环境要求

- **JDK 21+** / **Node.js 18+**（数据库内置 H2，无需安装）

### 后端启动

```bash
git clone https://github.com/jiangood/open-admin.git
cd open-admin
mvn clean compile
mvn -Pdev spring-boot:run   # 开发模式启动（内置 H2，首次启动自动建表 + 初始化数据）
```

数据库文件默认在 `/data/db/open-admin`（本地开发可用 `-Ddb_path=./data/db/open-admin` 覆盖）。

### 前端启动

```bash
cd web
npm install
npm run dev                    # 默认 http://localhost:7777
```

### 默认登录

首次启动会自动创建管理员账号 `admin`，初始密码**随机生成**（以 `GEN-` 开头）并打印在启动日志的英文横幅中（标记 `[INITIAL-ADMIN-PASSWORD]`，Docker 部署可用 `docker logs` 查看），登录后须修改密码。密码被修改前，每次启动都会打印 `[ADMIN-PASSWORD-CHECK]` 提醒横幅（只提醒，不再重复输出密码）。

### Docker 一键部署

镜像内置 H2 数据库，无需外部数据库，数据、日志、上传文件、登录会话持久化在 `/data`（正常重启后用户无需重新登录）：

```bash
# docker compose（首次自动构建镜像）
docker compose -f docker-compose/docker-compose.yml up -d --build

# 或手动构建 / 运行
docker build -t open-admin .
docker run -d --name open-admin -p 8888:8888 -v ./data:/data open-admin
```

容器默认以 `prod` profile 启动；可通过环境变量 `SPRING_PROFILES_ACTIVE` 覆盖。

### 集成到已有项目

**后端**：业务项目 `pom.xml` 添加依赖，`application.yml` 配置数据源，通过 `spring.config.import` 引入框架默认配置。注意 import 的内容会覆盖**同一个文档**里的配置，因此业务配置要写在 `---` 之后：

```yaml
# 第一个文档：只放 import
spring:
  config:
    import: classpath:application-lib.yml

# 第二个文档：业务项目自己的配置，优先级高于 application-lib.yml
---
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/demo
sys:
  title: 客户管理系统
```

详见[配置参考 · 覆盖框架默认值](docs/open-admin/config.md#覆盖框架默认值)。

**前端**：按以下清单配置（完整可运行示例见 [open-admin-example](https://github.com/jiangood/open-admin-example)）：

1. `package.json` 添加 `@jiangood/open-admin` 及 peer 依赖（react / react-dom / antd / @ant-design/icons / axios / dayjs / lodash / qs）
2. `vite.config.js`：
   - 注册插件 `openAdmin()`（来自 `@jiangood/open-admin/vite-plugin`，负责扫描 `src/pages` 生成路由）

3. `.env` 配置 `VITE_SERVER_SERVLET_CONTEXT_PATH`（必须与后端 `server.servlet.context-path` 一致）
4. 入口 `main.jsx` 引入虚拟路由表并渲染布局：

```jsx
import routes from 'virtual:open-admin/routes';
import {registerRoutes, PageLoading, Layouts} from '@jiangood/open-admin';

registerRoutes(routes);
createRoot(document.getElementById('root')).render(
    <React.Suspense fallback={<PageLoading/>}><Layouts/></React.Suspense>
);
```

**页面约定**（vite-plugin 扫描规则）：

- 页面文件放在 `src/pages/` 下，扩展名 `.jsx` 或 `.tsx`，文件名首字母小写（大写开头视为普通组件不注册路由）
- `src/pages/product/index.jsx` → 路由 `/product`；`$code.jsx` → 动态段 `/:code`
- 业务页面与框架页面路由冲突时业务页面优先（可覆盖框架页面）
- 页面组件可实现 `onShow()` 方法，在首次加载或 Tab 切换激活时自动调用（详见[页面生命周期](docs/open-admin/api.md#页面生命周期)）
- 页面文件可选导出 `pageMeta`（`title` / `requiresAuth` / `layout`），用于覆盖页面标题与页面类型（详见 [pageMeta](docs/open-admin/api.md#页面元信息-pagemeta)）

**目录约定**（无需配置，自动识别）：`src/pages/` 下的页面默认需登录且使用后台布局。框架内置登录页为 `/login`（`pages/login.jsx`，公开页面例外）。免登录页面需显式声明 `pageMeta: { requiresAuth: false, layout: 'blank' }`，需登录但不使用后台布局的页面需声明 `pageMeta: { requiresAuth: true, layout: 'blank' }`，详见 [development.md](docs/open-admin/development.md#页面目录约定)。


## 文档

| 文档 | 内容 |
|------|------|
| [docs/open-admin/guide.md](docs/open-admin/guide.md) | 架构设计 / 核心功能 / 添加业务模块 / 内置模块 / FAQ |
| [docs/open-admin/api.md](docs/open-admin/api.md) | 后端（Spec/注解/工具类/定时任务）+ 前端（组件/生命周期/字段组件/文件上传/工具类）API 参考 |
| [docs/open-admin/config.md](docs/open-admin/config.md) | 全部 `sys.*` 配置 / 文件存储 / 临时文件清理 / context-path / 主题定制 |
| [docs/open-admin/development.md](docs/open-admin/development.md) | 后端命名 / REST API 规范 / 前后端开发要点 |

## 开发（框架本仓库）

### 双项目工作流

```
D:/ws/
├── open-admin/              # 框架项目（本仓库）
│   ├── src/main/java/
│   ├── web/src/framework/   # 前端框架源码 (npm publish)
│   └── pom.xml
└── open-admin-example/      # 示例业务项目（依赖框架）
```

修改框架后需先执行 `mvn clean install -DskipTests`。

### 开发命令

```bash
mvn clean compile                                          # 编译
mvn test -Dtest=BeanToolTest                               # 运行单个测试
mvn test -Dtest='!*RepositoryTest,!*ServiceTest'           # 仅纯单元测试，跳过 SpringBootTest 集成测试（更快）
mvn clean package                                          # 打包
mvn -Pdev spring-boot:run                                  # 独立应用启动
mvn clean install -DskipTests                              # 安装到本地仓库
node scripts/bump-version.mjs <新版本号>                    # 仅升级 pom.xml + web/package.json 版本号
node scripts/release.mjs <新版本号>                        # 一键发版：bump + 全量测试 + commit + tag + push（见「发版」）
node scripts/release.mjs <版本号> --snapshot               # 发布非正式版（Maven SNAPSHOT + npm snapshot）
cd web && npm install                                         # 前端安装依赖
cd web && npm run dev                                         # 前端开发模式
cd web && npm run build                                       # 前端构建
```

测试使用 H2 内存数据库。RepositoryTest 和 ServiceTest 等集成测试同样使用 H2，可通过 `mvn test -Dtest='!*RepositoryTest,!*ServiceTest'` 跳过以加速。

### 启动脚本

日常开发用 `scripts/start.py` 管理前后端（后台运行，日志落 `logs/`，PID 在 `logs/*.pid`，`logs/` 已被 gitignore）。需 Python 3：

```bash
python scripts/start.py                                 # 后台启动前后端（target=all, action=start）
python scripts/start.py backend start|stop|restart|status     # 后端: mvn -Pdev spring-boot:run（devtools 热重载）
python scripts/start.py frontend start|stop|restart|status    # 前端: npm run dev（端口 7777，缺 node_modules 自动安装）
```

- `target` 可为 `all|backend|frontend`（缺省 `all`），`action` 可为 `start|stop|restart|status`（缺省 `start`）
- 日志 `logs/backend.log`、`logs/frontend.log`，PID 记录在 `logs/*.pid`
- Windows 下以 `cmd.exe /c` + `CREATE_NO_WINDOW` 后台启动、`taskkill /T /F` 结束整棵进程树；POSIX 下用独立会话 + 进程组 `SIGTERM/SIGKILL`
- 后端脚本即 `mvn -Pdev spring-boot:run`（用 `application.yml`，默认内置 H2；需要覆盖连接参数时用 `SPRING_DATASOURCE_URL` / `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD`）

### 发版

发版流程固化在 `scripts/release.mjs`（跨平台，需 Node.js 18+，输出纯 ASCII 避免乱码），日志落 `logs/{release|snapshot}-v<版本>-<时间戳>.log`（UTF-8，已被 gitignore）。正式版只给版本号；快照版加 `--snapshot` 或直接写 `-SNAPSHOT`：

```bash
node scripts/release.mjs 3.1.3                    # bump + 全量测试 + commit + tag + push，触发 CI 发布
node scripts/release.mjs v3.1.3                   # 版本号带不带 v 都行（推的 tag 一律带 v）
node scripts/release.mjs 3.1.3 --dry-run          # 只跑检查、打印计划，不改动仓库文件（仅写 logs/ 日志）
node scripts/release.mjs 3.1.3 --no-push          # 本地演练：commit + tag 但不推送
node scripts/release.mjs 3.1.3 --skip-tests       # 跳过 mvn test 与 npm build（发版不建议）
node scripts/release.mjs status                   # 当前分支 / 版本 / tag / 工作区状态
node scripts/release.mjs check                    # 只读检查，输出 KEY=VALUE（当前版本 + 候选版本号）
node scripts/release.mjs --help                   # 全部参数与退出码
```

- 跨平台（Windows / Linux / macOS），需 Node.js 18+；不再依赖 Python 或 PowerShell
- 版本号可带或不带 `v` 前缀（`3.1.3` / `v3.1.3` 等价）；正式版 tag 为 `v<version>`，快照版 tag 为 `snapshot-v<version>-<build>`，`publish.yml` 同时监听 `v*` 与 `snapshot-v*`
- 前置条件：`gh` 已登录、当前分支为 `main`、工作区干净
- 白名单：一次发版只允许修改 `*/pom.xml` 与 `web/package.json`，出现别的改动直接中止（退出码 3）
- 退出码：`0` 成功 / `1` 参数或前置条件 / `2` 测试失败 / `3` 工作区不干净 / `4` git 失败 / `5` 版本或 tag 冲突
- 失败自动回滚版本号改动；若 commit + tag 已建、只是推送失败，重跑同一条命令会进入 resume 模式只补推送
- push tag 后由 `.github/workflows/publish.yml` 自动发布 Maven Central + npm 并创建 GitHub Release（Release Notes 由 CI 生成）
- 排障：失败时会打印恢复命令与日志路径
- 手工等价命令见 README「开发命令」

### 非正式版（SNAPSHOT）

用于联调/预发布验证，不创建正式 GitHub Release，也不会更新 npm 的 `latest` 标签。版本与 tag 规则：

| 通道 | 版本示例 | git tag | 发布目标 |
|---|---|---|---|
| Maven | `3.4.0-SNAPSHOT` | `snapshot-v3.4.0-<build>` | Central Portal snapshot 仓库 |
| npm | `3.4.0-SNAPSHOT.<build>` | 同上 | npm dist-tag `snapshot` |

`<build>` 由脚本自动生成（时间戳 `YYYYMMDDHHMMSS`），不再支持手动指定。

```bash
node scripts/release.mjs 3.4.0 --snapshot          # build 号自动生成（时间戳）
node scripts/release.mjs 3.4.0-SNAPSHOT            # 同上
node scripts/release.mjs 3.4.0 --snapshot --dry-run
node scripts/release.mjs 3.4.0 --snapshot --no-push
```

- 快照版 tag 为 `snapshot-v<版本>-<build>`，由 `.github/workflows/publish.yml` 的 snapshot 流程触发，不会与正式版 `v*` tag 混淆
- Maven 走 `3.4.0-SNAPSHOT`，npm 使用带唯一 build 号的 `3.4.0-SNAPSHOT.<build>`（脚本自动生成），并通过 dist-tag `snapshot` 提供最新快照
- **前置条件**：Central Portal 的 namespace 需先开启 `SNAPSHOTs`（Namespaces 页面 → Enable SNAPSHOTs）；否则 Maven 快照发布会失败
- 消费方需显式添加快照仓库；npm 用 `@jiangood/open-admin@snapshot` 安装：
  ```xml
  <repository>
    <id>central-portal-snapshots</id>
    <url>https://central.sonatype.com/repository/maven-snapshots/</url>
    <releases><enabled>false</enabled></releases>
    <snapshots><enabled>true</enabled></snapshots>
  </repository>
  ```
- SNAPSHOT 可覆盖且约 90 天后会被 Central 清理；Maven Central 的 SNAPSHOT 通道与正式 release 分开，但具体用量/额度仍以 [Usage Center](https://central.sonatype.com/publishing/usage) 为准
- `pom.xml` 的 `publish` profile 已为 `central-publishing-maven-plugin` 显式配置 `centralSnapshotsUrl`，`-SNAPSHOT` 会直接上传到 Central Portal 快照仓库（插件版本需 `>= 0.7.0`，当前为 `0.12.0`）
- 只改版本号不发布：可用 `node scripts/bump-version.mjs 3.4.0-SNAPSHOT --npm-version 3.4.0-SNAPSHOT.<build>`（build 自行按规则生成）

## Skills (opencode)

### 内置 skills

| Skill | 用途 |
|-------|------|
| `oa-crud` | 创建 CRUD 业务模块 |
| `oa-upgrade` | 升级框架版本 |
| `oa-upgrade-docs` | 从 GitHub Release 同步框架文件（skills + docs） |

### 同步框架文件到业务项目

框架文件（skills + 文档）的同步范围以 **`docs/framework-sync.json`** 清单为准。`oa-upgrade-docs` skill 会按目标版本 tag 下载源码归档 `https://github.com/jiangood/open-admin/archive/refs/tags/{TAG}.tar.gz`，读取其中的清单并同步到业务项目根目录：

- `mirror` 条目：使目标路径与来源一致（该路径内删除来源不存在的文件/子目录）
- `copy` 条目：覆盖/合并到目标，不删除目标中的额外文件
- 仅作用于清单列出的路径，不会删除清单外文件（如业务自建 `.opencode/skills/*`）

正式版 tag 为 `vX.Y.Z`，快照版 tag 为 `snapshot-vX.Y.Z-{build}`，两者均通过 tag 归档同步（不再使用 `framework-files.zip`）。日常使用直接调用 `oa-upgrade-docs` skill（升级框架后 `oa-upgrade` 会在末尾触发）。

### 历史项目升级（本地 skill 过旧/缺失时）

历史项目若仍依赖旧版同步逻辑（例如下载已移除的 `framework-files.zip`），或尚未接入框架 skill，可在**业务项目根目录**手工执行一次“按清单同步”，将 `docs/framework-sync.json` 与最新框架 skill/文档同步进来，之后即可正常使用新 skill。

1. 确定目标 tag：正式版 `vX.Y.Z`（如 `v3.4.0`），快照版 `snapshot-vX.Y.Z-{build}`（如 `snapshot-v3.4.0-1`；无 build 号时可用 `git ls-remote --tags --sort=-v:refname https://github.com/jiangood/open-admin.git 'refs/tags/snapshot-vX.Y.Z-*' | head -1` 取最新）。
2. 下载并解包 tag 归档：

```bash
TAG="v3.4.0"   # 替换为目标 tag
git ls-remote --exit-code --tags https://github.com/jiangood/open-admin.git "refs/tags/$TAG" >/dev/null || exit 1
rm -rf /tmp/oa-framework-files /tmp/oa-framework-files.tar.gz
mkdir -p /tmp/oa-framework-files
curl -fL --retry 3 -o /tmp/oa-framework-files.tar.gz \
  "https://github.com/jiangood/open-admin/archive/refs/tags/$TAG.tar.gz"
tar -xzf /tmp/oa-framework-files.tar.gz -C /tmp/oa-framework-files --strip-components=1
test -f /tmp/oa-framework-files/docs/framework-sync.json || { echo "缺少 docs/framework-sync.json（目标版本过旧或来源异常）"; exit 1; }
```

3. 按清单同步（需 `python3`）：

```bash
python3 - <<'PY'
import json, os, shutil
from pathlib import Path

PROJ = Path.cwd()
SRC = Path("/tmp/oa-framework-files")
with open(SRC / "docs/framework-sync.json", "r", encoding="utf-8") as f:
    manifest = json.load(f)

for item in manifest.get("sync") or []:
    rel = item["path"].strip().strip("/")
    mode = item.get("mode", "copy")
    src, dst = SRC / rel, PROJ / rel
    if not src.exists():
        print(f"[跳过] 来源不存在: {rel}")
        continue
    if src.is_dir():
        dst.mkdir(parents=True, exist_ok=True)
        for root, dirs, files in os.walk(src):
            r = Path(root); rel_dir = r.relative_to(src); out_dir = dst / rel_dir
            out_dir.mkdir(parents=True, exist_ok=True)
            for d in dirs: (out_dir / d).mkdir(parents=True, exist_ok=True)
            for name in files: shutil.copy2(r / name, out_dir / name)
        if mode == "mirror":
            for root, dirs, files in os.walk(dst, topdown=False):
                r = Path(root); rel_dir = r.relative_to(dst); src_dir = src / rel_dir
                for name in files:
                    if not (src_dir / name).exists(): (r / name).unlink()
                for d in dirs:
                    if not (src_dir / d).exists(): shutil.rmtree(r / d)
        print(f"[同步:{mode}] {rel}")
    else:
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src, dst)
        print(f"[同步:{mode}] {rel}")
print("同步完成")
PY
```

4. 清理并验证：

```bash
rm -rf /tmp/oa-framework-files /tmp/oa-framework-files.tar.gz
test -f docs/framework-sync.json && test -f .opencode/skills/oa-upgrade-docs/SKILL.md && echo "ok"
```

> 若目标版本早于清单机制（解包后无 `docs/framework-sync.json`，且根目录也无 `framework-sync.json`），请先同步到一个包含清单的版本；也可按该版本仓库中的同步说明手工复制 `.opencode/skills/oa-*` 与 `docs/open-admin/`。详细规则与故障排查见 `.opencode/skills/oa-upgrade-docs/SKILL.md`。
