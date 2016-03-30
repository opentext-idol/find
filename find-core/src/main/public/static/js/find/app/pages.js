/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone'
], function(Backbone) {

    function changePage(name) {
        if (this.currentPage !== name && this.pageData[name]) {
            if (this.currentPage) {
                var currentView = this.pages[this.currentPage].view;
                currentView.hide();
                this.stopListening(currentView, 'change-title');
            }

            if (!this.pages[name].hasRendered) {
                this.pages[name].view.render();
                this.$el.append(this.pages[name].view.$el);
                this.pages[name].hasRendered = true;
            }

            var view = this.pages[name].view;
            view.show();
            this.currentPage = name;
        }
    }

    return Backbone.View.extend({
        initialize: function(options) {
            this.pageData = options.pageData;
            this.pages = {};

            _.each(this.pageData, function(data, name) {
                var viewOptions = {router: options.router};

                this.pages[name] = {
                    hasRendered: false,
                    view: new data.Constructor(viewOptions)
                };
            }, this);

            this.listenTo(options.router, 'route:find', changePage);
        }
    });

});
