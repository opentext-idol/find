#!/usr/bin/env node
'use strict';

/*
 * Re-parses the (now CJS) find/** source tree and asserts that every file listed in
 * webapp/idol/build/side-effect-requires.json still contains exactly its recorded bare
 * `require(...)` calls (side-effect-only, not assigned to a variable), in the same
 * multiset AND ORDER. Order matters here - e.g. `find/app/page/search/results/map-view.js`
 * needs `Leaflet.awesome-markers` before `leaflet.markercluster`, and the two
 * `flot.stack`/`flot.categories` sites need their historical shim ordering preserved.
 *
 * Run as an npm `prebuild` script so a future edit that reorders/drops one of these
 * "invisible" dependencies (no build error, a feature just stops initialising) fails
 * loudly instead of silently.
 */

const fs = require('fs');
const path = require('path');
const parser = require('@babel/parser');
const { toModuleId } = require('./codemod/amd-to-cjs');

const MANIFEST_PATH = path.resolve(__dirname, 'side-effect-requires.json');
const STATIC_JS_ROOTS = [
    path.resolve(__dirname, '..', 'src/main/public/static/js'),
    path.resolve(__dirname, '..', '..', 'core/src/main/public/static/js')
];

function findFileForModuleId(moduleId) {
    for(const root of STATIC_JS_ROOTS) {
        const candidate = path.join(root, ...moduleId.split('/')) + '.js';

        if(fs.existsSync(candidate)) {
            return candidate;
        }
    }

    return null;
}

// Returns the ordered list of string ids passed to bare, top-level `require(...)` calls
// (i.e. ExpressionStatements, not assigned to anything) in the given source.
function bareRequireIds(source) {
    const ast = parser.parse(source, { sourceType: 'script' });

    return ast.program.body
        .filter((node) =>
            node.type === 'ExpressionStatement' &&
            node.expression.type === 'CallExpression' &&
            node.expression.callee.type === 'Identifier' &&
            node.expression.callee.name === 'require' &&
            node.expression.arguments.length === 1 &&
            node.expression.arguments[0].type === 'StringLiteral'
        )
        .map((node) => node.expression.arguments[0].value);
}

function main() {
    const manifest = JSON.parse(fs.readFileSync(MANIFEST_PATH, 'utf8'));
    const moduleIds = Object.keys(manifest);
    let failures = 0;

    for(const moduleId of moduleIds) {
        const expected = manifest[moduleId];
        const file = findFileForModuleId(moduleId);

        if(file == null) {
            console.error(`MISSING  ${moduleId}: no file found for this module id`);
            failures++;
            continue;
        }

        const source = fs.readFileSync(file, 'utf8');
        const actual = bareRequireIds(source);

        const expectedTail = actual.slice(actual.length - expected.length);
        const matches = expected.length === expectedTail.length &&
            expected.every((id, i) => id === expectedTail[i]);

        if(!matches) {
            console.error(`MISMATCH ${moduleId}:\n  expected ${JSON.stringify(expected)}\n  actual   ${JSON.stringify(actual)}`);
            failures++;
        }
    }

    console.log(`Checked ${moduleIds.length} module(s) with side-effect-only requires.`);

    if(failures > 0) {
        console.error(`${failures} mismatch(es) - see above.`);
        process.exit(1);
    }
}

main();
