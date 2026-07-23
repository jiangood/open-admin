const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const version = process.argv[2];
if (!version) {
  console.error('Usage: node scripts/generate-build-info.js <version>');
  console.error('Example: node scripts/generate-build-info.js 2.5.5');
  process.exit(1);
}

const buildTime = new Date().toISOString();
const targetFile = path.resolve(ROOT, 'src/main/java/io/github/jiangood/openadmin/framework/common/LibInfo.java');

const content = `package io.github.jiangood.openadmin.framework.common;

public class LibInfo {
    public static final String VERSION = "${version}";
    public static final String BUILD_TIME = "${buildTime}";

    private LibInfo() {}
}
`;

fs.writeFileSync(targetFile, content);
console.log(`LibInfo.java generated (version=${version})`);
