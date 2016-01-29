define([
    'backbone',
    'underscore',
    'jquery',
    'js-whatever/js/list-view',
    'js-whatever/js/filtering-collection',
    'find/app/model/parametric-collection',
    'find/app/page/search/filters/parametric/parametric-field-view',
    'fieldtext/js/field-text-parser',
    'parametric-refinement/display-collection',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/filters/parametric/parametric-view.html'
], function(Backbone, _, $, ListView, FilteringCollection, ParametricCollection, FieldView, parser, DisplayCollection, i18n, template) {

    var DEBOUNCE_WAIT_MILLISECONDS = 500;

    return Backbone.View.extend({
        template: _.template(template)({i18n: i18n}),

        events: {
            'click [data-field] [data-value]': function(e) {
                var $target = $(e.currentTarget);
                var $field = $target.closest('[data-field]');

                var attributes = {
                    field: $field.attr('data-field'),
                    value: $target.attr('data-value')
                };

                if (this.selectedParametricValues.get(attributes)) {
                    this.selectedParametricValues.remove(attributes);
                } else {
                    this.selectedParametricValues.add(attributes);
                }
            }
        },

        initialize: function(options) {
            this.queryModel = options.queryModel;
            this.selectedParametricValues = options.selectedParametricValues;
            this.indexesCollection = options.indexesCollection;
            this.selectedIndexesCollection = options.selectedIndexesCollection;

            this.parametricCollection = new ParametricCollection();

            this.model = new Backbone.Model({processing: false, error: false});
            this.listenTo(this.model, 'change:processing', this.updateProcessing);
            this.listenTo(this.model, 'change:error', this.updateError);

            this.displayCollection = new DisplayCollection([], {
                parametricCollection: this.parametricCollection,
                selectedParametricValues: this.selectedParametricValues
            });

            this.fieldNamesListView = new ListView({
                collection: this.displayCollection,
                ItemView: FieldView
            });

            this.listenTo(this.selectedParametricValues, 'add remove reset', _.debounce(_.bind(function() {
                var node = this.selectedParametricValues.toFieldTextNode();
                this.queryModel.set('fieldText', node && node.toString());
            }, this), DEBOUNCE_WAIT_MILLISECONDS));

            this.listenTo(this.queryModel, 'change:indexes', function() {
                this.selectedParametricValues.reset();
            });

            function fetch() {
                this.parametricCollection.reset();
                this.model.set({processing: true, error: false});

                var fieldNames = this.selectedIndexesCollection.chain()
                    .map(function(database) {
                        var findArguments = {name: database.get('name')};

                        if (database.get('domain')) {
                            findArguments.domain = database.get('domain');
                        }

                        return this.indexesCollection.findWhere(findArguments).get('fieldNames');
                    }, this)
                    .flatten()
                    .uniq()
                    .value();

                if(!this.queryModel.get('queryText') || _.isEmpty(fieldNames)) {
                    this.model.set('processing', false);
                } else {
                    this.parametricCollection.fetch({
                        data: {
                            fieldNames: fieldNames,
                            databases: this.queryModel.get('indexes'),
                            queryText: this.queryModel.get('queryText'),
                            fieldText: this.queryModel.get('fieldText'),
                            minDate: this.queryModel.getIsoDate('minDate'),
                            maxDate: this.queryModel.getIsoDate('maxDate')
                        },
                        error: _.bind(function (collection, xhr) {
                            if (xhr.status !== 0) {
                                // The request was not aborted, so there isn't another request in flight
                                this.model.set({error: true, processing: false});
                            }
                        }, this),
                        success: _.bind(function () {
                            this.model.set({processing: false});
                        }, this)
                    });
                }
            }

            this.listenTo(this.queryModel, 'change', function() {
                if (this.queryModel.hasAnyChangedAttributes(['queryText', 'indexes', 'fieldText', 'minDate', 'maxDate'])) {
                    fetch.call(this);
                }
            }, this);

            fetch.call(this);
        },

        render: function() {
            this.$el.html(this.template).prepend(this.fieldNamesListView.render().$el);

            this.$errorMessage = this.$('.parametric-error');
            this.$processing = this.$('.parametric-processing-indicator');

            this.updateError();
            this.updateProcessing();

            return this;
        },

        updateProcessing: function() {
            if (this.$processing) {
                this.$processing.toggleClass('hide', !this.model.get('processing'));
            }
        },

        updateError: function() {
            if (this.$errorMessage) {
                this.$errorMessage.toggleClass('hide', !this.model.get('error'));
            }
        }
    });

});
