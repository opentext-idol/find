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

define(['underscore'], function(_) {
    'use strict';

    return {
        answers: _.constant(true),
        promotions: _.constant(true),
        canEditDocumentSelection: true,

        queryModelAttributes: [
            'indexes',
            'fieldText',
            'minDate',
            'maxDate',
            'minScore',
            'queryText',
            'editingDocumentSelection',
            'fieldTextWithoutDocumentSelection',
            'crosslingualOntology',
            'crosslingualIndex'
        ],

        requestParams: function(queryModel) {
            // show deselected documents while editing document selection
            const fieldTextAttr = queryModel.get('editingDocumentSelection') ?
                'fieldTextWithoutDocumentSelection' :
                'fieldText';

            return {
                indexes: queryModel.get('indexes'),
                field_text: queryModel.get(fieldTextAttr),
                min_date: queryModel.getIsoDate('minDate'),
                max_date: queryModel.getIsoDate('maxDate'),
                min_score: queryModel.get('minScore'),
                summary: 'context',
                text: queryModel.get('queryText'),
                crosslingualOntology: queryModel.get('crosslingualOntology'),
                crosslingualIndex: queryModel.get('crosslingualIndex')
            };
        },

        promotionsRequestParams: function(queryModel){
            const params = this.requestParams(queryModel);
            // promotions ignore document selection editing - always use full fieldtext
            params.field_text = queryModel.get('fieldText');
            delete params.indexes;
            return params;
        },

        validateQuery: function(queryModel) {
            return Boolean(queryModel.get('queryText'));
        },

        waitForIndexes: function(queryModel) {
            return _.isEmpty(queryModel.get('indexes'));
        }
    };

});
