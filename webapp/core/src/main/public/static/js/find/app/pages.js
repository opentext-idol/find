/*
 * Copyright 2014-2015 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
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
