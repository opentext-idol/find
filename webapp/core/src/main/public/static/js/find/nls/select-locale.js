/*
 * Copyright 2026 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

/**
 * @module find/nls/select-locale
 * @desc Reimplements the locale-selection and merge behaviour of requirejs/i18n@2.0.6's
 * load()/addPart()/mixin(), but statically: all locale parts are already loaded (as build-time
 * requires), so this just has to pick the right ones and merge them.
 */
// Mixes properties from source into target, but only fills in properties target does not
// already own - the earlier (more specific) values written during the backwards merge in
// selectLocale always win. Recurses into plain objects so nested string tables merge too.
function mixin(target, source) {
    for(var prop in source) {
        if(!source.hasOwnProperty(prop)) {
            continue;
        }

        if(!target.hasOwnProperty(prop)) {
            target[prop] = source[prop];
        } else if(typeof source[prop] === 'object' && source[prop] !== null) {
            if(!target[prop]) {
                target[prop] = {};
            }

            mixin(target[prop], source[prop]);
        }
    }
}

/**
 * @alias module:find/nls/select-locale
 * @desc Merges the locale bundles declared on a master bundle object, picking the browser's
 * locale at call time. There is no _ -> - normalisation and no build-time lookup of which
 * locales exist beyond the keys of the master object passed in - locales resolve entirely
 * at build time; to add one, create the directory and add it to the master bundle's require
 * list.
 * @param {Object} bundles Map from locale tag (e.g. 'root', 'en-gb') to that locale's already
 * resolved bundle object
 * @returns {Object} The merged bundle for the current browser locale
 */
function selectLocale(bundles) {
    var browserLocale = (
        (typeof navigator !== 'undefined' && navigator.languages && navigator.languages[0]) ||
        (typeof navigator !== 'undefined' && navigator.language) ||
        (typeof navigator !== 'undefined' && navigator.userLanguage) ||
        'root'
    ).toLowerCase();

    var parts = browserLocale.split('-');

    // Always try root first, then progressively longer locale prefixes, keeping only the
    // ones actually declared on the master bundle.
    var needed = [];

    if(bundles.hasOwnProperty('root')) {
        needed.push('root');
    }

    var current = '';

    for(var i = 0; i < parts.length; i++) {
        current += (current ? '-' : '') + parts[i];

        if(bundles.hasOwnProperty(current)) {
            needed.push(current);
        }
    }

    // Merge backwards - the most specific locale part is mixed in first, so it wins over
    // the less specific ones that follow.
    var value = {};

    for(var j = needed.length - 1; j >= 0; j--) {
        mixin(value, bundles[needed[j]]);
    }

    return value;
}

module.exports = selectLocale;

