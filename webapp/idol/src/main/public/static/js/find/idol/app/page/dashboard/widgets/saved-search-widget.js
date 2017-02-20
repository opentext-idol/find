/*
 * Copyright 2014-2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    './updating-widget',
    'find/app/configuration',
    'find/app/model/documents-collection',
    'find/idol/app/model/idol-indexes-collection',
    'find/app/model/saved-searches/saved-search-model',
    'find/app/vent'
], function($, UpdatingWidget, configuration, DocumentsCollection, IdolIndexesCollection, SavedSearchModel, vent) {
    'use strict';

    const DashboardSearchModel = SavedSearchModel.extend({
        urlRoot: function() {
            return 'api/bi/' + (this.get('type') === 'QUERY' ? 'saved-query': 'saved-snapshot');
        }
    });

    return UpdatingWidget.extend({

        clickable: true,

        initialize: function(options) {
            UpdatingWidget.prototype.initialize.apply(this, arguments);

            this.savedSearchRoute = '/search/tab/' + options.savedSearch.type + ':' + options.savedSearch.id + (this.viewType ? '/view/' + this.viewType : '');

            this.savedSearchModel = new DashboardSearchModel({
                id: options.savedSearch.id,
                type: options.savedSearch.type
            });

            this.savedSearchModel.fetch().done(function() {
                this.queryModel = this.savedSearchModel.toQueryModel(IdolIndexesCollection, false);
                this.postInitialize();
                this.getData();
            }.bind(this));
        },

        postInitialize: $.noop,

        doUpdate: function(done) {
            this.savedSearchModel.fetch().done(function() {
                this.queryModel = this.savedSearchModel.toQueryModel(IdolIndexesCollection, false);
                this.updatePromise = this.getData().done(done);
            }.bind(this));
        },

        onClick: function () {
            vent.navigate(this.savedSearchRoute);
        },

        getData: $.noop,

        onCancelled: function() {
            if (this.updatePromise && this.updatePromise.abort) {
                this.updatePromise.abort();
            }
        }
    });
});