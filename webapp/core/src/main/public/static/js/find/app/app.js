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
    'text!find/templates/app/app.html',
    'metisMenu'
], function($, Backbone, _, testBrowser, WindowScrollModel, SavedQueryCollection, parseUrl, ModelRegistry,
            Navigation, configuration, Pages, logout, vent, router, escapeRegex, template) {
    "use strict";
    function removeTrailingSlash(string) {
        return string.replace(/\/$/, '');
    }

    /**
     * Determine the current document's base URI.
     * @return {string} A fully qualified URI
     */
    function determineBaseURI() {
        if (document.body.baseURI) {
            return document.body.baseURI;
        } else {
            // IE11 does not have Node.baseURI so parse the <base> element's href directly
            return $('base').prop('href');
        }
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
            },
            'click .navbar-static-side > ul > li': function (event) {
                $('.navbar-static-side').find('li.active').removeClass('active');
                $(event.target).closest('li').addClass('active');

                if ($(event.target).closest('ul').hasClass('nav-second-level')) {
                    $(event.target).closest('ul').parent().addClass('active');
                }
            },
            'hidden.metisMenu': function(event) {
                $(event.target).parent().removeClass('active');
            },
            'click .side-nav-menu-button': function(event) {
                event.preventDefault();
                this.sideBarModel.set('collapsed', true);
            }
        },

        initialize: function() {
            $.ajaxSetup({cache: false});

            // disable Datatables alerting behaviour
            if ($.fn.dataTableExt) {
                $.fn.dataTableExt.sErrMode = 'throw';
            }

            const baseURI = determineBaseURI();
            const config = configuration();
            const applicationPath = config.applicationPath;
            this.sideBarModel = new Backbone.Model({collapsed: false});
            this.internalHrefRegexp = new RegExp('^' + escapeRegex(removeTrailingSlash(baseURI) + applicationPath));

            testBrowser().done(function() {
                var modelRegistry = new ModelRegistry(this.getModelData());
                var pageData = this.getPageData();

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
                    sideBarModel: this.sideBarModel
                });

                this.render();

                var matchedRoute = Backbone.history.start({
                    pushState: true,
                    // Application path must have a leading slash
                    root: removeTrailingSlash(parseUrl(baseURI).pathname) + applicationPath
                });

                if (!matchedRoute) {
                    vent.navigate(configuration().hasBiRole ? 'search/query/*' : 'search/splash');
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
            this.$('.side-menu').metisMenu();
            this.listenTo(this.sideBarModel, 'change:collapsed', function(model) {
                this.toggleSideBar(model.get('collapsed'));
            });

            this.listenTo(vent, 'vent:resize', function () {
                if ($(window).width() <= 785 && !this.sideBarModel.get('collapsed')) {
                    this.sideBarModel.set('collapsed', true);
                    this.sideBarModel.set('collapsedFromResize', true);
                }
                else if (this.sideBarModel.get('collapsedFromResize')) {
                    this.sideBarModel.set('collapsed', false);
                    this.sideBarModel.set('collapsedFromResize', false);
                }
            });

            this.sideBarModel.set('collapsed', true);
        },

        toggleSideBar: function (collapsed) { // side is for when not collapsed
            $(document.body).toggleClass('mini-navbar', collapsed);
            this.$('.navbar-static-side-container').toggleClass('navbar-static-side-hidden', collapsed);
            this.$('.page-content').toggleClass('page-wrapper-cover', collapsed);
            this.$('.find-logo-small.top-nav').toggleClass('hidden', !collapsed);
            this.$('.find-logo-small.side-nav').toggleClass('hidden', collapsed);
            this.$('.top-nav-menu-button').toggleClass('hidden', !collapsed);
            this.$('.side-nav-menu-button').toggleClass('hidden', collapsed);
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
