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
    'backbone',
    'find/app/vent',
    'find/app/mmap-tab'
], function(_, Backbone, vent, mmapTab) {
    'use strict';

    return Backbone.View.extend({
        initialize: function(options) {
            this.pages = _.mapObject(options.pageData, function(data) {
                const viewOptions = {router: options.router, mmapTab: mmapTab(options.configuration)};

                _.each(data.models, function(modelName) {
                    viewOptions[modelName] = options.modelRegistry.get(modelName);
                });

                _.extend(viewOptions, data.constructorArguments);

                return {
                    view: new data.Constructor(viewOptions)
                };
            });

            this.listenTo(options.router, 'route:page', function(name) {
                const pageName = this.pages[name]
                    ? name
                    : options.defaultPage;

                if(pageName && this.currentPage !== pageName) {
                    if(this.currentPage) {
                        const currentView = this.pages[this.currentPage].view;
                        currentView.hide();
                        this.stopListening(currentView, 'change-title');
                    }

                    const view = this.pages[pageName].view;
                    view.setLastNavigationOpts && view.setLastNavigationOpts.apply(view, _.toArray(arguments).slice(1));
                    view.show();

                    this.currentPage = pageName;

                    if(view.getSelectedRoute()) {
                        vent.navigate(view.getSelectedRoute(), {trigger: false, replace: true});
                    }
                }
            });
        },

        render: function() {
            _.each(this.pages, function(page) {
                this.$el.append(page.view.$el);
            }, this);
        }
    });
});
