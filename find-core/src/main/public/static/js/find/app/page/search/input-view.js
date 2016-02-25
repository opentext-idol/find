define([
    'backbone',
    'jquery',
    'underscore',
    'find/app/util/string-blank',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/input-view.html',
    'text!find/templates/app/page/search/related-concepts/selected-related-concept.html',
    'typeahead'
], function(Backbone, $, _, stringBlank, i18n, template, relatedConceptTemplate) {

    var html = _.template(template)({i18n: i18n});
    
    var conceptsTemplate = _.template(relatedConceptTemplate);

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
            'click .concept-remove-icon': function(e) {
                var id = $(e.currentTarget).closest('.selected-related-concept').data('id');

                this.removeRelatedConcept(id);
            },
            'hide.bs.dropdown .selected-related-concept-dropdown-container': function(e) {
                this.toggleRelatedConceptClusterDropdown(false, $(e.currentTarget));
            },
            'show.bs.dropdown .selected-related-concept-dropdown-container': function(e) {
                this.toggleRelatedConceptClusterDropdown(true, $(e.currentTarget));
            },
            'click .see-all-documents': function() {
                this.search('*');
            }
        },

        initialize: function() {
            this.listenTo(this.model, 'change:inputText', this.updateText);
            this.listenTo(this.model, 'change:relatedConcepts', this.updateRelatedConcepts);
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

        search: function(query) {
            this.model.set({
                inputText: $.trim(query),
                relatedConcepts: []
            });
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

                _.each(this.model.get('relatedConcepts'), function(conceptCluster, index) {
                    this.$additionalConcepts.append(conceptsTemplate({
                        concepts: conceptCluster,
                        clusterId: index
                    }));
                }, this);

                this.$alsoSearchingFor.toggleClass('hide', _.isEmpty(this.model.get('relatedConcepts')));
            }
        },

        toggleRelatedConceptClusterDropdown: function (open, $target) {
            var $icon = $target.find('.selected-related-concept-cluster-chevron');
            $icon.toggleClass('hp-chevron-up', open);
            $icon.toggleClass('hp-chevron-down', !open);
        },

        removeRelatedConcept: function(id){
            var concepts = this.model.get('relatedConcepts').slice(0);
            concepts.splice(id, 1);
            this.model.set('relatedConcepts', concepts);
        }
    });

});
