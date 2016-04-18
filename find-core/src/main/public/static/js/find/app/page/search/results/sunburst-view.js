define([
    'backbone',
    'find/app/model/dependent-parametric-collection',
    'underscore',
    'jquery',
    'i18n!find/nls/bundle',
    'sunburst/js/sunburst',
    'find/app/page/search/results/field-selection-view',
    'text!find/templates/app/page/search/results/sunburst/sunburst-view.html',
    'text!find/templates/app/page/search/results/sunburst/sunburst-label.html',
    'text!find/templates/app/page/loading-spinner.html'
], function (Backbone, DependentParametricCollection, _, $, i18n, Sunburst, FieldSelectionView, template, labelTemplate, loadingSpinnerTemplate) {

    'use strict';

    var SUNBURST_NAME_ATTR = 'text';
    var SUNBURST_SIZE_ATTR = 'count';

    var sunburstLabelIcon = '<i class="icon-zoom-out"></i>';
    var sunburstLabelTemplate = _.template(labelTemplate);

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

    function drawSunburst($el, data, secondField, onClick) {
        var color = d3.scale.category20c();
        $el.empty();

        return new Sunburst($el, {
            animate: false,
            data: {
                text: i18n['search.sunburst.title'],
                children: data,
                count: _.reduce(_.pluck(data, SUNBURST_SIZE_ATTR), function (a, b) {
                    return a + b;
                })
            },
            i18n: i18n,
            nameAttr: SUNBURST_NAME_ATTR,
            sizeAttr: SUNBURST_SIZE_ATTR,
            colorFn: function (data) {
                if (!data.parent) {
                    return color(data.parent);
                }

                if (data.hidden || data.parent.hidden) return '';

                if (!data.parent.parent) {
                    return data.color = d3.hsl(data[SUNBURST_SIZE_ATTR] ? color(data[SUNBURST_NAME_ATTR]) : 'black');
                }

                var parentColour = data.parent.color;
                var index = data.parent.children.indexOf(data);
                var saturationShift = index / data.parent.children.length;
                return data.color = d3.hsl(parentColour.h, (parentColour.s + saturationShift) % 1, parentColour.l + 0.05);
            },
            labelFormatter: function (data, prevClicked) {
                var zoomedOnRoot = !prevClicked || prevClicked.depth === 0;
                var hoveringCenter = prevClicked ? data === prevClicked.parent : data.depth === 0;

                var templateArguments = {
                    size: data[SUNBURST_SIZE_ATTR],
                    icon: !zoomedOnRoot && hoveringCenter ? sunburstLabelIcon : ''
                };

                if (data[SUNBURST_NAME_ATTR] === '') {
                    templateArguments.name = i18n['search.sunburst.noValue'](secondField);
                    templateArguments.italic = true;
                } else {
                    templateArguments.name = data[SUNBURST_NAME_ATTR];
                    templateArguments.italic = false;
                }

                return sunburstLabelTemplate(templateArguments);
            },
            onClick: onClick
        });
    }

    return Backbone.View.extend({
        template: _.template(template),
        loadingHtml: _.template(loadingSpinnerTemplate)({i18n: i18n, large: true}),

        initialize: function (options) {
            this.queryModel = options.queryModel;
            this.parametricCollection = options.parametricCollection;
            this.selectedParametricValues = options.queryState.selectedParametricValues;

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
                    fieldNames: second ? [first, second] : [first],
                    stateTokens: this.queryModel.get('stateTokens')
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

        update: function () {
            drawSunburst(this.$sunburst, this.dependentParametricCollection.toJSON(), this.fieldsCollection.at(1).get('field'), _.bind(this.onClick, this));
        },

        updateSelections: function() {
            this.firstSelection();
            this.secondSelection();
        },

        render: function () {
            this.$el.html(this.template({
                i18n: i18n,
                loadingHtml: this.loadingHtml
            }));

            this.$loadingSpinner = this.$('.sunburst-loading')
                .addClass('hide');

            this.$sunburst = this.$('.sunburst')
                .addClass('hide');

            this.$message = this.$('.sunburst-view-message');
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

            // Show if loading either collection
            this.$loadingSpinner.toggleClass('hide', !(parametricLoading || dependentParametricLoading));

            // Show if not loading parametric collection and no error on parametric collection
            this.$parametricSelections.toggleClass('hide', parametricCollectionState !== collectionState.DATA);

            // Show if not loading and not failure
            this.$sunburst.toggleClass('hide', dependentParametricCollectionState !== collectionState.DATA || parametricCollectionState !== collectionState.DATA);

            this.updateMessage(error, empty);
        },

        updateMessage: function (error, empty) {
            var message = '';

            if (error) {
                message = i18n['search.resultsView.sunburst.error.query'];
            } else if (empty) {
                message = i18n['search.resultsView.sunburst.error.noParametricValues'];
            }

            this.$message.text(message);
        },

        onClick: function(data) {
            var selectedParameters = [{field: this.fieldsCollection.at(data.depth - 1).get('field'), value: data.text}];
            
            if (data.depth === 2) {
                var parentParameter = {field: this.fieldsCollection.at(0).get('field'), value: data.parent.text};
                selectedParameters.push(parentParameter);

            }
            this.selectedParametricValues.add(selectedParameters)
        }
    });

});
