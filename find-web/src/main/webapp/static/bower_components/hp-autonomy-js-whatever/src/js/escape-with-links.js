/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module js-whatever/js/escape-with-links
 */
define([
    'js-whatever/js/regex-replace',
    '../../../underscore/underscore'
], function(regexReplace, _){
    /**
     * @alias module:js-whatever/js/escape-with-links
     * @desc HTML escapes a string, replacing any hyperlinks found with &lt;a&gt; tags
     * @param {string} text The text to escape
     * @param {boolean} catchSpaces Whether spaces should be treated as part of the link (true) or as the end of the
     * link (false)
     * @param {string} target The 'target' attribute for the &lt;a&gt; tag of any links found, e.g. '_blank'
     * @param {string} className The class to put on the &lt;a&gt; tag of any links found
     * @returns {string} The original string, HTML escaped and with links wrapped in &lt;a&gt; tags
     */
    var escapeWithLinks = function escapeWithLinks(text, catchSpaces, target, className) {
        var regex = /\(?https?:\/\/[-A-Za-z0-9+&@#/%?=~_()|!:,.;']*[-A-Za-z0-9+&@#/%=~_()|]/ig;
        var regexSpaces = /\(?https?:\/\/[-A-Za-z0-9+&@#/%?=~_()|!:,.;'\s]*[-A-Za-z0-9+&@#/%=~_()|]/ig;

        if (_.isUndefined(target) || _.isNull(target)) {
            target = '_blank';
        }

        if (_.isUndefined(className) || _.isNull(className)) {
            className = 'autoLink';
        }

        var reg = catchSpaces === true ? regexSpaces : regex;

        if (!text) {
            return text;
        }

        return regexReplace(reg, String(text), function(url) {
            var wrapLink = false;

            // Check for links wrapped in brackets
            if (url[0] === '(' && url[url.length - 1] === ')') {
                url = url.substring(1, url.length - 1);
                wrapLink = true
            }

            var escapedURL = _.escape(url);
            var link = '<a class="' + className + '" target="' + target + '" href="' + escapedURL + '">' + escapedURL + '</a>';

            if (wrapLink) {
                link = '(' + link + ')'
            }

            return link;
        }, _.escape);
    };

    return escapeWithLinks;
});