/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

define([
    'underscore'
], function(_) {

    /**
     * Replace the placeholder highlighting tags and instances of the given entity titles (if provided) with anchor tags
     * in the summary.
     * @param {string} summary May be null
     * @return {string}
     */
    function addLinksToSummary(summary) {
        if (!summary) {
            return '';
        }

        // Find highlighted query terms
        var queryTextRegex = /<HavenSearch-QueryText-Placeholder>([\s\S]*?)<\/HavenSearch-QueryText-Placeholder>/g;
        var queryText = [];
        var resultsArray;

        while ((resultsArray = queryTextRegex.exec(summary)) !== null) {
            queryText.push(resultsArray[1]);
        }

        // Protect us from XSS (but leave injected highlight tags alone)
        var otherText = summary.split(/<HavenSearch-QueryText-Placeholder>[\s\S]*?<\/HavenSearch-QueryText-Placeholder>/);
        var escapedSummaryElements = [];
        escapedSummaryElements.push(_.escape(otherText[0]));

        for (var i = 0; i < queryText.length; i++) {
            escapedSummaryElements.push('<span class="search-text">' + _.escape(queryText[i]) + '</span>');
            escapedSummaryElements.push(_.escape(otherText[i + 1]));
        }

        return escapedSummaryElements.join('');
    }

    return addLinksToSummary;

});
