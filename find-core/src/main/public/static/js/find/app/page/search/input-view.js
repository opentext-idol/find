define([
    'backbone',
    'jquery',
    'underscore',
    'find/app/vent',
    'find/app/util/string-blank',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/input-view.html',
    'typeahead',
    'bootstrap'
], function(Backbone, $, _, vent, stringBlank, i18n, template) {

    var html = _.template(template)({i18n: i18n});
    var relatedConceptsTemplate = _.template('<span class="selected-related-concept" data-id="<%-concept%>" data-toggle="tooltip" title="<%-concept%>">' +
        '<span class="selected-related-concept-text shorten"><%-concept%></span> ' +
        '<i class="clickable hp-icon hp-fw hp-close concept-remove-icon"></i>' +
        '</span>');
    var scrollingButtons = _.template('<span class="scrolling-buttons pull-right">' +
        '<button class="btn btn-xs btn-white left-scroll"><i class="hp-icon hp-chevron-left"></i></button> ' +
        '<button class="btn btn-xs btn-white right-scroll"><i class="hp-icon hp-chevron-right"></i></button> ' +
        '</span>');


    return Backbone.View.extend({
        events: {
            'submit .find-form': function (event) {
                event.preventDefault();
                this.search(this.$input.typeahead('val'));
                this.$input.typeahead('close');
            },
            'typeahead:select': function () {
                this.search(this.$input.typeahead('val'));
            },
            'click .concept-remove-icon': function (e) {
                var id = $(e.currentTarget).closest("span.selected-related-concept").attr('data-id');

                $(e.currentTarget).closest('[data-toggle="tooltip"]').tooltip('destroy');
                this.removeRelatedConcept(id);

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

        initialize: function() {
            this.listenTo(this.model, 'change:inputText', this.updateText);
            this.listenTo(this.model, 'change:relatedConcepts', this.updateRelatedConcepts);

            this.listenTo(vent, 'vent:resize', this.updateScrollingButtons);

            _.bindAll(this, 'updateScrollingButtons');
        },

        render: function () {
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
                source: function (query, sync, async) {
                    // Don't look for suggestions if the query is blank
                    if (stringBlank(query)) {
                        sync([]);
                    } else {
                        $.get('../api/public/typeahead', {
                            text: query
                        }, function (results) {
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

        search: function (query) {
            if (query === this.model.get('inputText')) {
                this.model.refresh();
            } else {
                var trimmedText = $.trim(query);

                this.model.set({
                    inputText: trimmedText,
                    relatedConcepts: []
                });
            }
        },

        updateText: function() {
            if (this.$input) {
                this.$input.typeahead('val', this.model.get('inputText'));
            }
        },

        updateRelatedConcepts: function () {
            if (this.$additionalConcepts) {
                this.$additionalConcepts.empty();
                this.$('.scrolling-buttons').remove();

                _.each(this.model.get('relatedConcepts'), function (concept) {
                    this.$additionalConcepts.prepend(relatedConceptsTemplate({
                        concept: concept
                    }));
                }, this);

                this.$('[data-toggle="tooltip"]').tooltip({
                    container: 'body',
                    placement: 'bottom'
                });

                this.$alsoSearchingFor.toggleClass('hide', _.isEmpty(this.model.get('relatedConcepts')));

                this.updateScrollingButtons();
            }
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

        removeRelatedConcept: function (id) {
            var newConcepts = _.without(this.model.get('relatedConcepts'), id);

            this.model.set('relatedConcepts', newConcepts);
        }
    });

});
