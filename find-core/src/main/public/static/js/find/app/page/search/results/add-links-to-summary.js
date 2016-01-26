/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'js-whatever/js/escape-regex',
    'text!find/templates/app/page/search/results/entity-label.html'
], function(_, escapeRegex, entityTemplate) {

    /** Whitespace OR character in set bounded by [] */
    var BOUNDARY_CHARACTERS = '\\s|[,.-:;?\'"!\\(\\)\\[\\]{}]';

    /** Start of input OR boundary chars */
    var START_REGEX = '(^|' + BOUNDARY_CHARACTERS + ')';

    /** End of input OR boundary chars */
    var END_REGEX = '($|' + BOUNDARY_CHARACTERS + ')';

    var entityTemplateFunction = _.template(entityTemplate);

    /**
     * Finds a string that's bounded by [some regex stuff] and replaces it with something else.
     * Used as part 1 of highlighting text in result summaries.
     * @param text The text to search in
     * @param textToFind The text to search for
     * @param replacement What to replace textToFind with
     * @returns {string|XML|void} `text`, but with replacements made
     */
    function replaceBoundedText(text, textToFind, replacement) {
        return text.replace(new RegExp(START_REGEX + escapeRegex(textToFind) + END_REGEX, 'gi'), '$1' + replacement + '$2');
    }

    /**
     * @typedef {Object} EntityTemplateOptions
     * @property elementType {string} The html element type the text should be in
     * @property replacement {string} The text of the element
     * @property elementClasses {string} The classes to add to the html element defined in elementType
     */
    /**
     * Finds a string and replaces it with an HTML label.
     * Used as part 2 of highlighting text in results summaries.
     * @param {string} text  The text to search in
     * @param {string} textToFind  The text to replace with a label
     * @param {EntityTemplateOptions} templateOptions A hash of options to configure the template
     * @returns {string|XML|*}  `text`, but with replacements made
     */
    function replaceTextWithLabel(text, textToFind, templateOptions) {
        var label = entityTemplateFunction(templateOptions);
        return text.replace(new RegExp(START_REGEX + textToFind + END_REGEX, 'g'), '$1' + label + '$2');
    }

    /**
     * Replace the placeholder highlighting tags and instances of the given entity titles with anchor tags in the summary.
     */
    return function addLinksToSummary(entityCollection, summary) {
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

        var escapedSummary = escapedSummaryElements.join('');

        // Create an array of the entity titles, longest first
        var entities = entityCollection.map(function(entity) {
            return {
                text: entity.get('text'),
                id: _.uniqueId('Find-IOD-Entity-Placeholder')
            };
        }).sort(function(a, b) {
            return b.text.length - a.text.length;
        });

        // Loop through entities, replacing each with a unique id to prevent later replaces finding what we've
        // changed here and messing things up badly
        _.each(entities, function(entity) {
            escapedSummary = replaceBoundedText(escapedSummary, entity.text, entity.id);
        });

        // Loop through entities again, replacing text with labels
        _.each(entities, function(entity) {
            escapedSummary = replaceTextWithLabel(escapedSummary, entity.id, {
                elementType: 'a',
                replacement: entity.text,
                elementClasses: 'entity-text entity-label label clickable'
            });
        });

        return escapedSummary;
    };

});
