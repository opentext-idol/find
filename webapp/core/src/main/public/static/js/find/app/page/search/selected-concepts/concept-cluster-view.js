/*
 * Copyright 2015-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'find/app/util/popover',
    './edit-concept-view',
    'text!find/templates/app/page/search/selected-concepts/selected-concept.html',
    'text!find/templates/app/page/search/selected-concepts/selected-concept-cluster.html',
    'jquery',
    'bootstrap'
], function(Backbone, _, popover, EditConceptView, conceptTemplate, conceptClusterTemplate, $) {

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
        editButtonHtml: '<button class="btn btn-xs button-primary edit-concept-button"><i class="hp-icon hp-edit"></i></button>',

        events: {
            'hide.bs.dropdown .selected-related-concept-dropdown-container': function() {
                this.toggleDropdownIcon(false);
            },
            'show.bs.dropdown .selected-related-concept-dropdown-container': function() {
                this.toggleDropdownIcon(true);
            },
            'mouseover': function() {
                this.$('.edit-concept-button').addClass('hovered-on');
            },
            'mouseout': function() {
                this.$('.edit-concept-button').removeClass('hovered-on');
            }
        },

        render: function() {
            this.$('[data-toggle="tooltip"]').tooltip('destroy');

            this.$content = $('<div class="inline-block"></div>');

            this.$el.empty()
                .append(this.$content)
                .append(this.editButtonHtml);

            this.updateConcepts();
            this.createPopover();
        },

        remove: function() {
            this.$('[data-toggle="tooltip"]').tooltip('destroy');
            this.$('.popover').popover('destroy');
            Backbone.View.prototype.remove.call(this);
        },

        // Called from outside whenever the model's concepts are changed
        updateConcepts: function() {
            this.$('[data-toggle="tooltip"]').tooltip('destroy');

            const concepts = this.model.get('concepts');
            const template = concepts.length > 1 ? this.clusterTemplate : this.conceptTemplate;

            this.$content.html(template({clusterCid: this.model.cid, concepts: concepts}));

            this.$('[data-toggle="tooltip"]').tooltip({
                container: 'body',
                placement: 'top'
            });

            // The chevron only exists for clusters, not single concepts
            if (concepts.length > 1) {
                this.toggleDropdownIcon(false);
            }
        },

        toggleDropdownIcon: function(open) {
            this.$('.selected-related-concept-cluster-chevron')
                .toggleClass('hp-chevron-up', open)
                .toggleClass('hp-chevron-down', !open);
        },

        createPopover: function () {
            var $popover;
            var $popoverControl = this.$('.edit-concept-button');

            var clickHandler = _.bind(function (e) {
                var $target = $(e.target);
                var notPopover = !$target.is($popover) && !$.contains($popover[0], $target[0]);
                var notPopoverControl = !$target.is($popoverControl) && !$.contains($popoverControl[0], $target[0]);

                if (notPopover && notPopoverControl) {
                    this.$('.edit-concept-button').click();
                }
            }, this);

            popover($popoverControl, 'click', _.bind(function (content) {
                content.html('<div class="edit-concept-container"></div>');
                this.renderEditConcept();
                $popover = content.closest('.popover');
                $(document.body).on('click', clickHandler);
            }, this), _.bind(function () {
                $(document.body).off('click', clickHandler);
            }, this));
        },

        renderEditConcept: function() {
            this.editConceptView = new EditConceptView({
                model: this.model
            });

            this.$('.edit-concept-container').append(this.editConceptView.$el);
            this.editConceptView.render();

            this.listenTo(this.editConceptView, 'remove', function () {
                this.$('.edit-concept-button').click();
            });
        }
    });

});
