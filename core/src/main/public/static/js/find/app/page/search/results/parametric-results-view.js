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

    var collectionState = {
        LOADING: 'LOADING',
        ERROR: 'ERROR',
        EMPTY: 'EMPTY',
        DATA: 'DATA'
    };

    var getCollectionState = function (collection) {
        if (collection.fetching) {
            return collectionState.LOADING;
        }
        else if (collection.error) {
            return collectionState.ERROR;
        }
        else if (collection.isEmpty()) {
            return collectionState.EMPTY;
        }
        else {
            return collectionState.DATA;
        }
    };

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
                dependentParametricCollectionState: collectionState.EMPTY,
                parametricCollectionState: getCollectionState(this.parametricCollection)
            });

            this.listenTo(this.fieldsCollection, 'change:field', this.fetchDependentFields);

            this.listenTo(this.parametricCollection, 'request sync error', function () {
                this.model.set('parametricCollectionState', getCollectionState(this.parametricCollection));
            });

            this.listenTo(this.dependentParametricCollection, 'request sync error', function () {
                this.model.set('dependentParametricCollectionState', getCollectionState(this.dependentParametricCollection));
            });
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

        update: $.noop,

        updateSelections: function() {
            this.firstSelection();
            this.secondSelection();
        },

        render: function () {
            this.$el.html(this.template({
                i18n: i18n,
                loadingHtml: this.loadingHtml
            }));

            this.$loadingSpinner = this.$('.parametric-loading')
                .addClass('hide');

            this.$content = this.$('.parametric-content')
                .addClass('hide');

            this.$message = this.$('.parametric-view-message');
            this.$parametricSelections = this.$('.parametric-selections');

            this.listenTo(this.fieldsCollection.at(0), 'change:field', this.secondSelection);

            this.makeSelectionsIfData();
            this.listenTo(this.model, 'change:parametricCollectionState', this.makeSelectionsIfData);

            this.updateIfData();
            this.listenTo(this.model, 'change:dependentParametricCollectionState', this.updateIfData);

            this.updateSelections();
            this.listenTo(this.parametricCollection, 'sync', this.updateSelections);
            this.listenTo(this.selectedParametricValues, 'add remove reset', this.updateSelections);

            this.uiUpdate();
            this.listenTo(this.model, 'change', this.uiUpdate);
        },

        makeSelectionsIfData: function() {
            if (this.model.get('parametricCollectionState') === collectionState.DATA) {
                this.resolveFieldSelections();
                this.fetchDependentFields();
            }
        },

        updateIfData: function() {
            if (this.model.get('parametricCollectionState') !== collectionState.LOADING && this.model.get('dependentParametricCollectionState') === collectionState.DATA) {
                this.update();
            }
        },

        uiUpdate: function () {
            var parametricCollectionState = this.model.get('parametricCollectionState');
            var dependentParametricCollectionState = this.model.get('dependentParametricCollectionState');

            var parametricLoading = parametricCollectionState === collectionState.LOADING;
            var dependentParametricLoading = dependentParametricCollectionState === collectionState.LOADING;

            var empty = this.model.get('parametricCollectionState') === collectionState.EMPTY;
            var error = parametricCollectionState === collectionState.ERROR || (!parametricLoading && dependentParametricCollectionState === collectionState.ERROR);
            var emptyDependentParametric = dependentParametricCollectionState === collectionState.EMPTY;

            // Show if loading either collection
            this.$loadingSpinner.toggleClass('hide', !(parametricLoading || dependentParametricLoading));

            // Show if not loading parametric collection and no error on parametric collection
            this.$parametricSelections.toggleClass('hide', parametricCollectionState !== collectionState.DATA);

            // Show if not loading and not failure
            this.$content.toggleClass('hide', dependentParametricCollectionState !== collectionState.DATA || parametricCollectionState !== collectionState.DATA);

            this.updateMessage(error, empty, emptyDependentParametric);
        },

        updateMessage: function (error, empty, emptyDependentParametric) {
            var message = '';

            if (error) {
                message = this.errorMessage;
            } else if (empty) {
                message = this.emptyMessage;
            } else if (emptyDependentParametric) {
                message = this.emptyDependentMessage;
            }

            this.$message.text(message);
        },

        onClick: function(data) {
            var selectedParameters = getClickedParameters(data, this.fieldsCollection.pluck('field'), []);

            // empty value means padding element was clicked on
            if (!_.findWhere(selectedParameters, {value: ''})) {
                this.selectedParametricValues.add(selectedParameters)
            }
        }
        
    });

});
