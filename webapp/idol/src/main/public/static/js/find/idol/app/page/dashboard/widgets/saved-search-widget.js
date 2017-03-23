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

        // Called once after the first saved search promise resolves. Calls
        // through to getData; if postInitialize() returns a promise,
        // this and any future calls to getData() will be contingent on its resolution.
        postInitialize: _.noop,

        // Called during every update. Must return a promise.
        getData: _.noop,

        initialize: function(options) {
            UpdatingWidget.prototype.initialize.apply(this, arguments);

            this.savedSearchRoute = '/search/tab/' +
                options.datasource.config.type +
                ':' +
                options.datasource.config.id +
                (this.viewType ? '/view/' + this.viewType : '');

            this.savedSearchModel = new DashboardSearchModel({
                id: options.datasource.config.id,
                type: options.datasource.config.type
            });
        },

        // Called by the widget's update() method, which in turn is called by the dashboard-page's update().
        // The argument callback hides the loading spinner -- every execution path that does not call it will
        // result in the loading spinner not disappearing after the update.
        doUpdate: function(done) {
            // TODO does not fetch saved search again unless widget is fully initialised. This fails to cover the edge case in which:
            // 1. the widget loads, a saved search is fetched, and its promise resolves
            // 2. then the postInitialize() promise takes a long time to resolve
            // 3. then an update happens. A new saved search is not fetched
            // 4. then the postInitialize() promise resolves and this.getData() is called using the 'old' saved search.
            // If the saved search was modified between 2. and 4., the first update will happen using stale data.
            if(this.initialiseWidgetPromise && this.initialiseWidgetPromise.state() !== 'resolved') {
                done();
            } else {
                const savedSearchPromise = this.savedSearchModel.fetch()
                    .done(function() {
                        this.queryModel = this.savedSearchModel.toQueryModel(IdolIndexesCollection, false);
                    }.bind(this));

                let promise;

                if(this.initialiseWidgetPromise) {
                    promise = $.when(savedSearchPromise, this.initialiseWidgetPromise);
                } else {
                    promise = savedSearchPromise
                        .then(function() {// TODO handle failure
                            // postInitialize may not return a promise
                            return $.when(this.postInitialize());// TODO handle failure
                        }.bind(this));

                    this.initialiseWidgetPromise = promise;
                }

                promise
                    .then(function() {
                        this.updatePromise = this.getData();// TODO handle failure
                        return this.updatePromise;
                    }.bind(this))
                    .done(done);
            }
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
