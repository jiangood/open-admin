const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const version = process.argv[2];
if (!version) {
  console.error('Usage: node scripts/generate-build-info.js <version>');
  console.error('Example: node scripts/generate-build-info.js 2.5.5');
  process.exit(1);
}

const props = `build.artifact=open-admin
build.group=io.github.jiangood
build.name=open-admin
build.version=${version}
build.time=${new Date().toISOString()}
`;

const outFile = path.resolve(ROOT, 'src/main/resources/build-info.properties');
fs.writeFileSync(outFile, props);
console.log(`build-info.properties generated (version=${version})`);
