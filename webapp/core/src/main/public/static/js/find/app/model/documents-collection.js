/*
 * (c) Copyright 2014-2017 Micro Focus or one of its affiliates.
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
    'underscore',
    'find/app/model/find-base-collection',
    'find/app/model/document-model'
], function(_, FindBaseCollection, DocumentModel) {
    'use strict';

    return FindBaseCollection.extend({
        model: DocumentModel,
        url: 'api/public/search/query-text-index/results',

        autoCorrection: null,
        totalResults: null,
        warnings: null,

        fetch: function(options) {
            this.autoCorrection = null;
            this.totalResults = null;
            this.warnings = null;

            const originalErrorHandler = options.error || _.noop;

            const errorHandler = function(collection, errorResponse) {
                if(errorResponse.responseJSON) {
                    this.autoCorrection = errorResponse.responseJSON.autoCorrection;
                }

                originalErrorHandler.apply(options, arguments);
            }.bind(this);

            return FindBaseCollection.prototype.fetch.call(this, _.extend(options, {error: errorHandler}));
        },

        parse: function(response) {
            this.autoCorrection = response.autoCorrection;
            this.totalResults = response.totalResults;
            this.warnings = response.warnings;

            return response.documents;
        },

        getAutoCorrection: function() {
            return this.autoCorrection;
        }
    });
});
