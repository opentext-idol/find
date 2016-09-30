/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore'
], function(_) {

    // TODO: Replace with window.URL when browser support is better
    /**
     * @typedef {Object} URLComponents
     * @property {string} hash
     * @property {string} host
     * @property {string} hostname
     * @property {string} href
     * @property {string} origin
     * @property {string} pathname
     * @property {number} port
     * @property {string} protocol
     * @property {string} search
     */
    /**
     * Parse the given URL into an object of URL components. If a relative URL is given, the components will be relative
     * to the current document URI.
     * @param {string} url
     * @return {URLComponents}
     */
    function parseUrl(url) {
        const anchor = document.createElement('a');
        anchor.setAttribute('href', url);

        return _.extend(
            {port: parseInt(anchor.port, 10)},
            _.pick(anchor, ['hash', 'host', 'hostname', 'href', 'origin', 'pathname', 'protocol', 'search'])
        );
    }

    return parseUrl;

});
