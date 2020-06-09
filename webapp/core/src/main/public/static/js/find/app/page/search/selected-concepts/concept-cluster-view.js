/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
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
    'underscore',
    'jquery',
    'backbone',
    'find/app/util/popover',
    './edit-concept-view',
    'text!find/templates/app/page/search/selected-concepts/selected-concept.html',
    'bootstrap'
], function(_, $, Backbone, popover, EditConceptView, template) {
    'use strict';

    /**
     * Attributes of a concept group model.
     * @typedef {Object} ConceptGroupModelAttributes
     * @property {string[]} concepts
     */
    return Backbone.View.extend({
        template: _.template(template),
        className: 'selected-concept-container',

        render: function() {
            this.$('[data-toggle="tooltip"]').tooltip('destroy');

            this.$content = $('<div class="inline-block"></div>');

            this.$el.html(this.$content);

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

            this.$content.html(this.template({clusterCid: this.model.cid, concepts: concepts}));

            this.$('[data-toggle="tooltip"]').tooltip({
                container: 'body',
                placement: 'top'
            });
        },

        createPopover: function() {
            let $popover;
            const $popoverControl = this.$content;

            const clickHandler = _.bind(function(e) {
                const $target = $(e.target);
                const isPopover = $target.is($popover) || $.contains($popover[0], $target[0]);
                const isPopoverControl = $target.is($popoverControl) || $.contains($popoverControl[0], $target[0]);

                if(!(isPopover || isPopoverControl)) {
                    this.$content.popover('hide');
                }
            }, this);

            popover($popoverControl, 'click', _.bind(function(content) {
                    content.html('<div class="edit-concept-container"></div>');
                    this.renderEditConcept();
                    $popover = content.closest('.popover');

                    $(document.body).on('click', clickHandler);
                }, this),
                _.bind(function() {
                    $(document.body).off('click', clickHandler);
                }, this));
        },

        renderEditConcept: function() {
            this.editConceptView = new EditConceptView({
                model: this.model
            });

            this.$('.edit-concept-container').append(this.editConceptView.$el);
            this.editConceptView.render();

            this.listenTo(this.editConceptView, 'remove', function() {
                this.$content.popover('hide');
            });
        }
    });
});
