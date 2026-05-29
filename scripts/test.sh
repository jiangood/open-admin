#!/bin/bash
set -e
cd "$(dirname "$0")/.."
./mvnw test -q