#!/bin/bash
set -e

VERSION=$1
if [ -z "$VERSION" ]; then
  echo "Usage: ./bump-version.sh <version>"
  echo "Example: ./bump-version.sh 2.3.0"
  exit 1
fi

if ! [[ "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "Error: Version must be in semver format (e.g., 2.3.0)"
  exit 1
fi

POM_FILE="pom.xml"
PKG_FILE="web/package.json"

for f in "$POM_FILE" "$PKG_FILE"; do
  if [ ! -f "$f" ]; then
    echo "Error: $f not found"
    exit 1
  fi
done

CURRENT_POM=$(awk '/<artifactId>open-admin<\/artifactId>/{found=1} found && /<version>[0-9]/{print; exit}' "$POM_FILE" | grep -oP '(?<=<version>)[0-9]+\.[0-9]+\.[0-9]+(?=</version>)')
CURRENT_NPM=$(grep -oP '(?<="version": ")[0-9]+\.[0-9]+\.[0-9]+(?=")' "$PKG_FILE")

if [ "$CURRENT_POM" != "$CURRENT_NPM" ]; then
  echo "Error: pom.xml ($CURRENT_POM) and package.json ($CURRENT_NPM) versions are out of sync"
  exit 1
fi

if [ "$CURRENT_POM" = "$VERSION" ]; then
  echo "Already at version $VERSION, nothing to do"
  exit 0
fi

sed -i "0,/<version>${CURRENT_POM//./\\.}<\/version>/s//<version>${VERSION}<\/version>/" "$POM_FILE"
sed -i "0,/\"version\": \"${CURRENT_NPM}\"/s//\"version\": \"${VERSION}\"/" "$PKG_FILE"

echo "Bumped version: $CURRENT_POM -> $VERSION"
echo "  pom.xml:       $(awk '/<artifactId>open-admin<\/artifactId>/{found=1} found && /<version>[0-9]/{print; exit}' "$POM_FILE" | grep -oP '(?<=<version>)[0-9]+\.[0-9]+\.[0-9]+(?=</version>)')"
echo "  package.json:  $(grep -oP '(?<="version": ")[0-9]+\.[0-9]+\.[0-9]+(?=")' "$PKG_FILE")"
