/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'i18n!find/nls/bundle',
    'i18n!find/nls/indexes',
    'parametric-refinement/prettify-field-name',
    'underscore'
], function(i18n, indexesI18n, prettifyFieldName, _) {

    var DATE_FORMAT = 'YYYY/MM/DD HH:mm';

    function dateRestriction(title, date) {
        return date ? {title: title, content: date.format(DATE_FORMAT)} : null;
    }

    function relatedConcepts(concepts) {
        return concepts.length ? {title: i18n['search.snapshot.restrictions.relatedConcepts'], content: concepts.join(', ')} : null;
    }

    /**
     * Target attributes and an attributes processor for the "Query Restrictions" {@link DataPanelView}.
     */
    return {
        targetAttributes: [
            'queryText',
            'relatedConcepts',
            'indexes',
            'parametricValues',
            'minDate',
            'maxDate'
        ],

        /**
         * @type {DataPanelAttributesProcessor}
         */
        processAttributes: function(attributes) {
            var indexesContent = _.pluck(attributes.indexes, 'name').join(', ');

            var parametricRestrictions = _.map(_.groupBy(attributes.parametricValues, 'field'), function(items, field) {
                return {
                    title: prettifyFieldName(field),
                    content: _.pluck(items, 'value').join(', ')
                };
            });

            return [
                {
                    title: i18n['search.snapshot.restrictions.queryText'],
                    content: attributes.queryText
                },
                relatedConcepts(attributes.relatedConcepts),
                {
                    title: indexesI18n['search.indexes'],
                    content: indexesContent
                },
                dateRestriction(i18n['search.snapshot.restrictions.minDate'], attributes.minDate),
                dateRestriction(i18n['search.snapshot.restrictions.maxDate'], attributes.maxDate)
            ].concat(parametricRestrictions);
        }
    };

});
