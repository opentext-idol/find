#!/usr/bin/env node
'use strict';

/*
 * AMD -> CommonJS codemod for Find's own JS sources (see amd-to-cjs-find.md, Phase 7).
 *
 * Deliberately NOT jscodeshift/recast: those reindent the moved factory body
 * heuristically, and across ~275 files that makes genuine changes indistinguishable from
 * reformatting. Instead: @babel/parser is used for ANALYSIS ONLY (finding node
 * boundaries), and the output is produced by splicing verbatim slices of the original
 * source text - the licence header, the factory body (minus the final `return` and the
 * hoisted `'use strict'`), and the final returned expression, are all copied byte-for-byte
 * from the input, not reprinted.
 *
 * Algorithm: parse -> find the single top-level `define(...)` ExpressionStatement ->
 * copy `source.slice(0, node.start)` verbatim (the licence header, plus anything else
 * before the call) -> emit per the rules below -> copy `source.slice(node.end)` verbatim
 * (normally just a trailing newline) -> `node --check` the result as a syntax gate.
 *
 * Rules:
 *  - `define([ids], function(params) { body })`: bound deps (one per param, by position)
 *    become `const <param> = require(<id>);` in original order; any trailing deps beyond
 *    `params.length` are side-effect-only and become bare `require(<id>);` calls under a
 *    fixed banner comment, in original order. The factory's final `return <expr>;`
 *    becomes `module.exports = <expr>;`, splicing `<expr>`'s ORIGINAL source text (matters
 *    for the large object literals some views return). The body is dedented by exactly 4
 *    spaces (the AMD nesting the factory added).
 *  - `'use strict'`: if it is the factory body's first statement, hoisted above the
 *    requires and dropped from the body. If absent, NOT added - some files rely on
 *    sloppy-mode behaviour.
 *  - `define(function(params) { body })`: same as above with zero deps.
 *  - `define({ ... })`: passthrough object literal -> `module.exports = { ... };`,
 *    splicing the object's original source text.
 *  - ID mapping: strip a leading `text!` or `i18n!` prefix; leave `./x`/`../x` and bare
 *    ids otherwise unchanged; never append an extension; hard-fail on any other `!`-using
 *    loader-plugin prefix (there should be none left in Find's own sources by this phase).
 *  - Hard-fail (never guess/truncate) if: `params.length > deps.length`; the factory has a
 *    non-final top-level `return` (i.e. one that isn't the literal last statement); the
 *    factory references the special `arguments` object directly in its own scope (nested
 *    plain `function` expressions get their own `arguments` and are not walked further;
 *    arrow functions are, since they don't shadow it); any dep id after loader-prefix
 *    stripping still contains a `!` (i.e. an unrecognised loader plugin).
 *
 * Files this codemod must NOT touch (handled elsewhere, see caller):
 *  - The 5 i18n master bundles (find/nls/{bundle,errors,indexes}.js,
 *    find/idol/nls/{comparisons,snapshots}.js) - hand-rewritten in place to the
 *    `find/nls/select-locale`-based CJS form described in amd-to-cjs-find.md.
 *  - idol/.../themetracker/themetracker.js - the one deliberate UMD file; webpack
 *    understands its `typeof define === 'function' && define.amd` branch natively, and
 *    converting it would be an unrelated API change to a file shaped like a
 *    redistributable library.
 *
 * Side effect: for every file with trailing (side-effect-only) deps, records
 * `{ [moduleId]: [depId, ...] }` (in original order) into the manifest object passed in by
 * the caller, which is written once, after the whole run, to
 * `webapp/idol/build/side-effect-requires.json`. `check-side-effect-requires.js` re-parses
 * the resulting CJS tree and asserts the multiset AND order of each file's bare
 * `require()` calls still match the manifest - order matters here (e.g. `flot.stack` vs
 * `flot.categories`, `leaflet.draw.negate` vs `leaflet.draw.polygonSpatial`).
 */

const fs = require('fs');
const path = require('path');
const { execFileSync } = require('child_process');
const parser = require('@babel/parser');

const SIDE_EFFECT_BANNER = '// Loaded for side effects only - do not remove.';

class CodemodError extends Error {}

function stripLoaderPrefix(id) {
    if(id.startsWith('text!')) {
        return id.slice('text!'.length);
    }

    if(id.startsWith('i18n!')) {
        return id.slice('i18n!'.length);
    }

    const bangIndex = id.indexOf('!');

    if(bangIndex !== -1) {
        throw new CodemodError(`unrecognised loader-plugin prefix in id "${id}"`);
    }

    return id;
}

// Converts a static/js-relative file path (e.g. "find/app/foo.js") to the module id other
// files use to require() it (e.g. "find/app/foo").
function toModuleId(relPathFromStaticJs) {
    return relPathFromStaticJs.replace(/\.js$/, '').split(path.sep).join('/');
}

// Dedents every line of `text` by up to `n` leading spaces (fewer if a line has less
// indentation than that - e.g. blank lines).
function dedent(text, n) {
    return text
        .split('\n')
        .map((line) => {
            let strip = 0;

            while(strip < n && line[strip] === ' ') {
                strip++;
            }

            return line.slice(strip);
        })
        .join('\n');
}

// Walks a factory function's own top-level statements (not descending into nested plain
// `function`s, which get their own `arguments`/scope, but DOES descend into arrow
// functions, object/array literals, etc., since `arguments` inside an arrow function
// refers to the enclosing function's) looking for `arguments` references and non-final
// top-level `return`s. `topLevelStatements` is the list of statements directly inside the
// factory's block body (used to know which `return`, if any, is the final statement).
function checkFactoryBody(bodyNode, topLevelStatements) {
    const finalStatement = topLevelStatements[topLevelStatements.length - 1];

    function walk(node, insideNestedFunction) {
        if(node == null || typeof node.type !== 'string') {
            return;
        }

        if(node.type === 'Identifier' && node.name === 'arguments' && !insideNestedFunction) {
            throw new CodemodError('factory references `arguments` directly - refusing to guess');
        }

        if(node.type === 'ReturnStatement' && !insideNestedFunction) {
            const isTopLevelStatement = topLevelStatements.includes(node);

            if(isTopLevelStatement && node !== finalStatement) {
                throw new CodemodError('factory has a non-final top-level `return` - refusing to guess');
            }
        }

        const nextInsideNestedFunction = insideNestedFunction ||
            node.type === 'FunctionExpression' ||
            node.type === 'FunctionDeclaration';

        for(const key of Object.keys(node)) {
            if(key === 'loc' || key === 'start' || key === 'end' || key === 'range' || key === 'leadingComments' || key === 'trailingComments' || key === 'extra') {
                continue;
            }

            const value = node[key];

            if(Array.isArray(value)) {
                value.forEach((child) => walk(child, nextInsideNestedFunction));
            } else if(value && typeof value.type === 'string') {
                walk(value, nextInsideNestedFunction);
            }
        }
    }

    walk(bodyNode, false);
}

// Parses `source` and returns { header, trailer, defineCall } for the single top-level
// `define(...)` ExpressionStatement, or throws if there isn't exactly one.
function findDefineCall(source) {
    const ast = parser.parse(source, { sourceType: 'script', errorRecovery: false });
    const topLevel = ast.program.body;

    const defineStatements = topLevel.filter((node) =>
        node.type === 'ExpressionStatement' &&
        node.expression.type === 'CallExpression' &&
        node.expression.callee.type === 'Identifier' &&
        node.expression.callee.name === 'define'
    );

    if(defineStatements.length !== 1) {
        throw new CodemodError(`expected exactly one top-level define(), found ${defineStatements.length}`);
    }

    const defineStatement = defineStatements[0];

    return {
        header: source.slice(0, defineStatement.start),
        trailer: source.slice(defineStatement.end),
        call: defineStatement.expression
    };
}

// Extracts (param name, dep id) pairs and the factory's rendered CJS body for a
// `function(params) { ... }` factory (with zero or more `deps`).
function renderFactory(source, deps, factory) {
    if(factory.type !== 'FunctionExpression') {
        throw new CodemodError(`expected a function expression factory, got ${factory.type}`);
    }

    const params = factory.params.map((p) => {
        if(p.type !== 'Identifier') {
            throw new CodemodError(`non-identifier factory param (${p.type}) - refusing to guess`);
        }

        return p.name;
    });

    if(params.length > deps.length) {
        throw new CodemodError(`factory has ${params.length} params but only ${deps.length} deps`);
    }

    const boundPairs = params.map((param, i) => ({ param, id: stripLoaderPrefix(deps[i]) }));
    const sideEffectIds = deps.slice(params.length).map(stripLoaderPrefix);

    const block = factory.body;

    if(block.type !== 'BlockStatement') {
        throw new CodemodError(`expected a block statement factory body, got ${block.type}`);
    }

    const statements = block.body;

    checkFactoryBody(block, statements);

    const hasUseStrict = (block.directives || []).some((d) => d.value.value === 'use strict');

    const finalStatement = statements[statements.length - 1];
    const hasFinalReturn = finalStatement != null && finalStatement.type === 'ReturnStatement' && finalStatement.argument != null;

    // The source range to keep from the body: everything between the end of the
    // `'use strict'` directive (if present) and the start of the final `return` (if
    // present), else the whole statement list.
    let bodyStart;

    if(hasUseStrict) {
        const useStrictDirective = block.directives[0];
        bodyStart = useStrictDirective.end;
    } else if(statements.length > 0) {
        bodyStart = statements[0].start;
    } else {
        bodyStart = block.start + 1; // right after the opening brace
    }

    const bodyEnd = hasFinalReturn ? finalStatement.start : block.end - 1; // before closing brace

    let middle = source.slice(bodyStart, bodyEnd);

    // Trim a single leading blank line left over from removing the 'use strict' directive
    // line, and any trailing whitespace-only tail before the (removed) final return / the
    // closing brace.
    middle = middle.replace(/^(?:[ \t]*\n)+/, '').replace(/\s*$/, '');

    const dedented = dedent(middle, 4);

    const requireLines = boundPairs.map(({ param, id }) => `const ${param} = require('${id}');`);

    const sideEffectLines = sideEffectIds.length === 0 ? [] : [
        '',
        SIDE_EFFECT_BANNER,
        ...sideEffectIds.map((id) => `require('${id}');`)
    ];

    const parts = [...requireLines, ...sideEffectLines];

    if(parts.length > 0) {
        parts.push('');
    }

    if(dedented.length > 0) {
        parts.push(dedented);
        parts.push('');
    }

    if(hasFinalReturn) {
        const arg = finalStatement.argument;
        parts.push(`module.exports = ${source.slice(arg.start, arg.end)};`);
    }

    return { text: parts.join('\n'), sideEffectIds };
}

// Converts one file's source. Returns { output, sideEffectIds } where sideEffectIds is
// null if the file had no trailing side-effect deps.
function convert(source) {
    const { header, trailer, call } = findDefineCall(source);
    const args = call.arguments;

    let bodyText;
    let sideEffectIds = [];

    if(args.length === 1 && args[0].type === 'ObjectExpression') {
        // define({ ... })
        const obj = args[0];
        bodyText = `module.exports = ${source.slice(obj.start, obj.end)};\n`;
    } else if(args.length === 1 && args[0].type === 'FunctionExpression') {
        // define(function(deps-less factory) { ... })
        const rendered = renderFactory(source, [], args[0]);
        bodyText = rendered.text + '\n';
        sideEffectIds = rendered.sideEffectIds;
    } else if(args.length === 2 && args[0].type === 'ArrayExpression' && args[1].type === 'FunctionExpression') {
        // define([ids], function(params) { ... })
        const depNodes = args[0].elements;

        const deps = depNodes.map((el) => {
            if(el == null || el.type !== 'StringLiteral') {
                throw new CodemodError('expected all deps to be string literals');
            }

            return el.value;
        });

        const rendered = renderFactory(source, deps, args[1]);
        bodyText = rendered.text + '\n';
        sideEffectIds = rendered.sideEffectIds;
    } else {
        throw new CodemodError(`unrecognised define() call shape (${args.length} args)`);
    }

    const output = header + bodyText + trailer;

    return { output, sideEffectIds };
}

function convertFile(absPath, staticJsRoot, manifest) {
    const source = fs.readFileSync(absPath, 'utf8');
    const { output, sideEffectIds } = convert(source);

    fs.writeFileSync(absPath, output);

    // Syntax gate.
    execFileSync(process.execPath, ['--check', absPath], { stdio: 'inherit' });

    if(sideEffectIds.length > 0) {
        const relFromStaticJs = path.relative(staticJsRoot, absPath);
        manifest[toModuleId(relFromStaticJs)] = sideEffectIds;
    }
}

module.exports = { convert, convertFile, toModuleId, CodemodError };

if(require.main === module) {
    const args = process.argv.slice(2);

    if(args.length === 0) {
        console.error('usage: amd-to-cjs.js <file-or-dir> [...] [--manifest=<path>]');
        process.exit(1);
    }

    let manifestPath = path.resolve(__dirname, '..', 'side-effect-requires.json');
    const targets = [];

    for(const arg of args) {
        if(arg.startsWith('--manifest=')) {
            manifestPath = path.resolve(arg.slice('--manifest='.length));
        } else {
            targets.push(arg);
        }
    }

    const manifest = fs.existsSync(manifestPath) ? JSON.parse(fs.readFileSync(manifestPath, 'utf8')) : {};

    function collectFiles(target) {
        const stat = fs.statSync(target);

        if(stat.isDirectory()) {
            return fs.readdirSync(target).flatMap((entry) => collectFiles(path.join(target, entry)));
        }

        if(target.endsWith('.js')) {
            return [target];
        }

        return [];
    }

    const files = targets.flatMap((t) => collectFiles(path.resolve(t)));

    // Every target file lives under some module's src/main/public/static/js tree - use
    // that as the module-id root regardless of which module (core/idol) it's under.
    function staticJsRootFor(absPath) {
        const marker = `${path.sep}static${path.sep}js${path.sep}`;
        const idx = absPath.indexOf(marker);

        if(idx === -1) {
            throw new Error(`${absPath} is not under a .../static/js/ tree`);
        }

        return absPath.slice(0, idx + marker.length - 1);
    }

    let failures = 0;

    for(const file of files) {
        try {
            convertFile(file, staticJsRootFor(file), manifest);
            console.log(`OK    ${file}`);
        } catch(e) {
            failures++;
            console.error(`FAIL  ${file}: ${e.message}`);
        }
    }

    fs.writeFileSync(manifestPath, JSON.stringify(manifest, null, 4) + '\n');

    if(failures > 0) {
        console.error(`\n${failures} file(s) failed - see above. Manifest written for successful files only.`);
        process.exit(1);
    }
}
