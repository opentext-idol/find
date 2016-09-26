/*
 * Copyright 2015-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
define([
    'find/app/page/search/abstract-section-view',
    'jquery',
    'underscore',
    './concept-cluster-view',
    'i18n!find/nls/bundle',
    'js-whatever/js/list-view',
    'text!find/templates/app/page/search/concept-view.html'
], function(AbstractSectionView, $, _, ConceptClusterView, i18n, ListView, template) {

    /**
     * View for displaying the selected concept groups eg in the left side panel.
     * Expects to be given the queryState on construction.
     */
    return AbstractSectionView.extend({
        html: _.template(template)({i18n: i18n}),

        events: {
            'click .concept-remove-icon': function(event) {
                const cid = $(event.currentTarget).closest('.selected-related-concept').attr('data-cluster-cid');
                this.conceptGroups.remove(cid);
            }
        },

        initialize: function(options) {
            AbstractSectionView.prototype.initialize.apply(this, arguments);

            this.conceptGroups = options.queryState.conceptGroups;

            this.listView = new ListView({
                collection: this.conceptGroups,
                ItemView: ConceptClusterView
            });

            this.listenTo(this.conceptGroups, 'update reset', this.updateEmpty);
        },

        render: function() {
            AbstractSectionView.prototype.render.apply(this, arguments);

            this.getViewContainer().html(this.html);
            this.listView.setElement(this.$('.concept-view-concepts')).render();
            this.updateEmpty();
        },

        updateEmpty: function() {
            const empty = this.conceptGroups.isEmpty();
            this.listView.$el.toggleClass('hide', empty);
            this.$('.concept-view-empty-message').toggleClass('hide', !empty);
        },

        remove: function() {
            this.listView.remove();
            AbstractSectionView.prototype.remove.call(this);
        }
    });
});
