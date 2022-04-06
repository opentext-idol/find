/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
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
    'underscore',
    'backbone',
    'find/app/model/dependent-parametric-collection',
    'i18n!find/nls/bundle',
    'find/app/page/search/results/field-selection-view',
    'text!find/templates/app/page/search/results/parametric-results-view.html',
    'find/app/util/generate-error-support-message',
    'text!find/templates/app/page/loading-spinner.html'
], function(_, Backbone, DependentParametricCollection, i18n, FieldSelectionView,
            template, generateErrorHtml, loadingSpinnerTemplate) {
    'use strict';

    function fieldIsValid(field, fields) {
        return field && _.contains(fields, field);
    }

    function getClickedParameters(data, fields, selectedParameters) {
        if(data.depth !== 0) {
            const parameter = {
                field: fields[data.depth - 1].field,
                displayName: fields[data.depth - 1].displayName,
                value: data.underlyingValue,
                displayValue: data.text,
                type: 'Parametric'
            };
            selectedParameters.push(parameter);

            if(data.parent && data.parent.depth !== 0) {
                getClickedParameters(data.parent, fields, selectedParameters)
            }
        }

        return selectedParameters;
    }

    const SNAPSHOT = 'SNAPSHOT';

    return Backbone.View.extend({
        template: _.template(template),
        loadingHtml: _.template(loadingSpinnerTemplate)({i18n: i18n, large: true}),

        initialize: function(options) {
            this.queryModel = options.queryModel;
            this.savedSearchModel = options.savedSearchModel;
            this.parametricCollection = options.parametricCollection;
            this.selectedParametricValues = options.queryState.selectedParametricValues;

            this.emptyDependentMessage = options.emptyDependentMessage;
            this.emptyMessage = options.emptyMessage;
            this.errorMessageArguments = options.errorMessageArguments;

            this.dependentParametricCollection = options.dependentParametricCollection || new DependentParametricCollection();
            this.fieldsCollection = new Backbone.Collection([{field: '', displayName: ''}, {
                field: '',
                displayName: ''
            }]);

            this.onClick = this.savedSearchModel.get('type') === SNAPSHOT ? _.noop : this.onSavedSearchClick;

            this.model = new Backbone.Model({
                loading: this.parametricCollection.fetching
            });

            this.listenTo(this.fieldsCollection, 'change:field', this.fetchDependentFields);
        },

        update: _.noop,

        render: function() {
            this.$el.html(this.template({
                i18n: i18n,
                loadingHtml: this.loadingHtml
            }));

            this.$loadingSpinner = this.$('.parametric-loading').addClass('hide');
            this.$content = this.$('.parametric-content').addClass('invisible');
            this.$message = this.$('.parametric-view-message');
            this.$errorMessage = this.$('.parametric-view-error-message');
            this.$parametricSelections = this.$('.parametric-selections').addClass('hide');

            this.listenTo(this.fieldsCollection.at(0), 'change:field', this.secondSelection);
            this.listenTo(this.model, 'change:loading', this.toggleLoading);
            this.listenTo(this.parametricCollection, 'error', this.errorHandler);
            this.listenTo(this.parametricCollection, 'sync', this.updateParametricCollection);
            this.listenTo(this.dependentParametricCollection, 'sync', this.updateData);
            this.listenTo(this.dependentParametricCollection, 'error', this.errorHandler);
            this.listenTo(this.selectedParametricValues, 'add remove reset', this.updateSelections);
            this.$parametricSwapButton = this.$('.parametric-swap');
            this.$parametricSwapButton.click(function() {
                this.swapFields();
            }.bind(this));

            this.listenTo(this.fieldsCollection, 'change:field', function() {
                const fieldsPopulated = this.fieldsCollection.every(function(model) {
                    return model.get('field');
                });
                this.$parametricSwapButton.toggleClass('disabled', !fieldsPopulated);
                this.$parametricSwapButton.prop('disabled', !fieldsPopulated);
            });

            this.setLoadingListeners([this.parametricCollection, this.dependentParametricCollection]);
            this.makeSelectionsIfData();
            this.updateSelections();
            this.updateParametricCollection();
        },

        toggleLoading: function() {
            const loading = this.model.get('loading');

            this.$loadingSpinner.toggleClass('hide', !loading);
            this.$content.toggleClass('invisible', loading);
            this.$parametricSelections.toggleClass('hide', this.noMoreParametricFields());
            this.updateMessage();
        },

        swapFields: function() {
            const first = this.fieldsCollection.at(0);
            const second = this.fieldsCollection.at(1);

            first.set(second.attributes, {silent: true});
            second.set(first.previousAttributes(), {silent: true});
            this.updateSelections();
            this.fetchDependentFields();
        },

        errorHandler: function(collection, xhr) {
            if(xhr.status !== 0) {
                this.model.set('loading', false);
                if(xhr.responseJSON) {
                    const messageArguments = _.extend({
                        errorDetails: xhr.responseJSON.message,
                        errorLookup: xhr.responseJSON.backendErrorCode,
                        errorUUID: xhr.responseJSON.uuid,
                        isUserError: xhr.responseJSON.isUserError
                    }, this.errorMessageArguments);

                    this.updateErrorMessage(generateErrorHtml(messageArguments));
                } else {
                    this.updateErrorMessage(generateErrorHtml(this.errorMessageArguments));
                }
            }
        },

        setLoadingListeners: function(collections) {
            _.each(collections, function(collection) {
                this.listenTo(collection, 'request', function() {
                    this.model.set('loading', true);
                }, this);
            }, this);
        },

        onSavedSearchClick: function(data) {
            const selectedParameters = getClickedParameters(data,
                this.fieldsCollection.invoke('pick', 'field', 'displayName'), []);

            // empty value means padding element was clicked on
            if(!_.findWhere(selectedParameters, {value: ''})) {
                this.selectedParametricValues.add(selectedParameters);
            }
        },

        updateParametricCollection: function() {
            const noMoreParametricFields = !!this.noMoreParametricFields();

            this.$parametricSelections.toggleClass('hide', noMoreParametricFields);

            if(noMoreParametricFields) {
                this.model.set('loading', false);
                this.updateMessage(this.emptyMessage);
            } else {
                this.makeSelectionsIfData();
            }
        },

        updateData: function() {
            this.model.set('loading', false);

            if(!(this.parametricCollection.fetching || this.dependentParametricCollection.isEmpty())) {
                this.update();
            } else if(this.dependentParametricCollection.isEmpty()) {
                this.updateMessage(this.emptyDependentMessage);
            }

            this.toggleContentDisplay();
        },

        updateSelections: function() {
            this.firstSelection();
            this.secondSelection();
        },

        firstSelection: function() {
            if(this.firstChosen) {
                this.firstChosen.remove();
            }

            const selectedFieldsAndValues = this.selectedParametricValues.toFieldsAndValues();
            this.firstChosen = new FieldSelectionView({
                model: this.fieldsCollection.at(0),
                name: 'first',
                fields: this.parametricCollection
                    .invoke('pick', 'id', 'displayName')
                    .filter(function(data) {
                        return !selectedFieldsAndValues[data.id];
                    }.bind(this)),
                allowEmpty: false
            });

            this.$parametricSelections.prepend(this.firstChosen.$el);
            this.firstChosen.render();
        },

        secondSelection: function() {
            if(this.secondChosen) {
                this.secondChosen.remove();
            }

            const selectedFieldsAndValues = this.selectedParametricValues.toFieldsAndValues();
            this.secondChosen = new FieldSelectionView({
                model: this.fieldsCollection.at(1),
                name: 'second',
                fields: this.parametricCollection
                    .invoke('pick', 'id', 'displayName')
                    .filter(function(data) {
                        return !(data.id === this.fieldsCollection.at(0).get('field') || selectedFieldsAndValues[data.id]);
                    }.bind(this))
                    .sort(),
                allowEmpty: true
            });

            this.$parametricSelections.append(this.secondChosen.$el);
            this.secondChosen.render();
        },

        makeSelectionsIfData: function() {
            if(!this.parametricCollection.isEmpty()) {
                this.resolveFieldSelections();
                this.updateSelections();
                this.fetchDependentFields();
            }
        },

        resolveFieldSelections: function() {
            const fields = _.difference(this.parametricCollection.pluck('id'),
                this.selectedParametricValues.pluck('field'));

            const primaryModel = this.fieldsCollection.at(0);
            const secondaryModel = this.fieldsCollection.at(1);
            const primaryField = primaryModel.get('field');

            if(!fieldIsValid(primaryField, fields)) {
                primaryModel.set('field', fields.sort()[0]);
                secondaryModel.set('field', '');
            } else if(!fieldIsValid(secondaryModel.get('field'), _.without(fields, primaryField))) {
                secondaryModel.set('field', '');
            }
        },

        fetchDependentFields: function() {
            const primaryField = this.fieldsCollection.at(0).get('field');
            const secondaryField = this.fieldsCollection.at(1).get('field');

            if(primaryField) {
                this.dependentParametricCollection
                    .fetchDependentFields(this.queryModel, primaryField, secondaryField);
            } else {
                this.dependentParametricCollection.reset();
            }
        },

        updateMessage: function(message) {
            this.$errorMessage.empty();
            if(message) {
                this.$content.addClass('invisible');
                this.$message.html(message);
            } else {
                this.$message.empty();
            }
        },

        updateErrorMessage: function(message) {
            this.$message.empty();
            if(message) {
                this.$content.addClass('invisible');
                this.$errorMessage.html(message);
            } else {
                this.$errorMessage.empty();
            }
        },

        toggleContentDisplay: function() {
            this.$content.toggleClass('invisible',
                this.parametricCollection.isEmpty() ||
                this.dependentParametricCollection.isEmpty() ||
                this.noMoreParametricFields());
        },

        noMoreParametricFields: function() {
            return _.isEmpty(this.parametricCollection.reject(function(model) {
                return this.selectedParametricValues.findWhere({field: model.get('id')});
            }, this));
        },

        remove: function() {
            if(this.firstChosen) {
                this.firstChosen.remove();
            }
            if(this.secondChosen) {
                this.secondChosen.remove();
            }

            Backbone.View.prototype.remove.call(this);
        },

        setRouteParams: function(routeParams) {
            const first = this.fieldsCollection.at(0);
            const second = this.fieldsCollection.at(1);

            let changed = false;

            _.each([first, second], function(model, index){
                const fieldId = routeParams[index];
                if (fieldId) {
                    model.set('field', fieldId);
                    changed = true;
                }
            });

            if (changed) {
                this.updateSelections();
            }
        }
    });
});
