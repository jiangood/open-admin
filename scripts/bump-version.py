#!/usr/bin/env python3
"""版本升级脚本 — 自动发现 pom.xml，一键升级全部版本

用法: python scripts/bump-version.py <新版本号>
示例: python scripts/bump-version.py 2.5.3

特性:
  - 自动递归扫描所有 pom.xml（排除 target、node_modules 等目录）
  - 从根 POM 的 <modules> 字段确定子模块列表，精准匹配替换策略
  - 根 POM 替换项目自身的 <version>，保留 <parent> 块不变
  - 子模块 POM 只替换 <parent> 块内的 <version>，不改其他任何版本
  - 同时升级 web/package.json（如存在）
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
VERSION_RE = re.compile(r"^\d+\.\d+\.\d+$")

# package.json 路径（如无前端可删此行）
PACKAGE_JSON_PATH = "web/package.json"

# 扫描时排除的目录
EXCLUDE_DIRS = {"node_modules", ".git", ".svn", "target", ".mvn", "dist", "build", ".gradle"}

ROOT_POM = "pom.xml"

for _stream in (sys.stdout, sys.stderr):
    try:
        _stream.reconfigure(errors="replace")
    except (AttributeError, ValueError):
        pass


def get_child_module_dirs(root_pom_content: str) -> set:
    """从根 POM 的 <modules> 字段解析子模块目录名。"""
    modules_match = re.search(r"<modules>([\s\S]*?)</modules>", root_pom_content)
    if not modules_match:
        return set()
    return {m.strip() for m in re.findall(r"<module>(.*?)</module>", modules_match.group(1))}


def replace_root_pom_version(content: str, version: str) -> str:
    """替换根 POM 的项目版本号（跳过 <parent> 块，只替换第一个 <version>）。"""
    parent_match = re.search(r"<parent>[\s\S]*?</parent>", content)
    pattern = re.compile(r"(<version>)\d+\.\d+\.\d+(</version>)")
    replacement = rf"\g<1>{version}\g<2>"
    if not parent_match:
        return pattern.sub(replacement, content, count=1)
    placeholder = "<!--__PARENT_BLOCK__-->"
    content = content.replace(parent_match.group(0), placeholder)
    content = pattern.sub(replacement, content, count=1)
    return content.replace(placeholder, parent_match.group(0))


def replace_child_module_version(content: str, version: str) -> str:
    """只替换子模块 POM 的 <parent> 块内的 <version>。"""
    pattern = re.compile(r"(<parent>[\s\S]*?<version>)\d+\.\d+\.\d+(</version>[\s\S]*?</parent>)")
    return pattern.sub(rf"\g<1>{version}\g<2>", content, count=1)


def replace_package_json(content: str, version: str) -> str:
    pattern = re.compile(r'("version":\s*")\d+\.\d+\.\d+(")')
    return pattern.sub(rf"\g<1>{version}\g<2>", content, count=1)


def find_pom_files(directory: Path):
    """递归查找所有 pom.xml，排除 EXCLUDE_DIRS。"""
    results = []
    try:
        entries = list(directory.iterdir())
    except OSError:
        return results
    for entry in entries:
        if entry.name in EXCLUDE_DIRS:
            continue
        if entry.is_dir():
            results.extend(find_pom_files(entry))
        elif entry.name == ROOT_POM:
            results.append(entry)
    return results


def main(argv) -> int:
    new_version = argv[0] if argv else None
    if not new_version:
        print("❌ 请提供版本号，例如: python scripts/bump-version.py 2.5.3")
        return 1
    if not VERSION_RE.match(new_version):
        print("❌ 版本号格式错误，应为 x.y.z 格式（如 2.5.3）")
        return 1

    print(f"\n🚀 开始升级版本至 v{new_version}\n")

    # ---------- 读取根 POM，获取子模块列表 ----------
    root_pom_path = ROOT / ROOT_POM
    try:
        root_pom_content = root_pom_path.read_text(encoding="utf-8")
    except OSError:
        print(f"❌ 未找到根 POM: {ROOT_POM}")
        return 1
    child_module_dirs = get_child_module_dirs(root_pom_content)
    print(f"📦 发现 {len(child_module_dirs)} 个子模块: {', '.join(sorted(child_module_dirs)) or '无'}\n")

    # ---------- pom.xml（自动扫描） ----------
    pom_files = find_pom_files(ROOT)
    updated_count = 0

    if not pom_files:
        print("⚠️  未找到任何 pom.xml 文件")
    else:
        for abs_path in pom_files:
            content = abs_path.read_text(encoding="utf-8")
            rel_path = abs_path.relative_to(ROOT).as_posix()

            if rel_path == ROOT_POM:
                type_name = "根 POM"
                new_content = replace_root_pom_version(content, new_version)
            elif (abs_path.parent.relative_to(ROOT).as_posix()) in child_module_dirs:
                type_name = "子模块"
                new_content = replace_child_module_version(content, new_version)
            else:
                type_name = "其他"
                new_content = replace_root_pom_version(content, new_version)

            if content == new_content:
                print(f"⚠️  [{type_name}] 未能找到版本号，跳过: {rel_path}")
                continue

            abs_path.write_text(new_content, encoding="utf-8")
            print(f"✅ [{type_name}] {rel_path}")
            updated_count += 1

    # ---------- package.json（固定路径） ----------
    pkg_path = ROOT / PACKAGE_JSON_PATH
    if pkg_path.exists():
        content = pkg_path.read_text(encoding="utf-8")
        new_content = replace_package_json(content, new_version)
        if content != new_content:
            pkg_path.write_text(new_content, encoding="utf-8")
            print(f"✅ package.json: {PACKAGE_JSON_PATH}")
            updated_count += 1
        else:
            print(f"⚠️  未能找到版本号，跳过: {PACKAGE_JSON_PATH}")

    print(f"\n🎉 升级完成！共更新 {updated_count} 个文件至 v{new_version}\n")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
