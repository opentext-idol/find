/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore'
], function(_) {
    /**
     * Replace the placeholder highlighting tags and instances of the given entity titles (if provided) with anchor tags in the summary.
     */
    return function addLinksToSummary(summary) {
        // Find highlighted query terms
        var queryTextRegex = /<HavenSearch-QueryText-Placeholder>(.*?)<\/HavenSearch-QueryText-Placeholder>/g;
        var queryText = [];
        var resultsArray;

        while ((resultsArray = queryTextRegex.exec(summary)) !== null) {
            queryText.push(resultsArray[1]);
        }

        // Protect us from XSS (but leave injected highlight tags alone)
        var otherText = summary.split(/<HavenSearch-QueryText-Placeholder>.*?<\/HavenSearch-QueryText-Placeholder>/);
        var escapedSummaryElements = [];
        escapedSummaryElements.push(_.escape(otherText[0]));

        for (var i = 0; i < queryText.length; i++) {
            escapedSummaryElements.push('<span class="search-text">' + _.escape(queryText[i]) + '</span>');
            escapedSummaryElements.push(_.escape(otherText[i + 1]));
        }

        return escapedSummaryElements.join('');
    };

});
