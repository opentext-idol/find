/*
 * Copyright 2015-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'text!find/templates/app/page/search/selected-concepts/selected-concept.html',
    'text!find/templates/app/page/search/selected-concepts/selected-concept-cluster.html',
    'bootstrap'
], function(Backbone, _, conceptTemplate, conceptClusterTemplate) {

    /**
     * Attributes of a concept group model.
     * @typedef {Object} ConceptGroupModelAttributes
     * @property {string[]} concepts
     */
    /**
     * View for displaying the concept(s) in a concept group model with the following attributes: {@link ConceptGroupModelAttributes}.
     * If the model has more than one concept, a dropdown is displayed, otherwise a dropdown is not displayed.
     * This view expects {@link #render} to be called from outside each time the model changes (probably by a ListView).
     */
    return Backbone.View.extend({
        conceptTemplate: _.template(conceptTemplate),
        clusterTemplate: _.template(conceptClusterTemplate),
        className: 'selected-concept-container',

        events: {
            'hide.bs.dropdown .selected-related-concept-dropdown-container': function() {
                this.toggleDropdownIcon(false);
            },
            'show.bs.dropdown .selected-related-concept-dropdown-container': function() {
                this.toggleDropdownIcon(true);
            }
        },

        render: function() {
            this.$('[data-toggle="tooltip"]').tooltip('destroy');

            const concepts = this.model.get('concepts');
            const template = concepts.length > 1 ? this.clusterTemplate : this.conceptTemplate;

            this.$el.html(template({clusterCid: this.model.cid, concepts: concepts}));

            this.$('[data-toggle="tooltip"]').tooltip({
                container: 'body',
                placement: 'top'
            });

            // The chevron only exists for clusters, not single concepts
            if (concepts.length > 1) {
                this.toggleDropdownIcon(false);
            }
        },

        remove: function() {
            this.$('[data-toggle="tooltip"]').tooltip('destroy');
            Backbone.View.prototype.remove.call(this);
        },

        toggleDropdownIcon: function(open) {
            this.$('.selected-related-concept-cluster-chevron')
                .toggleClass('hp-chevron-up', open)
                .toggleClass('hp-chevron-down', !open);
        }
    });

});
