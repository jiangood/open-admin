#!/usr/bin/env node

import fs from 'node:fs'
import path from 'node:path'
import {fileURLToPath} from 'node:url'

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const PACKAGE_JSON_PATH = 'web/package.json'
const EXCLUDE_DIRS = new Set(['node_modules', '.git', '.svn', 'target', '.mvn', 'dist', 'build', '.gradle'])
const MAVEN_VERSION_RE = /^\d+\.\d+\.\d+(?:-SNAPSHOT)?$/
const NPM_VERSION_RE = /^\d+\.\d+\.\d+(?:-SNAPSHOT(?:\.(?:0|[1-9]\d*))?)?$/
const VERSION_PATTERN = String.raw`\d+\.\d+\.\d+(?:-SNAPSHOT(?:\.\d+)?)?`

export function getChildModuleDirs(rootPomContent) {
    const modulesMatch = rootPomContent.match(/<modules>([\s\S]*?)<\/modules>/)
    if (!modulesMatch) return new Set()
    return new Set([...modulesMatch[1].matchAll(/<module>(.*?)<\/module>/g)].map(match => match[1].trim()))
}

export function replaceRootPomVersion(content, version) {
    const parentMatch = content.match(/<parent>[\s\S]*?<\/parent>/)
    const pattern = new RegExp(`(<version>)${VERSION_PATTERN}(</version>)`)
    const replacement = `$1${version}$2`
    if (!parentMatch) return content.replace(pattern, replacement)
    const placeholder = '<!--__PARENT_BLOCK__-->'
    const hidden = content.replace(parentMatch[0], placeholder)
    return hidden.replace(pattern, replacement).replace(placeholder, parentMatch[0])
}

export function replaceChildModuleVersion(content, version) {
    const pattern = new RegExp(`(<parent>[\\s\\S]*?<version>)${VERSION_PATTERN}(</version>[\\s\\S]*?</parent>)`)
    return content.replace(pattern, `$1${version}$2`)
}

export function replacePackageJsonVersion(content, version) {
    const pattern = new RegExp(`("version":\\s*")${VERSION_PATTERN}(")`)
    return content.replace(pattern, `$1${version}$2`)
}

export function findPomFiles(directory = ROOT) {
    const results = []
    let entries
    try {
        entries = fs.readdirSync(directory, {withFileTypes: true})
    } catch {
        return results
    }
    for (const entry of entries) {
        if (EXCLUDE_DIRS.has(entry.name)) continue
        const absolutePath = path.join(directory, entry.name)
        if (entry.isDirectory()) {
            results.push(...findPomFiles(absolutePath))
        } else if (entry.name === 'pom.xml') {
            results.push(absolutePath)
        }
    }
    return results
}

export function parseArgs(argv) {
    let newVersion
    let npmVersion
    for (let i = 0; i < argv.length; i += 1) {
        const arg = argv[i]
        if (arg === '--npm-version') {
            if (i + 1 >= argv.length) return {error: '--npm-version requires a value'}
            npmVersion = argv[++i]
        } else if (arg.startsWith('--npm-version=')) {
            npmVersion = arg.slice('--npm-version='.length)
        } else if (!newVersion) {
            newVersion = arg
        } else {
            return {error: `unknown argument: ${arg}`}
        }
    }
    if (!newVersion) return {error: 'provide a version, for example: node scripts/bump-version.mjs 3.4.0'}
    if (!MAVEN_VERSION_RE.test(newVersion)) {
        return {error: `invalid Maven version: ${newVersion}, expected x.y.z or x.y.z-SNAPSHOT`}
    }
    npmVersion ||= newVersion
    if (!NPM_VERSION_RE.test(npmVersion)) {
        return {error: `invalid npm version: ${npmVersion}, expected x.y.z, x.y.z-SNAPSHOT or x.y.z-SNAPSHOT.N`}
    }
    return {newVersion, npmVersion}
}

export function main(argv = process.argv.slice(2)) {
    const parsed = parseArgs(argv)
    if (parsed.error) {
        console.error(`ERROR: ${parsed.error}`)
        return 1
    }
    const {newVersion, npmVersion} = parsed
    console.log(`\nBumping version to ${newVersion} (npm: ${npmVersion})\n`)

    const rootPomPath = path.join(ROOT, 'pom.xml')
    let rootPomContent
    try {
        rootPomContent = fs.readFileSync(rootPomPath, 'utf8')
    } catch {
        console.error('ERROR: root pom.xml not found')
        return 1
    }
    const childModuleDirs = getChildModuleDirs(rootPomContent)
    console.log(`Child modules (${childModuleDirs.size}): ${[...childModuleDirs].sort().join(', ') || 'none'}\n`)

    let updatedCount = 0
    for (const absolutePath of findPomFiles()) {
        const content = fs.readFileSync(absolutePath, 'utf8')
        const relativePath = path.relative(ROOT, absolutePath).split(path.sep).join('/')
        const parentPath = path.relative(ROOT, path.dirname(absolutePath)).split(path.sep).join('/')
        let type
        let newContent
        if (relativePath === 'pom.xml') {
            type = 'root pom'
            newContent = replaceRootPomVersion(content, newVersion)
        } else if (childModuleDirs.has(parentPath)) {
            type = 'child module'
            newContent = replaceChildModuleVersion(content, newVersion)
        } else {
            type = 'other pom'
            newContent = replaceRootPomVersion(content, newVersion)
        }
        if (content === newContent) {
            console.log(`SKIP [${type}] version not found: ${relativePath}`)
            continue
        }
        fs.writeFileSync(absolutePath, newContent, 'utf8')
        console.log(`OK   [${type}] ${relativePath}`)
        updatedCount += 1
    }

    const packagePath = path.join(ROOT, PACKAGE_JSON_PATH)
    if (fs.existsSync(packagePath)) {
        const content = fs.readFileSync(packagePath, 'utf8')
        const newContent = replacePackageJsonVersion(content, npmVersion)
        if (content === newContent) {
            console.log(`SKIP package.json version not found: ${PACKAGE_JSON_PATH}`)
        } else {
            fs.writeFileSync(packagePath, newContent, 'utf8')
            console.log(`OK   package.json: ${PACKAGE_JSON_PATH}`)
            updatedCount += 1
        }
    }

    console.log(`\nDone. Updated ${updatedCount} file(s).\n`)
    return 0
}

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
    process.exit(main())
}
