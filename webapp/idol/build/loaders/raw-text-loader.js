'use strict';

// loader that returns the raw text of a file as a string
module.exports = function rawTextLoader(source) {
    // JSON.stringify leaves U+2028/U+2029 raw - legal in JSON, illegal in pre-ES2019 string
    // literals - so the two replaces below escape them explicitly.
    return 'module.exports = ' + JSON.stringify(source)
        .replace(/\u2028/g, '\\u2028').replace(/\u2029/g, '\\u2029') + ';\n';
};
