'use strict';

// Replaces grunt-peg (dead upstream, only ever installed from a GitHub fork). Uses the
// pegjs *API*, not the CLI, because the CLI's `-o` does not create parent directories and
// `mkdir -p` isn't portable to cmd.exe.
//
// format: 'umd' (not 'commonjs') so the generated parsers load under both RequireJS and
// webpack - this keeps the require.js rollback viable through the webpack migration's
// intermediate phases. trackLineAndColumn is a PEG.js 0.8 option that 0.10 silently ignores,
// so it is dropped rather than carried forward as dead configuration.

const fs = require('fs');
const path = require('path');
const peg = require('pegjs');

const targets = [
    {
        src: path.resolve(__dirname, '../frontend/node_modules/hp-autonomy-fieldtext-js/src/js/field-text.pegjs'),
        dest: path.resolve(__dirname, '../target/classes/static/js/pegjs/fieldtext/parser.js')
    },
    {
        src: path.resolve(__dirname, '../src/main/public/static/js/find/app/util/geoindex/idol-wkt.pegjs'),
        dest: path.resolve(__dirname, '../target/classes/static/js/pegjs/idol-wkt/parser.js')
    }
];

targets.forEach(({src, dest}) => {
    const grammar = fs.readFileSync(src, 'utf8');

    const source = peg.generate(grammar, {
        output: 'source',
        format: 'umd'
    });

    fs.mkdirSync(path.dirname(dest), {recursive: true});
    fs.writeFileSync(dest, source);

    process.stdout.write(`Generated ${path.relative(process.cwd(), dest)}\n`);
});
