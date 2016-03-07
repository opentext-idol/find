define([
    'backbone',
    'find/app/model/dependent-parametric-collection',
    'underscore',
    'jquery',
    'i18n!find/nls/bundle',
    'sunburst/js/sunburst',
    'text!find/templates/app/page/search/results/sunburst-view.html',
    'text!find/templates/app/page/loading-spinner.html',
    'chosen'
], function(Backbone, DependentParametricCollection, _, $, i18n, Sunburst, template, loadingSpinnerTemplate) {

    'use strict';

    var emptyOptionHtml = '<option value=""></option>';
    var optionTemplate = _.template('<option value="<%-field%>"><%-field%></option>');

    var SUNBURST_NAME_ATTR = 'text';
    var SUNBURST_SIZE_ATTR = 'count';

    var sunburstLabelIcon = '<i class="icon-zoom-out"></i>';
    var sunburstLabelTemplate = _.template('<div style="font-size:14px;font-weight:bold;"><%=icon%><%-name%></div><%-size%>');

    function drawSunburst($el, data) {
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

                return sunburstLabelTemplate({
                    name: data[SUNBURST_NAME_ATTR],
                    size: data[SUNBURST_SIZE_ATTR],
                    icon: !zoomedOnRoot && hoveringCenter ? sunburstLabelIcon : ''
                });
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

        update: function() {
            this.$sunburst.empty();

            if (!this.dependentParametricCollection.isEmpty()) {
                drawSunburst(this.$sunburst, this.dependentParametricCollection.toJSON());
                
                this.$sunburst.removeClass('hide');
                this.toggleError('');
            }

            this.toggleLoadingSpinner(false);
        },

        emptyDropdown: function($dropdown) {
            $dropdown
                .empty()
                .append($dropdown.hasClass('first-parametric') ? '' : emptyOptionHtml)
                .trigger('chosen:updated');
        },

        populateDropDown: function($dropdown, fields) {
            this.emptyDropdown($dropdown);

            var html = _.map(fields.sort(), function(field) {
                return optionTemplate({field: field});
            });

            $dropdown
                .append(html)
                .chosen({width: '20%'})
                .trigger('chosen:updated');
        },

        resetView: function() {
            this.emptyDropdown(this.$firstChosen);
            this.emptyDropdown(this.$secondChosen);
            this.$sunburst.empty().addClass('hide');

            this.toggleLoadingSpinner(true);
            this.toggleError('');
        },

        firstPass: function() {
            this.$sunburst.addClass('hide');

            var val = this.$firstChosen.val();
            this.fetchDependentFields(val);

            this.populateDropDown(this.$secondChosen, _.without(this.parametricCollection.pluck('name'), val));

            this.$secondChosen.removeClass('hide');
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
            this.$firstChosen = this.$('.first-parametric');
            this.$secondChosen = this.$('.second-parametric');

            this.populateDropDown(this.$firstChosen, this.parametricCollection.pluck('name'));
            this.firstPass();

            this.$firstChosen.change(_.bind(this.firstPass, this));

            this.$secondChosen.change(_.bind(function() {
                this.$sunburst.addClass('hide');
                this.fetchDependentFields(this.$firstChosen.val(), this.$secondChosen.val());
            }, this));

            this.listenTo(this.parametricCollection, 'request', this.resetView);

            this.listenTo(this.parametricCollection, 'sync', function() {
                this.populateDropDown(this.$firstChosen, this.parametricCollection.pluck('name'));
                this.firstPass();

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
