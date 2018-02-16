/*
 * Copyright 2014-2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    'backbone',
    'dropzone',
    'find/app/util/test-browser',
    'find/app/model/window-scroll-model',
    'find/app/model/saved-searches/saved-query-collection',
    'find/app/model/saved-searches/shared-saved-query-collection',
    'find/app/util/parse-url',
    './model-registry',
    'find/app/navigation',
    'find/app/configuration',
    'find/app/metrics',
    'find/app/pages',
    'find/app/util/logout',
    'find/app/vent',
    'find/app/router',
    'find/app/util/conversation',
    'js-whatever/js/escape-regex',
    'text!find/templates/app/app.html'
], function(_, $, Backbone, Dropzone, testBrowser, WindowScrollModel, SavedQueryCollection, SharedSavedQueryCollection, parseUrl, ModelRegistry,
            Navigation, configuration, metrics, Pages, logout, vent, router, conversation, escapeRegex, template) {
    'use strict';

    function removeTrailingSlash(string) {
        return string.replace(/\/$/, '');
    }

    /**
     * Determine the current document's base URI.
     * @return {string} A fully qualified URI
     */
    function determineBaseURI() {
        return document.body.baseURI
            ? document.body.baseURI
            // IE11 does not have Node.baseURI so parse the <base> element's href directly
            : $('base').prop('href');
    }

    return Backbone.View.extend({
        el: '.page',
        template: _.template(template),

        // Can be overridden
        defaultPage: null,
        Navigation: Navigation,
        IndexesCollection: null,

        // Abstract
        ajaxErrorHandler: null,
        getPageData: null,

        events: {
            'click .navigation-logout': function() {
                logout('logout');
            },
            'click a[href]': function(e) {
                // If not left click (e.which === 1) without the control key, continue with full page redirect
                if(e.which === 1 && !(e.ctrlKey || e.metaKey)) {
                    const href = $(e.currentTarget).prop('href');

                    // If not an internal route, continue with full page redirect
                    if(this.internalHrefRegexp.test(href)) {
                        e.preventDefault();
                        vent.navigate(href.replace(this.internalHrefRegexp, ''));
                    }
                }
            }
        },

        initialize: function() {
            $.ajaxSetup({cache: false});
            $(document).ajaxError(this.ajaxErrorHandler.bind(this));

            // disable auto-discover for dropzones
            Dropzone.autoDiscover = false;

            // disable Datatables alerting behaviour
            if($.fn.dataTableExt) {
                $.fn.dataTableExt.sErrMode = 'throw';
            }

            const baseURI = determineBaseURI();
            const config = configuration();
            const applicationPath = config.applicationPath;
            this.internalHrefRegexp = new RegExp('^' + escapeRegex(removeTrailingSlash(baseURI) + applicationPath));

            this.conversationEnabled = config.conversationEnabled;

            testBrowser().done(function() {
                const modelRegistry = new ModelRegistry(this.getModelData());
                const pageData = this.getPageData();

                this.pages = new Pages({
                    configuration: config,
                    defaultPage: this.defaultPage,
                    modelRegistry: modelRegistry,
                    pageData: pageData,
                    router: router
                });

                this.navigation = new this.Navigation({
                    pageData: pageData,
                    router: router,
                    sidebarModel: modelRegistry.get('sidebarModel')
                });

                this.render();

                let matchedRoute = Backbone.history.start({
                    pushState: true,
                    // Application path must have a leading slash
                    root: removeTrailingSlash(parseUrl(baseURI).pathname) + applicationPath
                });

                if(!matchedRoute) {
                    vent.navigate(configuration().hasBiRole
                        ? 'search/query'
                        : 'search/splash');
                }

                metrics.addTimeSincePageLoad('page-responsive-after-reload');
            }.bind(this));
        },

        render: function() {
            this.$el.html(this.template({
                username: configuration().username
            }));

            this.pages.setElement('.find-pages-container').render();

            this.navigation.render();

            this.$('.header').prepend(this.navigation.el);

            if (this.conversationEnabled) {
                conversation(document.body);
            }
        },

        // Can be overridden
        getModelData: function() {
            return {
                indexesCollection: {
                    Constructor: this.IndexesCollection
                },
                sidebarModel: {
                    Constructor: Backbone.Model,
                    fetch: false,
                    attributes: {
                        collapsed: false
                    }
                },
                windowScrollModel: {
                    Constructor: WindowScrollModel,
                    fetch: false
                },
                savedQueryCollection: configuration().hasBiRole
                    ? {
                        Constructor: SavedQueryCollection,
                        fetchOptions: {remove: false, reset: false}
                    }
                    : {
                        Constructor: Backbone.Collection,
                        fetch: false
                    },
                sharedSavedQueryCollection: configuration().hasBiRole
                    ? {
                        Constructor: SharedSavedQueryCollection,
                        fetchOptions: {remove: false, reset: false}
                    }
                    : {
                        Constructor: Backbone.Collection,
                        fetch: false
                    }
            };
        }
    });
});
