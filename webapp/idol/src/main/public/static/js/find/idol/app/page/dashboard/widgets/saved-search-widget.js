/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    './updating-widget',
    'find/idol/app/model/idol-indexes-collection',
    'find/app/model/saved-searches/saved-search-model',
    'find/app/vent'
], function(_, $, UpdatingWidget, IdolIndexesCollection, SavedSearchModel, vent) {
    'use strict';

    const DashboardSearchModel = SavedSearchModel.extend({
        urlRoot: function() {
            return 'api/bi/' +
                (this.get('type') === 'QUERY'
                    ? 'saved-query'
                    : 'saved-snapshot');
        }
    });

    return UpdatingWidget.extend({
        clickable: true,
        postInitialize: _.noop,
        getData: _.noop,

        initialize: function(options) {
            UpdatingWidget.prototype.initialize.apply(this, arguments);

            this.savedSearchRoute = '/search/tab/' +
                options.savedSearch.type +
                ':' +
                options.savedSearch.id +
                (this.viewType ? '/view/' + this.viewType : '');

            this.savedSearchModel = new DashboardSearchModel({
                id: options.savedSearch.id,
                type: options.savedSearch.type
            });

            this.initialiseWidgetPromise = this.savedSearchModel.fetch()
                .then(function() {// TODO handle failure
                    this.queryModel = this.savedSearchModel.toQueryModel(IdolIndexesCollection, false);
                    return $.when(this.postInitialize());
                }.bind(this));
        },

        doUpdate: function(done) {
            $.when(this.savedSearchModel.fetch(), this.initialiseWidgetPromise)
                .done(function() {// TODO handle failure
                    this.queryModel = this.savedSearchModel.toQueryModel(IdolIndexesCollection, false);
                    this.updatePromise = this.getData()
                        .done(done);// TODO handle failure
                }.bind(this));
        },

        onClick: function() {
            vent.navigate(this.savedSearchRoute);
        },

        onCancelled: function() {
            if(this.updatePromise && this.updatePromise.abort) {
                this.updatePromise.abort();
            }
        }
    });
});
