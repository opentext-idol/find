define([
    'backbone',
    'jquery',
    'underscore',
    'find/app/util/string-blank',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/input-view.html',
    'typeahead'
], function(Backbone, $, _, stringBlank, i18n, template) {

    var html = _.template(template)({i18n: i18n});
    
    var relatedConceptsTemplate = _.template(
        '<span class="selected-related-concepts" data-id="<%-concept%>">' +
            '<%-concept%> <i class="clickable hp-icon hp-fw hp-close concepts-remove-icon"></i>' +
        '</span> '
    );

    return Backbone.View.extend({
        events: {
            'submit .find-form': function(event) {
                event.preventDefault();
                this.search(this.$input.typeahead('val'));
                this.$input.typeahead('close');
            },
            'typeahead:select': function() {
                this.search(this.$input.typeahead('val'));
            },
            'click .concepts-remove-icon': function(e) {
                var id = $(e.currentTarget).closest("span").attr('data-id');

                this.removeRelatedConcept(id);
            },
            'click .see-all-documents': function() {
                this.model.set('queryText', '*');
            }
        },

        initialize: function(options) {
            this.listenTo(this.model, 'change:queryText', this.updateText);
            this.listenTo(this.model, 'change:relatedConcepts', this.updateRelatedConcepts);
            
            this.search = _.debounce(function(query) {
                options.model.set({inputText: query});
            }, 500);
        },

        render: function() {
            this.$el.html(html);
            this.$input = this.$('.find-input');
            this.$additionalConcepts = this.$('.additional-concepts');
            this.$alsoSearchingFor = this.$('.also-searching-for');

            this.$input.typeahead({
                hint: false,
                hightlight: true,
                minLength: 1
            }, {
                async: true,
                limit: 7,
                source: function(query, sync, async) {
                    // Don't look for suggestions if the query is blank
                    if (stringBlank(query)) {
                        sync([]);
                    } else {
                        $.get('../api/public/typeahead', {
                            text: query
                        }, function(results) {
                            async(results);
                        });
                    }
                }
            });

            this.updateText();
            this.updateRelatedConcepts();
        },

        updateText: function() {
            if (this.$input) {
                this.$input.typeahead('val', this.model.get('inputText'));
                this.$('.see-all-documents').toggleClass('disabled-clicks cursor-not-allowed', this.model.get('inputText') === '*');
            }
        },

        updateRelatedConcepts: function() {
            if (this.$additionalConcepts) {
                this.$additionalConcepts.empty();

                _.each(this.model.get('relatedConcepts'), function(concept) {
                    this.$additionalConcepts.append(relatedConceptsTemplate({
                        concept: concept
                    }))
                }, this);

                this.$alsoSearchingFor.toggleClass('hide', _.isEmpty(this.model.get('relatedConcepts')));
            }
        },

        removeRelatedConcept: function(id){
            var newConcepts = _.without(this.model.get('relatedConcepts'), id);
            this.model.set('relatedConcepts', newConcepts);
        }
    });

});
