/*
 * Copyright 2014-2018 Open Text.
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

const _ = require('underscore');
const $ = require('jquery');
const Backbone = require('backbone');
const Dropzone = require('dropzone');
const testBrowser = require('find/app/util/test-browser');
const WindowScrollModel = require('find/app/model/window-scroll-model');
const SavedQueryCollection = require('find/app/model/saved-searches/saved-query-collection');
const SharedSavedQueryCollection = require('find/app/model/saved-searches/shared-saved-query-collection');
const parseUrl = require('find/app/util/parse-url');
const ModelRegistry = require('./model-registry');
const Navigation = require('find/app/navigation');
const configuration = require('find/app/configuration');
const Pages = require('find/app/pages');
const logout = require('find/app/util/logout');
const vent = require('find/app/vent');
const router = require('find/app/router');
const conversation = require('find/app/util/conversation');
const escapeRegex = require('js-whatever/js/escape-regex');
const template = require('find/templates/app/app.html');

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

module.exports = Backbone.View.extend({
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

