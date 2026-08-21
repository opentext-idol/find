'use strict';

// `type: 'asset/source'` and `raw-loader` both emit an ES-module default export, so a plain
// CJS `require()` yields `{ default: '<html>' }` - every one of the 171 `_.template(template)`
// call sites would get an object instead of a string. This emits a real CJS export instead.
module.exports = function rawTextLoader(source) {
    // JSON.stringify leaves U+2028/U+2029 raw - legal in JSON, illegal in pre-ES2019 string
    // literals - so the two replaces below escape them explicitly.
    return 'module.exports = ' + JSON.stringify(source)
        .replace(/\u2028/g, '\\u2028').replace(/\u2029/g, '\\u2029') + ';\n';
};
