/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone'
], function(Backbone) {

    'use strict';

    return Backbone.View.extend({
        // abstract
        TabSubContentConstructor: null,

        attributes: {
            role: 'tabpanel'
        },

        initialize: function(options) {
            this.tab = options.tab;

            var active = this.tab.index === 0 ? 'active' : '';
            this.$el
                .attr('id', 'document-detail-tab-' + this.tab.index)
                .attr('class', 'tab-pane tab-content-view-container ' + active);

            this.contentView = new (this.TabSubContentConstructor)({
                tab: this.tab,
                model: options.model,
                indexesCollection: options.indexesCollection
            });
        },

        render: function() {
            this.contentView.setElement(this.$el).render();
        },

        remove: function() {
            this.contentView.remove();
            Backbone.View.prototype.remove.call(this);
        }
    });

});
