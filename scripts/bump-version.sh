#!/bin/bash
set -e
cd "$(dirname "$0")/.."
node scripts/bump-version.js bump pom.xml web/package.json "$1"