/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    'underscore',
    'find/app/page/search/filters/parametric/abstract-parametric-view',
    'find/app/page/search/filters/parametric/numeric-parametric-field-view',
    'js-whatever/js/list-view',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/filters/parametric/numeric-parametric-view.html'
], function($, _, AbstractView, FieldView, ListView, i18n, template) {
    "use strict";
    
    var PreInitialisedListView = ListView.extend({
        createItemView: function(model) {
            //noinspection AssignmentResultUsedJS,JSUnresolvedFunction
            var view = this.views[model.cid] = new this.ItemView(_.extend({
                model: model,
                viewWidth: this.$el.width()
            }, this.itemOptions));

            //noinspection JSUnresolvedFunction
            _.each(this.proxyEvents, function(event) {
                this.listenTo(view, event, function() {
                    this.trigger.apply(this, ['item:' + event].concat(Array.prototype.slice.call(arguments, 0)));
                });
            }, this);

            view.render();
            return view;
        }
    });

    return AbstractView.extend({
        template: _.template(template)({i18n: i18n}),
        
        initialize: function(options) {
            this.monitorCollection(options.numericParametricCollection);

            this.fieldNamesListView = new PreInitialisedListView({
                collection: options.numericParametricCollection,
                ItemView: FieldView
            });
        },

        remove: function() {
            this.fieldNamesListView.remove();
            AbstractView.prototype.remove.call(this);
        },

        updateEmpty: $.noop
    });
});