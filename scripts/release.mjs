#!/usr/bin/env node

import fs from 'node:fs'
import path from 'node:path'
import {spawn, spawnSync} from 'node:child_process'
import {fileURLToPath} from 'node:url'

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const LOG_DIR = path.join(ROOT, 'logs')
const BUMP_SCRIPT = path.join(ROOT, 'scripts', 'bump-version.mjs')
const VERSION_RE = /^\d+\.\d+\.\d+$/
const SNAPSHOT_VERSION_RE = /^(\d+\.\d+\.\d+)-SNAPSHOT$/i
const WHITELIST_RE = /^.. ((.*\/)?pom\.xml|web\/package\.json)$/

const EXIT_OK = 0
const EXIT_USAGE = 1
const EXIT_TESTS = 2
const EXIT_WORKSPACE = 3
const EXIT_GIT = 4
const EXIT_CONFLICT = 5

function pad(value) {
    return String(value).padStart(2, '0')
}

export function formatTimestamp(date = new Date()) {
    return `${date.getFullYear()}${pad(date.getMonth() + 1)}${pad(date.getDate())}${pad(date.getHours())}${pad(date.getMinutes())}${pad(date.getSeconds())}`
}

function formatLogTimestamp(date = new Date()) {
    return `${date.getFullYear()}${pad(date.getMonth() + 1)}${pad(date.getDate())}-${pad(date.getHours())}${pad(date.getMinutes())}${pad(date.getSeconds())}`
}

export function normalizeVersion(input, snapshot = false, now = new Date()) {
    let raw = input.replace(/\s/g, '')
    if (/^[vV]/.test(raw)) raw = raw.slice(1)
    const snapshotMatch = raw.match(SNAPSHOT_VERSION_RE)
    if (snapshotMatch || (snapshot && VERSION_RE.test(raw))) {
        const baseVersion = snapshotMatch ? snapshotMatch[1] : raw
        const snapshotBuild = formatTimestamp(now)
        const version = `${baseVersion}-SNAPSHOT`
        const npmVersion = `${version}.${snapshotBuild}`
        return {
            version,
            npmVersion,
            displayVersion: npmVersion,
            tag: `snapshot-v${baseVersion}-${snapshotBuild}`,
            isSnapshot: true,
            baseVersion,
            snapshotBuild,
        }
    }
    return {
        version: raw,
        npmVersion: raw,
        displayVersion: raw,
        tag: `v${raw}`,
        isSnapshot: false,
        baseVersion: raw,
        snapshotBuild: null,
    }
}

export function computeNext(base, pomVersion) {
    let value = base || pomVersion
    if (/^[vV]/.test(value)) value = value.slice(1)
    if (/-SNAPSHOT$/i.test(value)) value = value.slice(0, -'-SNAPSHOT'.length)
    if (!VERSION_RE.test(value)) return null
    const [major, minor, patch] = value.split('.').map(Number)
    return {
        base: value,
        patch: `${major}.${minor}.${patch + 1}`,
        minor: `${major}.${minor + 1}.0`,
        major: `${major + 1}.0.0`,
    }
}

export function isWhitelistLine(line) {
    return WHITELIST_RE.test(line)
}

export function parseArgs(argv) {
    let command = 'run'
    let version = null
    const options = {snapshot: false, skipTests: false, noPush: false, dryRun: false, noRollback: false}
    const args = [...argv]
    if (['status', 'check', 'run'].includes(args[0])) command = args.shift()
    if (args.includes('--help') || args.includes('-h')) return {command: 'help', options}

    for (let i = 0; i < args.length; i += 1) {
        const arg = args[i]
        if (arg === '--snapshot') options.snapshot = true
        else if (arg === '--skip-tests') options.skipTests = true
        else if (arg === '--no-push') options.noPush = true
        else if (arg === '--dry-run') options.dryRun = true
        else if (arg === '--no-rollback') options.noRollback = true
        else if (command === 'run' && !version) version = arg
        else return {error: `unknown argument: ${arg}`}
    }
    return {command, version, options}
}

// Node refuses to spawn .cmd/.bat shims (mvn.cmd, npm.cmd) directly on Windows
// since the 2024 hardening, so those fall back to a cmd.exe command line.
function windowsCommandLine(command, args) {
    const quote = value => {
        const text = String(value)
        return /[\s"]/.test(text) ? `"${text.replace(/"/g, '""')}"` : text
    }
    return [command, ...args].map(quote).join(' ')
}

function spawnResult(command, args, cwd, shell = false) {
    const [file, argv] = shell
        ? [windowsCommandLine(command, args), []]
        : [command, args]
    const result = spawnSync(file, argv, {cwd, encoding: 'utf8', windowsHide: true, shell})
    return {
        status: result.status,
        stdout: result.stdout || '',
        stderr: result.stderr || '',
        error: result.error,
    }
}

function run(command, args = [], cwd = ROOT) {
    let result = spawnResult(command, args, cwd)
    if (process.platform === 'win32' && result.error) {
        const fallback = spawnResult(command, args, cwd, true)
        if (!fallback.error || fallback.status !== null) result = fallback
    }
    return result
}

function commandExists(command) {
    const locator = process.platform === 'win32' ? 'where' : 'which'
    return run(locator, [command]).status === 0
}

export function readVersions() {
    let pomVersion = ''
    let packageVersion = ''
    try {
        const pom = fs.readFileSync(path.join(ROOT, 'pom.xml'), 'utf8')
        const withoutParent = pom.replace(/<parent>[\s\S]*?<\/parent>/, '')
        pomVersion = withoutParent.match(/<version>([^<]+)<\/version>/)?.[1]?.trim() || ''
    } catch {
        // Reported by the caller.
    }
    try {
        packageVersion = JSON.parse(fs.readFileSync(path.join(ROOT, 'web', 'package.json'), 'utf8')).version || ''
    } catch {
        // Reported by the caller.
    }
    return {pomVersion, packageVersion, ok: Boolean(pomVersion && packageVersion)}
}

export function usage() {
    console.log('')
    console.log('  open-admin release helper')
    console.log('')
    console.log('  Usage:')
    console.log('    node scripts/release.mjs <version> [--snapshot] [--skip-tests] [--no-push]')
    console.log('    node scripts/release.mjs [--dry-run] [--no-rollback] <version>')
    console.log('    node scripts/release.mjs status')
    console.log('    node scripts/release.mjs check')
    console.log('')
    console.log('  release : node scripts/release.mjs 3.1.3       -> tag v3.1.3')
    console.log('  snapshot: node scripts/release.mjs 3.4.0 --snapshot')
    console.log('            node scripts/release.mjs 3.4.0-SNAPSHOT')
    console.log('            -> Maven 3.4.0-SNAPSHOT, npm 3.4.0-SNAPSHOT.<build>')
    console.log('')
    console.log('  the leading v is optional')
    console.log('')
    console.log('  Exit codes: 0 ok / 1 usage or preflight / 2 tests / 3 workspace / 4 git / 5 conflict')
    return EXIT_OK
}

export class Release {
    constructor(options = {}) {
        this.options = options
        this.version = null
        this.npmVersion = null
        this.displayVersion = null
        this.isSnapshot = false
        this.baseVersion = null
        this.snapshotBuild = null
        this.tag = null
        this.branch = ''
        this.pomVersion = ''
        this.packageVersion = ''
        this.localTag = ''
        this.remoteTag = ''
        this.remoteCheckOk = false
        this.tagLocal = false
        this.tagRemote = false
        this.tagAtHead = false
        this.versionsAtTarget = false
        this.dirtyLines = []
        this.workspaceWhitelistOk = true
        this.baked = false
        this.resume = false
        this.logPath = null
        this.logFd = null
    }

    msg(text = '') {
        console.log(text)
    }

    log(text = '') {
        this.msg(text)
        if (this.logFd !== null) fs.writeSync(this.logFd, `${text}\n`)
    }

    closeLog() {
        if (this.logFd !== null) {
            fs.closeSync(this.logFd)
            this.logFd = null
        }
    }

    initLog() {
        fs.mkdirSync(LOG_DIR, {recursive: true})
        const kind = this.isSnapshot ? 'snapshot' : 'release'
        this.logPath = path.join(LOG_DIR, `${kind}-v${this.displayVersion}-${formatLogTimestamp()}.log`)
        this.logFd = fs.openSync(this.logPath, 'w')
    }

    step(index, title) {
        this.log('')
        this.log(`[${index}] ${title}`)
    }

    ok(text) {
        this.log(`    OK - ${text}`)
    }

    git(...args) {
        return run('git', args)
    }

    async gitLogged(...args) {
        return this.runLogged('git', args)
    }

    async runLogged(command, args) {
        let result = await this.spawnLogged(command, args)
        if (process.platform === 'win32' && result.error) {
            result = await this.spawnLogged(command, args, true)
        }
        return result.code
    }

    spawnLogged(command, args, shell = false) {
        return new Promise(resolve => {
            const [file, argv] = shell
                ? [windowsCommandLine(command, args), []]
                : [command, args]
            const child = spawn(file, argv, {
                cwd: ROOT,
                stdio: ['ignore', 'pipe', 'pipe'],
                windowsHide: true,
                shell,
            })
            const write = text => {
                process.stdout.write(text)
                if (this.logFd !== null) fs.writeSync(this.logFd, text)
            }
            child.stdout.setEncoding('utf8')
            child.stderr.setEncoding('utf8')
            child.stdout.on('data', write)
            child.stderr.on('data', write)
            child.on('error', error => {
                this.log(`    ERROR: ${error.message}`)
                resolve({code: 1, error})
            })
            child.on('close', code => resolve({code: code ?? 1}))
        })
    }

    readTags() {
        const local = this.git('describe', '--tags', '--abbrev=0', '--match', 'v*')
        this.localTag = local.status === 0 ? local.stdout.trim() : ''
        const remote = this.git('ls-remote', '--tags', '--refs', '--sort=-v:refname', 'origin', 'refs/tags/v*')
        this.remoteCheckOk = remote.status === 0
        this.remoteTag = ''
        if (this.remoteCheckOk) {
            for (const line of remote.stdout.split(/\r?\n/)) {
                const parts = line.split(/\s+/)
                if (parts.length >= 2 && parts[1].startsWith('refs/tags/')) {
                    this.remoteTag = parts[1].slice('refs/tags/'.length)
                    break
                }
            }
        }
    }

    readDirty() {
        const result = this.git('status', '--porcelain')
        this.dirtyLines = result.status === 0 ? result.stdout.split(/\r?\n/).filter(Boolean) : []
        this.workspaceWhitelistOk = this.dirtyLines.every(isWhitelistLine)
        return this.dirtyLines.length
    }

    checkWhitelist() {
        this.workspaceWhitelistOk = this.dirtyLines.every(isWhitelistLine)
        return this.workspaceWhitelistOk
    }

    badLines() {
        return this.dirtyLines.filter(line => !isWhitelistLine(line))
    }

    getBranch() {
        this.branch = this.git('rev-parse', '--abbrev-ref', 'HEAD').stdout.trim()
    }

    shortSha() {
        return this.git('rev-parse', '--short', 'HEAD').stdout.trim()
    }

    loadVersions() {
        const versions = readVersions()
        this.pomVersion = versions.pomVersion
        this.packageVersion = versions.packageVersion
        return versions.ok
    }

    tagExistsLocal() {
        return this.git('rev-parse', '-q', '--verify', `refs/tags/${this.tag}`).status === 0
    }

    computeTagAtHead() {
        if (!this.tagLocal) return false
        const tagSha = this.git('rev-list', '-n', '1', this.tag)
        if (tagSha.status !== 0 || !tagSha.stdout.trim()) return false
        return tagSha.stdout.trim() === this.git('rev-parse', 'HEAD').stdout.trim()
    }

    tagExistsRemote() {
        const result = this.git('ls-remote', '--tags', '--refs', 'origin', `refs/tags/${this.tag}`)
        this.remoteCheckOk = result.status === 0
        return this.remoteCheckOk && Boolean(result.stdout.trim())
    }

    async preflight() {
        this.log(`    repository: ${ROOT}`)
        for (const command of ['git', 'node', 'mvn', 'npm', 'gh']) {
            if (!commandExists(command)) {
                this.log(`    ERROR: required command not found: ${command}`)
                return false
            }
        }
        if (run('gh', ['auth', 'status']).status !== 0) {
            this.log('    ERROR: gh is not authenticated, run: gh auth login')
            return false
        }
        if (this.git('rev-parse', '--git-dir').status !== 0) {
            this.log('    ERROR: not a git repository')
            return false
        }
        if (this.git('remote', 'get-url', 'origin').status !== 0) {
            this.log("    ERROR: git remote 'origin' is not configured")
            return false
        }
        this.getBranch()
        if (this.branch !== 'main') {
            this.log(`    ERROR: current branch is ${this.branch}, expected main`)
            return false
        }

        const gitDir = this.git('rev-parse', '--git-dir').stdout.trim()
        const gitDirPath = path.resolve(ROOT, gitDir)
        for (const marker of ['MERGE_HEAD', 'CHERRY_PICK_HEAD', 'REVERT_HEAD']) {
            if (fs.existsSync(path.join(gitDirPath, marker))) {
                this.log(`    ERROR: unfinished git operation: ${marker}`)
                return false
            }
        }
        for (const marker of ['rebase-merge', 'rebase-apply']) {
            if (fs.existsSync(path.join(gitDirPath, marker))) {
                this.log('    ERROR: a rebase/am is in progress')
                return false
            }
        }
        this.ok('git, node, mvn, npm, gh ready; branch=main; no unfinished git operation')
        return true
    }

    rollback() {
        if (!this.baked || this.resume) return
        if (this.options.noRollback) {
            this.log('   version bump kept (--no-rollback)')
            return
        }
        this.readDirty()
        if (!this.checkWhitelist()) {
            this.log('   rollback skipped: workspace has files outside the whitelist')
            return
        }
        this.log('   rolling back the version bump')
        for (const line of this.dirtyLines) this.git('checkout', '--', line.slice(3))
        this.baked = false
    }

    fail(code, message) {
        this.log('')
        this.log(`!! ${message}`)
        this.rollback()
        if (this.logPath) this.log(`   log: ${this.logPath}`)
        return code
    }

    async cmdStatus() {
        this.msg('')
        this.msg('==================== open-admin release status ====================')
        if (!this.loadVersions()) return this.fail(EXIT_USAGE, 'cannot read versions from pom.xml / web/package.json')
        this.readTags()
        this.readDirty()
        this.getBranch()
        this.msg(`  repository      : ${ROOT}`)
        this.msg(`  branch          : ${this.branch}`)
        this.msg(`  pom.xml version : ${this.pomVersion}`)
        this.msg(`  npm version     : ${this.packageVersion}`)
        this.msg(`  latest tag local: ${this.localTag}`)
        this.msg(`  latest tag origin: ${this.remoteTag}`)
        this.msg(`  modified files  : ${this.dirtyLines.length}`)
        if (!this.workspaceWhitelistOk) this.msg('  workspace       : dirty (files outside whitelist)')
        else if (this.dirtyLines.length) this.msg('  workspace       : dirty (version files only)')
        else this.msg('  workspace       : clean')
        this.msg('==================================================================')
        this.msg('')
        return EXIT_OK
    }

    async cmdCheck() {
        if (!this.loadVersions()) return this.fail(EXIT_USAGE, 'cannot read versions from pom.xml / web/package.json')
        this.readTags()
        this.readDirty()
        this.getBranch()
        const next = computeNext(this.remoteTag || this.localTag, this.pomVersion)
        if (!next) {
            this.msg('')
            this.msg(`  ERROR: cannot parse latest tag: ${this.remoteTag || this.localTag || this.pomVersion}`)
            return EXIT_USAGE
        }
        const workspace = !this.workspaceWhitelistOk
            ? 'dirty-outside-whitelist'
            : this.dirtyLines.length ? 'dirty-version-files' : 'clean'
        this.msg(`REPO_ROOT=${ROOT}`)
        this.msg(`BRANCH=${this.branch}`)
        this.msg(`POM_VERSION=${this.pomVersion}`)
        this.msg(`NPM_VERSION=${this.packageVersion}`)
        this.msg(`LATEST_TAG_LOCAL=${this.localTag}`)
        this.msg(`LATEST_TAG_REMOTE=${this.remoteTag}`)
        this.msg(`BASE_VERSION=${next.base}`)
        this.msg(`NEXT_PATCH=${next.patch}`)
        this.msg(`NEXT_MINOR=${next.minor}`)
        this.msg(`NEXT_MAJOR=${next.major}`)
        this.msg(`WORKSPACE=${workspace}`)
        if (!this.remoteCheckOk) {
            this.msg('TAGS_IN_SYNC=unknown')
            this.msg('WARN=cannot reach origin, remote tag check skipped')
        } else {
            this.msg(`TAGS_IN_SYNC=${this.localTag === this.remoteTag ? 'yes' : 'no'}`)
        }
        return EXIT_OK
    }

    workspaceViolation() {
        this.log('')
        this.log('!! workspace has files outside the whitelist:')
        for (const line of this.badLines()) this.log(`   BAD: ${line}`)
        this.log('   whitelist: */pom.xml, web/package.json')
        return this.fail(EXIT_WORKSPACE, 'workspace is not clean')
    }

    async cmdRun() {
        if (!this.options.version) {
            this.msg('')
            this.msg('  ERROR: no version given')
            this.msg('  use: node scripts/release.mjs 3.1.3')
            usage()
            return EXIT_USAGE
        }
        const normalized = normalizeVersion(this.options.version, this.options.snapshot)
        Object.assign(this, normalized)
        if (!this.isSnapshot && !VERSION_RE.test(this.version)) {
            this.msg('')
            this.msg(`  ERROR: invalid version '${this.version}', expected x.y.z`)
            this.msg('  snapshot: 3.4.0 --snapshot | 3.4.0-SNAPSHOT')
            return EXIT_USAGE
        }

        const kind = this.isSnapshot ? 'snapshot' : 'release'
        const label = this.isSnapshot ? this.displayVersion : `v${this.version}`
        this.initLog()
        this.log('==================================================================')
        this.log(` open-admin ${kind} ${label}`)
        this.log(` maven: ${this.version}`)
        this.log(` npm  : ${this.npmVersion}`)
        this.log(` tag  : ${this.tag}`)
        this.log(` root : ${ROOT}`)
        this.log(` log  : ${this.logPath}`)
        this.log('==================================================================')

        this.step('1/8', 'Preflight checks')
        if (!await this.preflight()) return this.fail(EXIT_USAGE, 'preflight checks failed')

        this.step('2/8', 'Version, tag and workspace checks')
        if (!this.loadVersions()) return this.fail(EXIT_USAGE, 'cannot read versions from pom.xml / web/package.json')
        this.tagLocal = this.tagExistsLocal()
        this.tagAtHead = this.computeTagAtHead()
        this.versionsAtTarget = this.pomVersion === this.version && this.packageVersion === this.npmVersion
        this.log(`    pom.xml=${this.pomVersion}  web/package.json=${this.packageVersion}  target=${this.version} / npm ${this.npmVersion}`)

        if (this.tagLocal) {
            if (this.tagAtHead && this.versionsAtTarget) {
                this.resume = true
                this.ok(`tag ${this.tag} already at HEAD - resume mode, only the push is left`)
            } else {
                this.log(`    local tag ${this.tag} already exists and does not match the current state`)
                this.log(`    to start over: git tag -d ${this.tag}`)
                return this.fail(EXIT_CONFLICT, `local tag ${this.tag} already exists`)
            }
        } else {
            this.tagRemote = this.tagExistsRemote()
            if (!this.remoteCheckOk) this.log('    WARN: cannot reach origin, remote tag check skipped')
            if (this.tagRemote) {
                this.log(`    tag ${this.tag} already exists on origin`)
                return this.fail(EXIT_CONFLICT, `tag ${this.tag} already exists on origin`)
            }
        }

        this.readDirty()
        if (!this.checkWhitelist()) return this.workspaceViolation()
        this.log(`    workspace: ${this.dirtyLines.length} modified file(s)`)
        if (this.dirtyLines.length) {
            if (this.versionsAtTarget) {
                this.baked = true
                this.ok(`version files already at ${label}, bump will be skipped`)
            } else if (!this.options.dryRun) {
                this.log('    resetting leftover version changes from a previous run')
                for (const line of this.dirtyLines) this.git('checkout', '--', line.slice(3))
            } else {
                this.log('    [dry-run] would reset leftover version changes')
            }
        }

        if (this.options.dryRun) {
            this.log('')
            this.log('[dry-run] would execute:')
            this.log(`    1) node scripts/bump-version.mjs ${this.version}${this.npmVersion === this.version ? '' : ` --npm-version ${this.npmVersion}`}`)
            this.log('    2) mvn -B clean test')
            this.log('    3) npm run build, run inside web')
            this.log(`    4) git add -u ; git commit ; git tag -a ${this.tag}`)
            this.log(`    5) git push origin ${this.branch} ; git push origin ${this.tag}`)
            if (this.resume) this.log('    state: resume, only step 5 would run')
            if (this.baked) this.log('    state: version already at target, step 1 would be skipped')
            this.log('[dry-run] no repo file was modified')
            this.log('')
            return EXIT_OK
        }

        if (!this.baked && !this.resume) {
            this.step('3/8', `Bump version to ${label}`)
            this.baked = true
            const args = [BUMP_SCRIPT, this.version]
            if (this.npmVersion !== this.version) args.push('--npm-version', this.npmVersion)
            if (await this.runLogged(process.execPath, args) !== 0) {
                return this.fail(EXIT_USAGE, 'bump-version.mjs failed')
            }
        }

        this.step('4/8', 'Verify pom.xml and web/package.json')
        if (!this.loadVersions()) return this.fail(EXIT_USAGE, 'cannot read versions from pom.xml / web/package.json')
        if (this.pomVersion !== this.version || this.packageVersion !== this.npmVersion) {
            return this.fail(EXIT_CONFLICT, `version mismatch after bump: pom.xml=${this.pomVersion} npm=${this.packageVersion} expected=${this.version} / ${this.npmVersion}`)
        }
        this.ok(`pom.xml=${this.version}, web/package.json=${this.npmVersion}`)

        this.step('5/8', 'Tests')
        if (this.resume) {
            // Resume mode only pushes the existing commit and tag.
        } else if (this.options.skipTests) {
            this.log('    WARN: tests skipped (--skip-tests)')
        } else {
            this.log('    mvn -B clean test')
            if (await this.runLogged('mvn', ['-B', 'clean', 'test']) !== 0) {
                return this.fail(EXIT_TESTS, 'backend tests failed (mvn -B clean test)')
            }
            this.ok('backend tests passed')
            if (!fs.existsSync(path.join(ROOT, 'web', 'node_modules'))) {
                this.log('    web/node_modules missing, running npm install first')
                if (await this.runLogged('npm', ['--prefix', 'web', 'install']) !== 0) {
                    return this.fail(EXIT_TESTS, 'npm install failed')
                }
            }
            this.log('    npm run build in web folder')
            if (await this.runLogged('npm', ['--prefix', 'web', 'run', 'build']) !== 0) {
                return this.fail(EXIT_TESTS, 'frontend build failed (npm run build)')
            }
            this.ok('frontend build passed')
        }

        this.step('6/8', 'Workspace whitelist check')
        this.readDirty()
        if (!this.checkWhitelist()) return this.workspaceViolation()
        this.log(`    modified: ${this.dirtyLines.length} files, all inside the whitelist`)
        this.ok('no unexpected artifacts')

        if (!this.resume) {
            this.step('7/8', `Commit and tag ${this.tag}`)
            if (await this.gitLogged('add', '-u') !== 0) return this.fail(EXIT_GIT, 'git add failed')
            const message = this.isSnapshot ? `snapshot: ${this.displayVersion}` : `release: v${this.version}`
            if (await this.gitLogged('commit', '-m', message) !== 0) return this.fail(EXIT_GIT, 'git commit failed')
            if (await this.gitLogged('tag', '-a', this.tag, '-m', message) !== 0) return this.fail(EXIT_GIT, 'git tag failed')
            this.ok('commit and tag created')
        }

        if (this.options.noPush) {
            const kindLabel = this.isSnapshot ? 'Snapshot' : 'Release'
            this.log('')
            this.log('==================================================================')
            this.log(` ${kindLabel} ${label} committed and tagged, not pushed because of --no-push`)
            this.log(` tag    : ${this.tag}`)
            this.log(` commit : ${this.shortSha()}`)
            this.log(` log    : ${this.logPath}`)
            this.log(' to finish, run:')
            this.log(`   git push origin ${this.branch}`)
            this.log(`   git push origin ${this.tag}`)
            this.log(' to undo, run:')
            this.log(`   git tag -d ${this.tag}`)
            this.log(`   git reset --hard origin/${this.branch}`)
            this.log('==================================================================')
            this.log('')
            return EXIT_OK
        }

        this.step('8/8', `Push ${this.branch} and ${this.tag} to origin`)
        if (await this.gitLogged('push', 'origin', this.branch) !== 0) return this.fail(EXIT_GIT, 'git push branch failed')
        if (await this.gitLogged('push', 'origin', this.tag) !== 0) return this.fail(EXIT_GIT, 'git push tag failed')
        this.ok('pushed to origin')

        const kindLabel = this.isSnapshot ? 'Snapshot' : 'Release'
        this.log('')
        this.log('==================================================================')
        this.log(` ${kindLabel} ${label} finished`)
        this.log(` maven  : ${this.version}`)
        this.log(` npm    : ${this.npmVersion}`)
        this.log(` tag    : ${this.tag}`)
        this.log(` commit : ${this.shortSha()}`)
        this.log(` log    : ${this.logPath}`)
        this.log(' next   : CI publish.yml runs on the tag push, see gh run list --limit 3')
        this.log('==================================================================')
        this.log('')
        return EXIT_OK
    }
}

export async function main(argv = process.argv.slice(2)) {
    const parsed = parseArgs(argv)
    if (parsed.error) {
        console.error(`ERROR: ${parsed.error}`)
        usage()
        return EXIT_USAGE
    }
    if (parsed.command === 'help') return usage()
    const release = new Release({...parsed.options, version: parsed.version})
    try {
        if (parsed.command === 'status') return await release.cmdStatus()
        if (parsed.command === 'check') return await release.cmdCheck()
        return await release.cmdRun()
    } catch (error) {
        return release.fail(EXIT_USAGE, `unexpected error: ${error.message}`)
    } finally {
        release.closeLog()
    }
}

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
    process.exit(await main())
}
