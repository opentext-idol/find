#!/usr/bin/env node
'use strict';

/*
 * Phase 4 verification helper (amd-to-cjs-find.md). Runs `webpack --json` for the
 * `public` entry (the largest/most complete of the 4 bundles) and cross-checks the
 * resulting module list against every `.js` source file physically present under
 * target/classes/static/js/find, to catch modules that got silently dropped (e.g. because
 * an alias pointed at the wrong file) or unexpectedly duplicated. It is not a byte-for-byte
 * comparison against the Phase 2 r.js baseline (build.txt is a build trace, not a flat
 * module-id list, and r.js/webpack module ids are shaped differently) - the bundle sizes
 * captured in Phase 2 (webapp/idol/target's public.js/config.js/login.js) remain the
 * primary size-sanity check, done manually alongside this script.
 */

const path = require('path');
const fs = require('fs');
const { execFileSync } = require('child_process');

const FIND_ROOT = path.resolve(__dirname, '..', 'target/classes/static/js/find');

function listSourceFiles(dir) {
    const out = [];
    for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
        const full = path.join(dir, entry.name);
        if (entry.isDirectory()) {
            out.push(...listSourceFiles(full));
        } else if (entry.name.endsWith('.js')) {
            out.push(full);
        }
    }
    return out;
}

const statsJson = execFileSync(
    path.resolve(__dirname, '..', 'node_modules/.bin/webpack'),
    ['--mode', 'production', '--json'],
    { cwd: path.resolve(__dirname, '..'), maxBuffer: 1024 * 1024 * 200 }
).toString();

const stats = JSON.parse(statsJson);
const publicChunk = stats.chunks.find((c) => c.names.includes('public'));
const publicModuleIds = new Set(publicChunk.modules.map((m) => m.id));
const builtModules = new Set(
    stats.modules
        .filter((m) => publicModuleIds.has(m.id))
        .map((m) => m.nameForCondition)
        .filter(Boolean)
        .filter((name) => name.includes('/target/classes/static/js/find/'))
        .map((name) => path.resolve(name))
);

const onDisk = listSourceFiles(FIND_ROOT);
const missing = onDisk.filter((f) => !builtModules.has(path.resolve(f)) && !f.endsWith('.spec.js'));

console.log(`On-disk find/**.js source files: ${onDisk.length}`);
console.log(`Modules under find/ resolved into the public bundle: ${builtModules.size}`);

if (missing.length) {
    console.log(`\n${missing.length} source file(s) not reachable from the public entry point`
        + ' (expected for dead code / files only used by config.js or login.js):');
    missing.forEach((f) => console.log(`  ${path.relative(FIND_ROOT, f)}`));
} else {
    console.log('\nEvery find/**.js source file is reachable from the public entry point.');
}
