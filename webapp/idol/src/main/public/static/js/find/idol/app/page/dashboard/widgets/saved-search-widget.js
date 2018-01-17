/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    './updating-widget',
    'find/idol/app/model/idol-indexes-collection',
    'find/app/configuration',
    'find/app/model/saved-searches/saved-search-model',
    'find/app/vent',
    'text!find/idol/templates/page/dashboards/saved-search-widget-error.html',
    'i18n!find/nls/bundle'
], function(_, $, UpdatingWidget, IdolIndexesCollection, configuration, SavedSearchModel, vent, errorTemplate, i18n) {
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

    function toggleEmptyMessage(isEmpty) {
        this.$empty.toggleClass('hide', !isEmpty);
    }

    function toggleErrorMessage(msg) {
        this.$error.toggleClass('hide', !this.hasErrorState);

        this.$error.html(this.hasErrorState
            ? errorTemplateFn({
                i18n: i18n,
                errorMessage: msg
                    ? _.escape(msg)
                    : ''
            })
            : '');
    }

    return UpdatingWidget.extend({
        viewType: '', // determines which results view is loaded when navigating to the saved search on click
        clickable: true,

        // May be overridden. Return true if the query returned no data to display, false otherwise.
        // Only called if data fetch was successful.
        isEmpty: _.constant(false),

        // Reports if the widget is in an error state
        hasError: function() {
            return this.hasErrorState === true;
        },

        // Update visualizer if necessary _before_ $content is shown toggled into view
        updateVisualizer: _.noop,

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

            this.savedSearchModel = new DashboardSearchModel({
                id: options.datasource.config.id,
                type: options.datasource.config.type
            });

            this.savedQueryCollection = options.savedQueryCollection;
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
                        : (this.widgetInitializePromise = $.when(this.postInitialize()));
                }.bind(this))
                .then(function() {
                    this.hasInitialized = true;
                    return this.updatePromise = this.getData();
                }.bind(this))
                // Call done() before other callbacks to make sure this.$content correctly shown/hidden
                .always(done)
                .done(function() {
                    const empty = this.isEmpty();
                    toggleEmptyMessage.call(this, empty);
                    this.hasErrorState = false;
                    toggleErrorMessage.call(this);
                    this.toggleContent(!empty);
                    if(!empty) {
                        this.updateVisualizer();
                    }
                }.bind(this))
                .fail(function(error) {
                    this.queryModel = null;
                    this.toggleContent(false);
                    toggleEmptyMessage.call(this, false);
                    this.hasErrorState = true;
                    toggleErrorMessage.call(this,
                        error.statusText === 'abort'
                            ? i18n['dashboards.widget.dataError.tooSlow']
                            : (error.responseJSON
                                ? error.responseJSON.message
                                : ''));
                }.bind(this));
        },

        onClick: function() {
            const config = configuration();

            const attribs = this.savedSearchModel.attributes;

            const viewRoute = this.viewType ? '/view/' + this.viewType : '';

            const shared = attribs.canEdit && config.username !== attribs.user.username;

            const savedSearchRoute = '/search/tab/' +
                (shared ? SavedSearchModel.Type.SHARED_QUERY : attribs.type) +
                ':' +
                attribs.id +
                viewRoute;

            if (config && config.uiCustomization && config.uiCustomization.openSharedDashboardQueryAsNewSearch
                && config.username !== attribs.user.username && attribs.type === SavedSearchModel.Type.QUERY) {
                // Create a new search
                const newSearch = new SavedSearchModel(_.defaults({
                    id: null,
                    title: i18n['search.newSearch'],
                    type: SavedSearchModel.Type.QUERY
                }, attribs));

                this.savedQueryCollection.add(newSearch);
                const route = '/search/tab/QUERY:' + newSearch.cid + viewRoute;
                vent.navigate(route + this.getSavedSearchRouterParameters());
            }
            else {
                vent.navigate(savedSearchRoute + this.getSavedSearchRouterParameters());
            }
        },

        getSavedSearchRouterParameters: function() {
            return '';
        },

        onCancelled: function() {
            if(this.savedSearchPromise && this.savedSearchPromise.abort) {
                this.savedSearchPromise.abort();
            }
            if(this.widgetInitializePromise && this.widgetInitializePromise.abort) {
                this.widgetInitializePromise.abort();
            }
            if(this.updatePromise && this.updatePromise.abort) {
                this.updatePromise.abort();
            }
        }
    });
});
