/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    'backbone',
    'underscore',
    'find/app/util/test-browser',
    'find/app/model/window-scroll-model',
    'find/app/model/saved-searches/saved-query-collection',
    'find/app/util/parse-url',
    './model-registry',
    'find/app/navigation',
    'find/app/configuration',
    'find/app/pages',
    'find/app/util/logout',
    'find/app/vent',
    'find/app/router',
    'js-whatever/js/escape-regex',
    'text!find/templates/app/app.html'
], function($, Backbone, _, testBrowser, WindowScrollModel, SavedQueryCollection, parseUrl, ModelRegistry,
            Navigation, configuration, Pages, logout, vent, router, escapeRegex, template) {

    function removeTrailingSlash(string) {
        return string.replace(/\/$/, '');
    }

    return Backbone.View.extend({
        el: '.page',
        template: _.template(template),

        // Can be overridden
        defaultPage: null,
        Navigation: Navigation,
        IndexesCollection: null,

        // Abstract
        getPageData: null,

        events: {
            'click .navigation-logout': function() {
                logout('logout');
            },
            'click a[href]': function(event) {
                // If not left click (event.which === 1) without the control key, continue with full page redirect
                if (event.which === 1 && !(event.ctrlKey || event.metaKey)) {
                    var href = $(event.currentTarget).prop('href');

                    // If not an internal route, continue with full page redirect
                    if (this.internalHrefRegexp.test(href)) {
                        event.preventDefault();
                        var route = href.replace(this.internalHrefRegexp, '');
                        vent.navigate(route);
                    }
                }
            }
        },

        initialize: function() {
            $.ajaxSetup({cache: false});

            // disable Datatables alerting behaviour
            if ($.fn.dataTableExt) {
                $.fn.dataTableExt.sErrMode = 'throw';
            }

            const applicationPath = configuration().applicationPath;
            this.internalHrefRegexp = new RegExp('^' + escapeRegex(removeTrailingSlash(document.body.baseURI) + applicationPath));

            testBrowser().done(function() {
                var modelRegistry = new ModelRegistry(this.getModelData());
                var pageData = this.getPageData();

                this.pages = new Pages({
                    defaultPage: this.defaultPage,
                    modelRegistry: modelRegistry,
                    pageData: pageData,
                    router: router
                });

                this.navigation = new this.Navigation({
                    pageData: pageData,
                    router: router
                });

                this.render();

                var matchedRoute = Backbone.history.start({
                    pushState: true,
                    // Application path must have a leading slash
                    root: removeTrailingSlash(parseUrl(document.body.baseURI).pathname) + applicationPath
                });

                if (!matchedRoute) {
                    vent.navigate(configuration().hasBiRole ? 'find/search/query/*' : 'find/search/splash');
                }
            }.bind(this));
        },

        render: function() {
            this.$el.html(this.template({
                username: configuration().username
            }));

            this.pages.setElement('.find-pages-container').render();

            this.navigation.render();

            this.$('.header').prepend(this.navigation.el);
        },

        // Can be overridden
        getModelData: function() {
            var modelData = {
                indexesCollection: {
                    Constructor: this.IndexesCollection
                },
                windowScrollModel: {
                    Constructor: WindowScrollModel,
                    fetch: false
                }
            };

            if (configuration().hasBiRole) {
                modelData.savedQueryCollection = {
                    Constructor: SavedQueryCollection,
                    fetchOptions: {remove: false}
                };
            }
            else {
                modelData.savedQueryCollection = {
                    Constructor: Backbone.Collection,
                    fetch: false
                };
            }

            return modelData;
        }
    });

});
