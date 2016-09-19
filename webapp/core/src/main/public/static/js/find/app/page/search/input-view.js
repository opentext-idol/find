define([
    'backbone',
    'jquery',
    'underscore',
    'find/app/util/database-name-resolver',
    'find/app/util/string-blank',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/input-view.html',
    'typeahead',
    'bootstrap'
], function(Backbone, $, _, databaseNameResolver, stringBlank, i18n, template) {

    return Backbone.View.extend({
        template: _.template(template),

        events: {
            'submit .find-form': function(event) {
                event.preventDefault();
                this.search(this.$input.typeahead('val'));
                this.$input.typeahead('close');
            },
            'typeahead:select': function() {
                this.search(this.$input.typeahead('val'));
            },
            'click .see-all-documents': function() {
                var queryState = this.queryStates.get(this.selectedTabModel.get('selectedSearchCid'));

                if (queryState) {
                    queryState.datesFilterModel.clear().set(queryState.datesFilterModel.defaults);
                    queryState.selectedParametricValues.reset();
                    queryState.selectedIndexes.set(databaseNameResolver.getDatabaseInfoFromCollection(this.indexesCollection));
                }

                this.search('*');
            }
        },

        initialize: function(options) {
            this.queryStates = options.queryStates;
            this.selectedTabModel = options.selectedTabModel;
            this.indexesCollection = options.indexesCollection;
            this.hasBiRole = options.hasBiRole;

            this.listenTo(this.model, 'change:inputText', this.updateText);

            if (!this.hasBiRole) {
                this.listenTo(this.queryStates, 'change', function() {
                    // listen to the new query state(s)
                    var changed = this.queryStates.changed[this.selectedTabModel.get('selectedSearchCid')];

                    if (changed) {
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
        },

        render: function() {
            this.$el.html(this.template({i18n: i18n, hasBiRole: this.hasBiRole}));
            this.$input = this.$('.find-input');

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
                        $.get('api/public/typeahead', {
                            text: query
                        }, function(results) {
                            async(results);
                        });
                    }
                }
            });

            this.updateText();
        },

        focus: function() {
            _.defer(_.bind(function() {
                this.$input.focus();
            }, this));
        },

        unFocus: function() {
            _.defer(_.bind(function() {
                this.$input.blur();
            }, this));
        },

        search: function(query) {
            this.model.set({inputText: $.trim(query)});
        },

        updateText: function() {
            if (this.$input) {
                this.$input.typeahead('val', this.model.get('inputText'));
                this.updateSeeAllDocumentsLink();
            }
        },

        updateSeeAllDocumentsLink: function(queryStateChanged) {
            if (this.hasBiRole) {
                var disableLink = this.model.get('inputText') === '*' && !queryStateChanged;
                this.$('.see-all-documents').toggleClass('disabled-clicks cursor-not-allowed', disableLink);
            }
        }
    });

});
