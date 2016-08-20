/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore'
], function(Backbone, _) {

    return Backbone.View.extend({
        initialize: function(options) {
            this.pages = _.mapObject(options.pageData, function(data) {
                var viewOptions = {router: options.router};

                _.each(data.models, function(modelName) {
                    viewOptions[modelName] = options.modelRegistry.get(modelName);
                });

                return {
                    hasRendered: false,
                    view: new data.Constructor(viewOptions)
                };
            });

            this.listenTo(options.router, 'route:find', function(name) {
                var pageName = this.pages[name] ? name : options.defaultPage;

                if (pageName && this.currentPage !== pageName) {
                    if (this.currentPage) {
                        var currentView = this.pages[this.currentPage].view;
                        currentView.hide();
                        this.stopListening(currentView, 'change-title');
                    }

                    if (!this.pages[pageName].hasRendered) {
                        this.pages[pageName].view.render();
                        this.$el.append(this.pages[pageName].view.$el);
                        this.pages[pageName].hasRendered = true;
                    }

                    var view = this.pages[pageName].view;
                    view.show();
                    this.currentPage = pageName;
                }
            });
        }
    });

});
