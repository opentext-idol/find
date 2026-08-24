/*
 * Copyright 2015-2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

const _ = require('underscore');
const $ = require('jquery');
const Backbone = require('backbone');
const popover = require('find/app/util/popover');
const EditConceptView = require('./edit-concept-view');
const template = require('find/templates/app/page/search/selected-concepts/selected-concept.html');

// Loaded for side effects only - do not remove.
require('bootstrap');

/**
 * Attributes of a concept group model.
 * @typedef {Object} ConceptGroupModelAttributes
 * @property {string[]} concepts
 */

module.exports = Backbone.View.extend({
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

