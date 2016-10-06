/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'jquery',
    'text!find/templates/app/page/search/selected-concepts/edit-concept-view.html',
    'i18n!find/nls/bundle'
], function(Backbone, _, $, template, i18n) {
    "use strict";

    return Backbone.View.extend({
        className: 'edit-concept-form',
        tagName: 'form',
        template: _.template(template),

        events: {
            'input .edit-concept-input': 'updateConfirmButton',
            'click .edit-concept-cancel-button': function() {
                this.trigger('remove');
            },
            'submit': function(event) {
                event.preventDefault();
                var newConceptsString = this.$('.edit-concept-input').val() + '\n';

                const concepts = [];
                //matching quoted or non quoted string and new lines after them
                const regex = new RegExp('("[^"]+"|[^"\n]+)\s*\n', 'g');

                let match = regex.exec(newConceptsString);
                while (match != null) {
                    concepts.push(match[1]);
                    match = regex.exec(newConceptsString);
                }

                this.model.set({
                    concepts: _.compact(concepts.map(function(string) {return string.trim()}))
                });

                this.trigger('remove');
            }
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n
            }));

            this.$confirmButton = this.$('.edit-concept-confirm-button');
            this.$conceptInput = this.$('.edit-concept-input');

            const conceptsString = this.model.get('concepts').join('\n');

            this.$conceptInput
                .focus()
                .val(conceptsString);

            this.updateConfirmButton();
        },

        updateConfirmButton: function() {
            var disabled = !this.$('.edit-concept-input').val();
            this.$confirmButton.toggleClass('disabled not-clickable', disabled).prop('disabled', disabled);
        }
    });

});
