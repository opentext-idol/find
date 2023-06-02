/*
 * Copyright 2016 Open Text.
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
