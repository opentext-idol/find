/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'find/app/vent',
    'find/app/mmap-tab'
], function(Backbone, _, vent, mmapTab) {

    return Backbone.View.extend({
        initialize: function(options) {
            this.pages = _.mapObject(options.pageData, function(data) {
                var viewOptions = {router: options.router, mmapTab: mmapTab(options.configuration)};

                _.each(data.models, function(modelName) {
                    viewOptions[modelName] = options.modelRegistry.get(modelName);
                });

                _.extend(viewOptions, data.constructorArguments);

                return {
                    hasRendered: false,
                    view: new data.Constructor(viewOptions)
                };
            });

            this.listenTo(options.router, 'route:page', function(name) {
                var pageName = this.pages[name] ? name : options.defaultPage;

                if (pageName && this.currentPage !== pageName) {
                    if (this.currentPage) {
                        var currentView = this.pages[this.currentPage].view;
                        currentView.hide();
                        this.stopListening(currentView, 'change-title');
                    }

                    // Order is important here. We need to add the element and show it before rendering, so that features
                    // such as the topic map which rely upon DOM size, have something to work with
                    if (!this.pages[pageName].hasRendered) {
                        this.$el.append(this.pages[pageName].view.$el);
                    }

                    var view = this.pages[pageName].view;
                    view.show();

                    if (!this.pages[pageName].hasRendered) {
                        this.pages[pageName].view.render();
                        this.pages[pageName].hasRendered = true;
                    }

                    this.currentPage = pageName;

                    if (view.getSelectedRoute()) {
                        vent.navigate(view.getSelectedRoute(), {trigger: false, replace: true});
                    }
                }
            });
        }
    });

});
