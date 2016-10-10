/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
define([
    'backbone',
    'find/app/model/dependent-parametric-collection',
    'underscore',
    'jquery',
    'i18n!find/nls/bundle',
    'find/app/page/search/results/field-selection-view',
    'text!find/templates/app/page/search/results/parametric-results-view.html',
    'text!find/templates/app/page/search/results/accuracy-warning.html',
    'find/app/util/generate-error-support-message',
    'text!find/templates/app/page/loading-spinner.html'
], function(Backbone, DependentParametricCollection, _, $, i18n, FieldSelectionView, template, accuracyTemplate, generateErrorHtml, loadingSpinnerTemplate) {
    'use strict';

    var fieldInvalid = function(field, fields) {
        return !field || !_.contains(fields, field);
    };

    function getClickedParameters(data, fields, selectedParameters) {
        if(data.depth !== 0) {
            var parameter = {field: fields[data.depth - 1], value: data.text};
            selectedParameters.push(parameter);

            if(data.parent && data.parent.depth !== 0) {
                getClickedParameters(data.parent, fields, selectedParameters)
            }
        }

        return selectedParameters;
    }

    var SNAPSHOT = 'SNAPSHOT';

    return Backbone.View.extend({
        template: _.template(template),
        accuracyHtml: _.template(accuracyTemplate)({warning: i18n['search.parametric.accuracy.warning']}),
        loadingHtml: _.template(loadingSpinnerTemplate)({i18n: i18n, large: true}),

        initialize: function(options) {
            this.queryModel = options.queryModel;
            this.savedSearchModel = options.savedSearchModel;
            this.parametricCollection = options.restrictedParametricCollection;
            this.selectedParametricValues = options.queryState.selectedParametricValues;

            this.emptyDependentMessage = options.emptyDependentMessage;
            this.emptyMessage = options.emptyMessage;
            this.errorMessageArguments = options.errorMessageArguments;

            this.dependentParametricCollection = options.dependentParametricCollection || new DependentParametricCollection();
            this.fieldsCollection = new Backbone.Collection([{field: ''}, {field: ''}]);

            this.model = new Backbone.Model({
                loading: this.parametricCollection.fetching
            });

            this.listenTo(this.fieldsCollection, 'change:field', this.fetchDependentFields);
        },

        update: $.noop,

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

            this.$accuracyWarning = $(this.accuracyHtml);
            this.$parametricSelections.append(this.$accuracyWarning);
            this.$accuracyWarning.addClass('hide');

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

            this.onClick = this.savedSearchModel.get('type') !== SNAPSHOT ? this.onSavedSearchClick : _.noop;

            this.updateParametricCollection();
        },

        toggleLoading: function() {
            this.$loadingSpinner.toggleClass('hide', !this.model.get('loading'));
            this.$content.toggleClass('invisible', this.model.get('loading'));
            this.$parametricSelections.toggleClass('hide', this.noMoreParametricFields());
            this.updateMessage();
        },

        errorHandler: function(collection, xhr) {
            if(xhr.status !== 0) {
                this.model.set('loading', false);
                if(xhr.responseJSON) {
                    var messageArguments = _.extend({
                        errorDetails: xhr.responseJSON.message,
                        errorLookup: xhr.responseJSON.backendErrorCode,
                        errorUUID: xhr.responseJSON.uuid
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
                }, this)
            }, this)
        },

        onSavedSearchClick: function(data) {
            var selectedParameters = getClickedParameters(data, this.fieldsCollection.pluck('field'), []);

            // empty value means padding element was clicked on
            if(!_.findWhere(selectedParameters, {value: ''})) {
                this.selectedParametricValues.add(selectedParameters)
            }
        },

        updateParametricCollection: function() {
            if(!this.parametricCollection.isEmpty() && !this.noMoreParametricFields()) {
                this.$parametricSelections.removeClass('hide');
                this.makeSelectionsIfData();
            } else {
                this.model.set('loading', false);
                this.$parametricSelections.addClass('hide');
                this.updateMessage(this.emptyMessage);
            }
        },

        updateData: function() {
            this.model.set('loading', false);

            if(!this.parametricCollection.fetching && !this.dependentParametricCollection.isEmpty()) {
                this.$accuracyWarning.toggleClass("hide", this.dependentParametricCollection.accurateCounts);
                this.update();
            } else if(this.dependentParametricCollection.isEmpty()) {
                this.model.set('loading', false);
                this.updateMessage(this.emptyDependentMessage)
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

            this.firstChosen = new FieldSelectionView({
                model: this.fieldsCollection.at(0),
                name: 'first',
                fields: _.difference(this.parametricCollection.pluck('name'), this.selectedParametricValues.pluck('field')).sort(),
                allowEmpty: false
            });

            this.$parametricSelections.prepend(this.firstChosen.$el);
            this.firstChosen.render();
        },

        secondSelection: function() {
            if(this.secondChosen) {
                this.secondChosen.remove();
            }

            this.secondChosen = new FieldSelectionView({
                model: this.fieldsCollection.at(1),
                name: 'second',
                fields: _.difference(this.parametricCollection.pluck('name'), _.union([this.fieldsCollection.at(0).get('field')], this.selectedParametricValues.pluck('field'))).sort(),
                allowEmpty: true
            });

            if (this.firstChosen) {
                this.firstChosen.$el.after(this.secondChosen.$el);
            }
            else {
                this.$parametricSelections.prepend(this.secondChosen.$el);
            }
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
            var fields = _.difference(this.parametricCollection.pluck('name'), this.selectedParametricValues.pluck('field'));

            var primaryModel = this.fieldsCollection.at(0);
            var secondaryModel = this.fieldsCollection.at(1);

            if(fieldInvalid(primaryModel.get('field'), fields)) {
                primaryModel.set('field', fields.sort()[0]);
                secondaryModel.set('field', '');
            }
            else if(fieldInvalid(secondaryModel.get('field'))) {
                secondaryModel.set('field', '');
            }
        },

        fetchDependentFields: function() {
            var first = this.fieldsCollection.at(0).get('field');
            var second = this.fieldsCollection.at(1).get('field');

            if(first) {

                this.dependentParametricCollection.fetch({
                    data: {
                        databases: this.queryModel.get('indexes'),
                        queryText: this.queryModel.get('queryText'),
                        field_matches: this.queryModel.getFieldMatches(),
                        field_ranges: this.queryModel.getFieldRanges(),
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
            }
        },

        updateMessage: function(message) {
            this.$errorMessage.empty();
            if(message) {
                this.$content.addClass('invisible');
                this.$message.empty().append(message);
            } else {
                this.$message.empty();
            }
        },

        updateErrorMessage: function(message) {
            this.$message.empty();
            if(message) {
                this.$content.addClass('invisible');
                this.$errorMessage.empty().append(message);
            } else {
                this.$errorMessage.empty();
            }
        },

        toggleContentDisplay: function() {
            this.$content.toggleClass('invisible', this.parametricCollection.isEmpty() || this.dependentParametricCollection.isEmpty() || this.noMoreParametricFields());
        },

        noMoreParametricFields: function() {
            return _.isEmpty(this.parametricCollection.reject(function(model) {
                return this.selectedParametricValues.findWhere({field: model.get('name')});
            }, this), this);
        }
    });
});
