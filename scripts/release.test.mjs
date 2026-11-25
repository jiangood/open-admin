import assert from 'node:assert/strict'
import test from 'node:test'

import {
    computeNext,
    formatTimestamp,
    isWhitelistLine,
    normalizeVersion,
    parseArgs,
} from './release.mjs'
import {
    parseArgs as parseBumpArgs,
    replaceChildModuleVersion,
    replacePackageJsonVersion,
    replaceRootPomVersion,
} from './bump-version.mjs'

test('formats snapshot build timestamps in local time', () => {
    assert.equal(formatTimestamp(new Date(2026, 10, 25, 18, 6, 35)), '20261125180635')
})

test('normalizes release versions and tags', () => {
    assert.deepEqual(normalizeVersion('v3.1.3'), {
        version: '3.1.3',
        npmVersion: '3.1.3',
        displayVersion: '3.1.3',
        tag: 'v3.1.3',
        isSnapshot: false,
        baseVersion: '3.1.3',
        snapshotBuild: null,
    })
})

test('normalizes snapshot versions and tags', () => {
    const result = normalizeVersion('3.4.0', true, new Date(2026, 10, 25, 18, 6, 35))
    assert.equal(result.version, '3.4.0-SNAPSHOT')
    assert.equal(result.npmVersion, '3.4.0-SNAPSHOT.20261125180635')
    assert.equal(result.tag, 'snapshot-v3.4.0-20261125180635')
    assert.equal(result.isSnapshot, true)
})

test('accepts an explicit SNAPSHOT version', () => {
    const result = normalizeVersion('3.4.0-SNAPSHOT', false, new Date(2026, 10, 25, 18, 6, 35))
    assert.equal(result.npmVersion, '3.4.0-SNAPSHOT.20261125180635')
    assert.equal(result.tag, 'snapshot-v3.4.0-20261125180635')
})

test('computes the next release versions', () => {
    assert.deepEqual(computeNext('v3.3.3', '3.3.4-SNAPSHOT'), {
        base: '3.3.3',
        patch: '3.3.4',
        minor: '3.4.0',
        major: '4.0.0',
    })
    assert.equal(computeNext('', '3.3.4-SNAPSHOT').base, '3.3.4')
})

test('checks the release workspace whitelist', () => {
    assert.equal(isWhitelistLine(' M pom.xml'), true)
    assert.equal(isWhitelistLine('M  web/package.json'), true)
    assert.equal(isWhitelistLine(' M README.md'), false)
    assert.equal(isWhitelistLine('?? scripts/release.mjs'), false)
})

test('parses release command options', () => {
    const parsed = parseArgs(['3.4.0', '--snapshot', '--dry-run'])
    assert.equal(parsed.command, 'run')
    assert.equal(parsed.version, '3.4.0')
    assert.equal(parsed.options.snapshot, true)
    assert.equal(parsed.options.dryRun, true)
    assert.equal(parseArgs(['status']).command, 'status')
})

test('bump script validates Maven and npm versions', () => {
    assert.deepEqual(parseBumpArgs(['3.4.0-SNAPSHOT', '--npm-version', '3.4.0-SNAPSHOT.1']), {
        newVersion: '3.4.0-SNAPSHOT',
        npmVersion: '3.4.0-SNAPSHOT.1',
    })
    assert.match(parseBumpArgs(['3.4.0-SNAPSHOT.1']).error, /invalid Maven version/)
})

test('root POM bump preserves the parent version', () => {
    const input = '<project><parent><version>4.1.2</version></parent><artifactId>open-admin</artifactId><version>3.3.4-SNAPSHOT</version></project>'
    const output = replaceRootPomVersion(input, '3.4.0-SNAPSHOT')
    assert.match(output, /<parent><version>4\.1\.2<\/version><\/parent>/)
    assert.match(output, /<artifactId>open-admin<\/artifactId><version>3\.4\.0-SNAPSHOT<\/version>/)
})

test('child POM bump only changes the parent version', () => {
    const input = '<project><parent><version>3.3.3</version></parent><dependencies><dependency><version>1.2.3</version></dependency></dependencies></project>'
    const output = replaceChildModuleVersion(input, '3.3.4')
    assert.match(output, /<parent><version>3\.3\.4<\/version><\/parent>/)
    assert.match(output, /<dependency><version>1\.2\.3<\/version>/)
})

test('package bump preserves formatting', () => {
    assert.equal(
        replacePackageJsonVersion('{\n  "version": "3.3.4-SNAPSHOT.1"\n}', '3.3.4-SNAPSHOT.2'),
        '{\n  "version": "3.3.4-SNAPSHOT.2"\n}',
    )
})
