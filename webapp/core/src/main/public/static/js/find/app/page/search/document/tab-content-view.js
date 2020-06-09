/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
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
                indexesCollection: options.indexesCollection,
                documentRenderer: options.documentRenderer
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
