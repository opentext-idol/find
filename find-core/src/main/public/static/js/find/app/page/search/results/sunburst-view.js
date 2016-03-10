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
], function(Backbone, DependentParametricCollection, _, $, i18n, Sunburst, FieldSelectionView, template, labelTemplate, loadingSpinnerTemplate) {

    'use strict';

    var SUNBURST_NAME_ATTR = 'text';
    var SUNBURST_SIZE_ATTR = 'count';

    var sunburstLabelIcon = '<i class="icon-zoom-out"></i>';
    var sunburstLabelTemplate = _.template(labelTemplate);

    function drawSunburst($el, data, secondField) {
        var color = d3.scale.category20c();

        return new Sunburst($el, {
            data: {
                text: i18n['search.sunburst.title'],
                children: data,
                count: _.reduce(_.pluck(data, SUNBURST_SIZE_ATTR), function(a, b) {return a + b;})
            },
            i18n: i18n,
            nameAttr: SUNBURST_NAME_ATTR,
            sizeAttr: SUNBURST_SIZE_ATTR,
            colorFn: function(data) {
                if (!data.parent) {
                    return color(data.parent);
                }

                if (!data.parent.parent) {
                    return data.color = d3.hsl(data[SUNBURST_SIZE_ATTR] ? color(data[SUNBURST_NAME_ATTR]) : 'black');
                }

                var parentColour = data.parent.color;
                var index = data.parent.children.indexOf(data);
                var saturationShift = index / data.parent.children.length;
                return data.color = d3.hsl(parentColour.h, (parentColour.s + saturationShift) % 1, parentColour.l + 0.05);
            },
            labelFormatter: function(data, prevClicked) {
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
            }
        });
    }

    return Backbone.View.extend({
        template: _.template(template),
        loadingHtml: _.template(loadingSpinnerTemplate)({i18n: i18n, large: true}),

        initialize: function(options) {
            this.queryModel = options.queryModel;
            this.parametricCollection = options.parametricCollection;
            this.dependentParametricCollection = new DependentParametricCollection();

            this.firstFieldModel = new Backbone.Model();
            this.secondFieldModel = new Backbone.Model();

            this.listenTo(this.firstFieldModel, 'change:field', _.bind(this.firstPass, this));

            this.listenTo(this.secondFieldModel, 'change:field', _.bind(function() {
                this.$sunburst.addClass('hide');
                this.fetchDependentFields(this.firstFieldModel.get('field'), this.secondFieldModel.get('field'));
            }, this));
        },

        fetchDependentFields: function(first, second) {
            if (!second) this.$sunburst.empty();
            this.toggleLoadingSpinner(true);
            this.toggleError('');

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

        firstDropdown: function() {
            if(this.firstChosen) {
                this.firstChosen.remove();
            }

            if (this.secondChosen) {
                this.secondChosen.remove();
            }

            var fields = this.parametricCollection.pluck('name').sort();

            this.firstFieldModel.set('field', fields[0]);
            this.secondFieldModel.set('field', '');

            this.firstChosen = new FieldSelectionView({
                model: this.firstFieldModel,
                name: 'first',
                fields: fields,
                allowEmpty: false
            });

            this.$parametricSelections.prepend(this.firstChosen.$el);
            this.firstChosen.render();
        },

        update: function() {
            this.$sunburst.empty();

            if (!this.dependentParametricCollection.isEmpty()) {
                drawSunburst(this.$sunburst, this.dependentParametricCollection.toJSON(), this.secondFieldModel.get('field'));

                this.$sunburst.removeClass('hide');
                this.toggleError('');
            }

            this.toggleLoadingSpinner(false);
        },

        resetView: function() {
            this.$sunburst.empty().addClass('hide');

            this.toggleLoadingSpinner(true);
            this.toggleError('');
        },

        firstPass: function() {
            this.$sunburst.addClass('hide');

            this.fetchDependentFields(this.firstFieldModel.get('field'));

            if(this.secondChosen) {
                this.secondChosen.remove();
            }

            this.secondFieldModel.set('field', '');

            this.secondChosen = new FieldSelectionView({
                model: this.secondFieldModel,
                name: 'second',
                fields: _.without(this.parametricCollection.pluck('name'), this.firstFieldModel.get('field')).sort(),
                allowEmpty: true
            });

            this.$parametricSelections.append(this.secondChosen.$el);
            this.secondChosen.render();
        },

        toggleError: function(message) {
            this.$error.text(message);
            this.$('.parametric-selections').toggleClass('hide', Boolean(message));
        },

        toggleLoadingSpinner: function(toggle) {
            this.$loadingSpinner.toggleClass('hide', !toggle);
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n,
                loadingHtml: this.loadingHtml
            }));

            this.$loadingSpinner = this.$('.sunburst-loading')
                .addClass('hide');

            this.$error = this.$('.sunburst-view-error');
            this.$sunburst = this.$('.sunburst');
            this.$sunburst.addClass('hide');

            this.$parametricSelections = this.$('.parametric-selections');

            if(!this.parametricCollection.isEmpty()) {
                this.firstDropdown();
            }

            this.listenTo(this.parametricCollection, 'request', this.resetView);

            this.listenTo(this.parametricCollection, 'sync', function() {
                this.firstDropdown();

                if (this.parametricCollection.isEmpty()) {
                    this.toggleLoadingSpinner(false);
                    this.toggleError(i18n['search.resultsView.sunburst.error.noParametricValues']);
                } else {
                    this.toggleError('');
                }
            });

            this.listenTo(this.dependentParametricCollection, 'sync', this.update);
        }
    });

});
