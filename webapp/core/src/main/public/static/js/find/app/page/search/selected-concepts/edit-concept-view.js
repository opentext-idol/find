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
    'backbone',
    'underscore',
    'jquery',
    'find/app/util/global-key-listener',
    'text!find/templates/app/page/search/selected-concepts/edit-concept-view.html',
    'i18n!find/nls/bundle'
], function(Backbone, _, $, globalKeyListener, template, i18n) {
    'use strict';

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
                const regex = /(("[^"]+"|[^"\n]+)+)\n/g;

                let match = regex.exec(newConceptsString);
                while(match != null) {
                    concepts.push(match[1].trim());
                    match = regex.exec(newConceptsString);
                }

                this.model.set({
                    concepts: _.compact(concepts.map(function(string) {
                        // the regex above will leave new lines if they were between quotes
                        return string.replace(/\n+/g, ' ').trim();
                    }))
                });

                this.trigger('remove');
            }
        },

        initialize: function() {
            Backbone.View.prototype.initialize.apply(this, arguments);
            this.listenTo(globalKeyListener, 'escape', function(){
                this.trigger('remove');
            });
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
            var disabled = !this.$('.edit-concept-input').val().trim();
            this.$confirmButton.toggleClass('disabled not-clickable', disabled).prop('disabled', disabled);
        }
    });
});
