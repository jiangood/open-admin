#!/bin/bash
set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
node "$SCRIPT_DIR/bump-version.js" bump "$SCRIPT_DIR/pom.xml" "$SCRIPT_DIR/web/package.json" "$1"