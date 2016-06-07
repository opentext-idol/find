define([
    'backbone',
    'find/app/model/dependent-parametric-collection',
    'underscore',
    'jquery',
    'i18n!find/nls/bundle',
    'find/app/page/search/results/field-selection-view',
    'text!find/templates/app/page/search/results/parametric-results-view.html',
    'text!find/templates/app/page/loading-spinner.html'
], function (Backbone, DependentParametricCollection, _, $, i18n, FieldSelectionView, template, loadingSpinnerTemplate) {
    'use strict';

    var fieldInvalid = function (field, fields) {
        return !field || !_.contains(fields, field);
    };

    function getClickedParameters (data, fields, selectedParameters) {
        if(data.depth !== 0){
            var parameter = {field: fields[data.depth - 1], value: data.text};
            selectedParameters.push(parameter);

            if (data.parent && data.parent.depth !== 0) {
                getClickedParameters(data.parent, fields, selectedParameters)
            }
        }

        return selectedParameters;
    }

    return Backbone.View.extend({
        template: _.template(template),
        loadingHtml: _.template(loadingSpinnerTemplate)({i18n: i18n, large: true}),

        initialize: function (options) {
            this.queryModel = options.queryModel;
            this.parametricCollection = options.parametricCollection;
            this.selectedParametricValues = options.queryState.selectedParametricValues;

            this.emptyDependentMessage = options.emptyDependentMessage;
            this.emptyMessage = options.emptyMessage;
            this.errorMessage = options.errorMessage;

            this.dependentParametricCollection = new DependentParametricCollection();
            this.fieldsCollection = new Backbone.Collection([{text: ''}, {text: ''}]);

            this.model = new Backbone.Model({
                loading: this.parametricCollection.fetching
            });

            this.listenTo(this.fieldsCollection, 'change:field', this.fetchDependentFields);
        },

        update: $.noop,

        render: function () {
            this.$el.html(this.template({
                i18n: i18n,
                loadingHtml: this.loadingHtml
            }));

            this.$loadingSpinner = this.$('.parametric-loading').addClass('hide');

            this.$content = this.$('.parametric-content').addClass('hide');

            this.$message = this.$('.parametric-view-message');

            this.$parametricSelections = this.$('.parametric-selections');
            this.$parametricSelections.addClass('hide');

            this.listenTo(this.fieldsCollection.at(0), 'change:field', this.secondSelection);

            this.listenTo(this.model, 'change:loading', this.toggleLoading);

            this.listenTo(this.parametricCollection, 'error', this.errorHandler);

            this.listenTo(this.parametricCollection, 'sync', this.updateParametricCollection);

            this.listenTo(this.dependentParametricCollection, 'sync', this.updateData);

            this.listenTo(this.dependentParametricCollection, 'error', this.errorHandler);

            this.listenTo(this.selectedParametricValues, 'add remove reset', this.updateSelections);

            this.setLoadingListeners([this.parametricCollection, this.dependentParametricCollection]);

            this.makeSelectionsIfData();

            this.updateSelections();
        },

        toggleLoading: function () {
            this.$loadingSpinner.toggleClass('hide', !this.model.get('loading'));
            this.$content.toggleClass('hide', this.model.get('loading'));
            this.updateMessage();
        },

        errorHandler: function(collection, event) {
            if (event.status !== 0) {
                this.model.set('loading', false);
                this.updateMessage(this.errorMessage);
            }
        },

        setLoadingListeners: function(collections) {
            _.each(collections, function(collection) {
                this.listenTo(collection, 'request', function() {
                    this.model.set('loading', true);
                }, this)
            }, this)
        },

        onClick: function(data) {
            var selectedParameters = getClickedParameters(data, this.fieldsCollection.pluck('field'), []);

            // empty value means padding element was clicked on
            if (!_.findWhere(selectedParameters, {value: ''})) {
                this.selectedParametricValues.add(selectedParameters)
            }
        },

        updateParametricCollection: function() {
            if (!this.parametricCollection.isEmpty() && !this.noMoreParametricFields()) {
                this.$parametricSelections.removeClass('hide');
                this.updateSelections();
                this.makeSelectionsIfData();
            } else {
                this.model.set('loading', false);
                this.$parametricSelections.addClass('hide');
                this.updateMessage(this.emptyMessage);
            }
        },

        updateData: function() {
            this.model.set('loading', false);

            if (!this.parametricCollection.fetching && !this.dependentParametricCollection.isEmpty()) {
                this.update();
            } else if (this.dependentParametricCollection.isEmpty()) {
                this.model.set('loading', false);
                this.updateMessage(this.emptyDependentMessage)
            }

            this.toggleContentDisplay();
        },

        updateSelections: function() {
            this.firstSelection();
            this.secondSelection();
        },

        firstSelection: function () {
            if (this.firstChosen) {
                this.firstChosen.remove();
            }

            this.firstChosen = new FieldSelectionView({
                model: this.fieldsCollection.at(0),
                name: 'first',
                fields: _.difference(this.parametricCollection.pluck('name'), this.selectedParametricValues.pluck('field')).sort(),
                allowEmpty: false
            });

            this.$parametricSelections.prepend(this.firstChosen.$el);
            this.firstChosen.render();
        },

        secondSelection: function () {
            if (this.secondChosen) {
                this.secondChosen.remove();
            }

            this.secondChosen = new FieldSelectionView({
                model: this.fieldsCollection.at(1),
                name: 'second',
                fields: _.difference(this.parametricCollection.pluck('name'), _.union([this.fieldsCollection.at(0).get('field')], this.selectedParametricValues.pluck('field'))).sort(),
                allowEmpty: true
            });

            this.$parametricSelections.append(this.secondChosen.$el);
            this.secondChosen.render();
        },

        makeSelectionsIfData: function() {
            if (!this.parametricCollection.isEmpty()) {
                this.resolveFieldSelections();
                this.fetchDependentFields();
            }
        },

        resolveFieldSelections: function () {
            var fields = _.difference(this.parametricCollection.pluck('name'), this.selectedParametricValues.pluck('field'));

            var primaryModel = this.fieldsCollection.at(0);
            var secondaryModel = this.fieldsCollection.at(1);

            if (fieldInvalid(primaryModel.get('field'), fields)) {
                primaryModel.set('field', fields.sort()[0]);
                secondaryModel.set('field', '');
            }

            if (fieldInvalid(secondaryModel.get('field'))) {
                secondaryModel.set('field', '');
            }
        },

        fetchDependentFields: function () {
            var first = this.fieldsCollection.at(0).get('field');
            var second = this.fieldsCollection.at(1).get('field');

            if (first) {

                this.dependentParametricCollection.fetch({
                    data: {
                        databases: this.queryModel.get('indexes'),
                        queryText: this.queryModel.get('queryText'),
                        fieldText: this.queryModel.get('fieldText') ? this.queryModel.get('fieldText').toString() : '',
                        minDate: this.queryModel.getIsoDate('minDate'),
                        maxDate: this.queryModel.getIsoDate('maxDate'),
                        minScore: this.queryModel.get('minScore'),
                        fieldNames: second ? [first, second] : [first],
                        stateTokens: this.queryModel.get('stateMatchIds')
                    }
                });
            }
            else {
                this.dependentParametricCollection.reset();
                this.$content.empty();
            }
        },

        updateMessage: function (message) {
            if (message) {
                this.$content.addClass('hide');
                this.$message.text(message);
            } else {
                this.$message.empty();
            }

        },

        toggleContentDisplay: function () {
            this.$content.toggleClass('hide', this.parametricCollection.isEmpty() || this.dependentParametricCollection.isEmpty() || this.noMoreParametricFields());
        },

        noMoreParametricFields: function () {
            return _.isEmpty(this.parametricCollection.reject(function (model) {
                    return this.selectedParametricValues.findWhere({field: model.get('name')});
                }, this), this);
        }
    });

});
