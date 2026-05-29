const fs = require('fs');
const path = require('path');

const cmd = process.argv[2];
const file = process.argv[3];

if (cmd === 'pom') {
  // Extract project version: find <version> after <artifactId>open-admin</artifactId>
  const xml = fs.readFileSync(file, 'utf8');
  const idx = xml.indexOf('<artifactId>open-admin</artifactId>');
  if (idx === -1) { process.exit(1); }
  const after = xml.substring(idx);
  const m = after.match(/<version>(\d+\.\d+\.\d+)<\/version>/);
  if (!m) { process.exit(1); }
  process.stdout.write(m[1]);
} else if (cmd === 'npm') {
  const pkg = JSON.parse(fs.readFileSync(file, 'utf8'));
  process.stdout.write(pkg.version);
} else if (cmd === 'bump') {
  const pomFile = process.argv[3];
  const npmFile = process.argv[4];
  const newVersion = process.argv[5];

  let pom = fs.readFileSync(pomFile, 'utf8');
  let pkg = fs.readFileSync(npmFile, 'utf8');

  const pomIdx = pom.indexOf('<artifactId>open-admin</artifactId>');
  if (pomIdx === -1) { console.error('Error: open-admin artifactId not found'); process.exit(1); }
  const after = pom.substring(pomIdx);
  const m = after.match(/<version>(\d+\.\d+\.\d+)<\/version>/);
  if (!m) { console.error('Error: project version not found'); process.exit(1); }
  const oldVersion = m[1];

  if (oldVersion === newVersion) {
    console.log('Already at version ' + newVersion + ', nothing to do');
    process.exit(0);
  }

  // Replace only the first <version> after open-admin artifactId
  const versionTagStart = pomIdx + after.indexOf('<version>' + oldVersion + '</version>');
  pom = pom.substring(0, versionTagStart) +
        '<version>' + newVersion + '</version>' +
        pom.substring(versionTagStart + ('<version>' + oldVersion + '</version>').length);

  pkg = pkg.replace('"version": "' + oldVersion + '"', '"version": "' + newVersion + '"');

  fs.writeFileSync(pomFile, pom, 'utf8');
  fs.writeFileSync(npmFile, pkg, 'utf8');

  console.log('Bumped version: ' + oldVersion + ' -> ' + newVersion);
  console.log('  pom.xml:       ' + newVersion);
  console.log('  package.json:  ' + newVersion);
}