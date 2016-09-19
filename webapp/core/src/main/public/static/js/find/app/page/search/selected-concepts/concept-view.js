/*
 * Copyright 2015-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'jquery',
    'underscore',
    './concept-cluster-view',
    'find/app/page/search/input-view',
    'i18n!find/nls/bundle',
    'js-whatever/js/list-view',
    'text!find/templates/app/page/search/concept-view.html'
], function (Backbone, $, _, ConceptClusterView, InputView, i18n, ListView, template) {

    /**
     * View for displaying the selected concept groups eg in the left side panel.
     * Expects to be given the queryState on construction.
     */
    return Backbone.View.extend({
        html: _.template(template)({i18n: i18n}),

        events: {
            'click .concept-remove-icon': function (event) {
                const cid = $(event.currentTarget).closest('.selected-related-concept').attr('data-cluster-cid');
                this.conceptGroups.remove(cid);
            }
        },

        initialize: function (options) {
            this.conceptGroups = options.queryState.conceptGroups;

            const optionalViews = [{
                enabled: options.configuration.hasBiRole,
                selector: '.concept-view-container',
                construct: function () {
                    return new InputView({
                        model: options.queryState.queryTextModel
                    });
                }
            }];
            //noinspection JSUnresolvedFunction
            this.optionalViews = _.where(optionalViews, {enabled: true});

            //noinspection JSUnresolvedFunction
            this.optionalViews.forEach(function (view) {
                view.instance = view.construct();
            });


            this.listView = new ListView({
                collection: this.conceptGroups,
                ItemView: ConceptClusterView
            });

            this.listenTo(this.conceptGroups, 'update reset', this.updateEmpty);
        },

        render: function () {
            this.$el.html(this.html);

            this.optionalViews.forEach(function (view) {
                view.instance.setElement(this.$(view.selector)).render();
            }, this);

            this.listView.setElement(this.$('.concept-view-concepts')).render();
            this.updateEmpty();
        },

        updateEmpty: function () {
            const empty = this.conceptGroups.isEmpty();
            this.listView.$el.toggleClass('hide', empty);
            this.$('.concept-view-empty-message').toggleClass('hide', !empty);
        },

        remove: function () {
            //noinspection JSUnresolvedFunction
            _.chain(this.optionalViews).pluck('instance').invoke('remove');

            this.listView.remove();
            Backbone.View.prototype.remove.call(this);
        }
    });

});
