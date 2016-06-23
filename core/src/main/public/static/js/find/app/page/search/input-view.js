define([
    'backbone',
    'jquery',
    'underscore',
    'find/app/vent',
    'find/app/util/string-blank',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/input-view.html',
    'text!find/templates/app/page/search/related-concepts/selected-related-concept.html',
    'typeahead',
    'bootstrap'
], function(Backbone, $, _, vent, stringBlank, i18n, template, relatedConceptTemplate) {

    var html = _.template(template)({i18n: i18n});

    var conceptsTemplate = _.template(relatedConceptTemplate);
    
    var scrollingButtons = _.template('<span class="scrolling-buttons">' +
            '<button class="btn btn-xs btn-white left-scroll"><i class="hp-icon hp-chevron-left"></i></button> ' +
            '<button class="btn btn-xs btn-white right-scroll"><i class="hp-icon hp-chevron-right"></i></button> ' +
        '</span>');

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

                $(e.currentTarget).closest('[data-toggle="tooltip"]').tooltip('destroy');
                this.removeRelatedConcept(id);
            },
            'hide.bs.dropdown .selected-related-concept-dropdown-container': function(e) {
                this.toggleRelatedConceptClusterDropdown(false, $(e.currentTarget));
            },
            'show.bs.dropdown .selected-related-concept-dropdown-container': function(e) {
                this.toggleRelatedConceptClusterDropdown(true, $(e.currentTarget), $(e.relatedTarget));
            },
            'click .see-all-documents': function() {
                var queryState = this.queryStates.get(this.selectedTabModel.get('selectedSearchCid'));

                queryState.datesFilterModel.clear().set(queryState.datesFilterModel.defaults);
                queryState.selectedParametricValues.reset();
                queryState.selectedIndexes.set(this.indexesCollection.toResourceIdentifiers());

                this.search('*');
            },
            'click .right-scroll': function(e) {
                e.preventDefault();

                var $additionalConcepts = this.$('.additional-concepts');
                $additionalConcepts.scrollLeft($additionalConcepts.scrollLeft() + 85);
            },
            'click .left-scroll': function(e) {
                e.preventDefault();

                var $additionalConcepts = this.$('.additional-concepts');
                $additionalConcepts.scrollLeft($additionalConcepts.scrollLeft() - 85);
            }
        },

        initialize: function(options) {
            this.queryStates = options.queryStates;
            this.selectedTabModel = options.selectedTabModel;
            this.indexesCollection = options.indexesCollection;

            this.listenTo(this.model, 'change:inputText', this.updateText);
            this.listenTo(this.model, 'change:relatedConcepts', this.updateRelatedConcepts);

            this.listenTo(vent, 'vent:resize', this.updateScrollingButtons);

            if (!options.hasBiRole) {
                this.listenTo(this.queryStates, 'change', function() {
                    // listen to the new query state(s)
                    var changed = this.queryStates.changed[this.selectedTabModel.get('selectedSearchCid')];

                    if(changed){
                        var update = _.bind(function() {
                            // all selected indexes is the default
                            var hasSelectedIndexes = changed.selectedIndexes.length < this.indexesCollection.length;
                            var hasSelectedParametricValues = changed.selectedParametricValues.length > 0;

                            // if a date range is not selected the query model attributes will only contain null values
                            var hasSelectedDates = _.chain(changed.datesFilterModel.toQueryModelAttributes())
                                .values()
                                .any()
                                .value();

                            this.updateSeeAllDocumentsLink(hasSelectedIndexes || hasSelectedParametricValues || hasSelectedDates);
                        }, this);

                        this.listenTo(changed.selectedIndexes, 'add remove', update);
                        this.listenTo(changed.selectedParametricValues, 'add remove', update);
                        this.listenTo(changed.datesFilterModel, 'change', update);
                    }
                });
            }

            _.bindAll(this, 'updateScrollingButtons');
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

        focus: function() {
            _.defer(_.bind(function () {
                this.$input.focus();
            }, this));
        },

        unFocus: function() {
            _.defer(_.bind(function () {
                this.$input.blur();
            }, this));
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
                this.updateSeeAllDocumentsLink();
            }
        },

        updateSeeAllDocumentsLink: function(queryStateChanged) {
            var disableLink = this.model.get('inputText') === '*' && !this.model.get('relatedConcepts').length && !queryStateChanged;
            this.$('.see-all-documents').toggleClass('disabled-clicks cursor-not-allowed', disableLink);
        },

        updateRelatedConcepts: function() {
            if (this.$additionalConcepts) {
                this.$additionalConcepts.empty();
                this.$('.scrolling-buttons').remove();

                _.each(this.model.get('relatedConcepts'), function(conceptCluster, index) {
                    this.$additionalConcepts.prepend(conceptsTemplate({
                        concepts: conceptCluster,
                        clusterId: index
                    }));
                }, this);

                this.$('[data-toggle="tooltip"]').tooltip({
                    container: 'body',
                    placement: 'top'
                });

                this.$alsoSearchingFor.toggleClass('hide', _.isEmpty(this.model.get('relatedConcepts')));

                this.updateScrollingButtons();
            }

            this.updateSeeAllDocumentsLink();
        },

        updateScrollingButtons: function() {
            if (this.$additionalConcepts) {
                //calculate the total width of all the related concepts
                var relatedConceptsWidth = 0;

                this.$('.selected-related-concept').each(function () {
                    relatedConceptsWidth += parseInt($(this).outerWidth(true), 10);
                });

                //add scrolling template if total width of rc's is bigger than their container
                if (this.$additionalConcepts.width() < relatedConceptsWidth) {
                    if(!this.$('.scrolling-buttons').length) {
                        this.$additionalConcepts.after(scrollingButtons);
                    }
                } else {
                    this.$('.scrolling-buttons').remove();
                }
            }
        },

        toggleRelatedConceptClusterDropdown: function (open, $target, $relatedTarget) {
            var $icon = $target.find('.selected-related-concept-cluster-chevron');
            $icon.toggleClass('hp-chevron-up', open);
            $icon.toggleClass('hp-chevron-down', !open);

            if($relatedTarget) {
                var triggerOffset = $relatedTarget.closest('.selected-related-concept').offset();

                $target.offset({
                    top: triggerOffset.top + 22,
                    left: triggerOffset.left
                });
            }
        },

        removeRelatedConcept: function(id) {
            var concepts = _.clone(this.model.get('relatedConcepts'));
            concepts.splice(id, 1);
            this.model.set('relatedConcepts', concepts);
        }
    });

});
