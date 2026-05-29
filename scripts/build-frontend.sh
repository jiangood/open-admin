#!/bin/bash
set -e
cd "$(dirname "$0")/.."
cd web
npm install
npm run build