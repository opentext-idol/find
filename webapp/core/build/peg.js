'use strict';

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
        format: 'umd' // support both RequireJS and Webpack
    });

    fs.mkdirSync(path.dirname(dest), {recursive: true});
    fs.writeFileSync(dest, source);

    process.stdout.write(`Generated ${path.relative(process.cwd(), dest)}\n`);
});
