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
    'find/app/vent',
    'text!find/idol/templates/page/dashboards/saved-search-widget-error.html',
    'i18n!find/nls/bundle'
], function(_, $, UpdatingWidget, IdolIndexesCollection, SavedSearchModel, vent, errorTemplate, i18n) {
    'use strict';

    const DashboardSearchModel = SavedSearchModel.extend({
        urlRoot: function() {
            return 'api/bi/' +
                (this.get('type') === 'QUERY'
                    ? 'saved-query'
                    : 'saved-snapshot');
        }
    });

    const errorTemplateFn = _.template(errorTemplate);

    function toggleErrorMessage(hasError, msg) {
        this.$error.toggleClass('hide', !hasError);
        this.$content.toggleClass('hide', hasError);

        this.$error.html(hasError
            ? errorTemplateFn({
                i18n: i18n,
                errorMessage: msg
                    ? _.escape(msg)
                    : ''
            })
            : '');
    }

    function getResponseMessage(error) {
        return (error && error.responseJSON)
            ? error.responseJSON.message
            : ''
    }

    return UpdatingWidget.extend({
        viewType: '', // which view to load when navigating to the saved search on click
        clickable: true,

        // Called after the saved search promise resolves. Calls through to getData();
        // if postInitialize() returns a promise, this and any future calls to getData()
        // will be contingent on its resolution.
        // If the promise returned by postInitialize() is rejected, the widget will retry
        // calling it on the next update until the promise resolves successfully. Afterwards,
        // the update cycle will comprise sequential calls to this.savedSearchModel.fetch() and
        // calls to getData(), but not to postInitialize().
        postInitialize: _.noop,

        // Called during every update. Must return a promise.
        getData: _.noop,

        initialize: function(options) {
            UpdatingWidget.prototype.initialize.apply(this, arguments);

            this.savedSearchRoute = '/search/tab/' +
                options.datasource.config.type +
                ':' +
                options.datasource.config.id +
                (this.viewType
                    ? '/view/' + this.viewType
                    : '');

            this.savedSearchModel = new DashboardSearchModel({
                id: options.datasource.config.id,
                type: options.datasource.config.type
            });
        },

        // Called by the widget's update() method, which in turn is called by the dashboard-page's update().
        // The argument callback hides the loading spinner -- every execution path that does not call it will
        // result in the loading spinner not disappearing after the update.
        doUpdate: function(done) {
            this.savedSearchPromise = this.savedSearchModel.fetch()
                .then(function() {
                    this.queryModel = this.savedSearchModel
                        .toQueryModel(IdolIndexesCollection, false);

                    return this.hasInitialized
                        ? $.when()
                        : (this.widgetInitialisePromise = $.when(this.postInitialize()));
                }.bind(this))
                .then(function() {
                    this.hasInitialized = true;
                    this.initialised();
                    return this.updatePromise = this.getData();
                }.bind(this))
                .done(function() {
                    toggleErrorMessage.call(this, false);
                }.bind(this))
                .fail(function(error) {
                    this.queryModel = null;
                    this.initialised();
                    toggleErrorMessage.call(this, true, getResponseMessage(error));
                }.bind(this))
                .always(done);
        },

        onClick: function() {
            vent.navigate(this.savedSearchRoute);
        },

        onCancelled: function() {
            if(this.savedSearchPromise && this.savedSearchPromise.abort) {
                this.savedSearchPromise.abort();
            }
            if(this.widgetInitialisePromise && this.widgetInitialisePromise.abort) {
                this.widgetInitialisePromise.abort();
            }
            if(this.updatePromise && this.updatePromise.abort) {
                this.updatePromise.abort();
            }
        }
    });
});
