define([
    'find/app/page/search/results/parametric-results-view',
    'find/app/model/dependent-parametric-collection',
    'underscore',
    'jquery',
    'i18n!find/nls/bundle',
    'sunburst/js/sunburst',
    'find/app/page/search/results/field-selection-view',
    'text!find/templates/app/page/search/results/sunburst/sunburst-view.html',
    'text!find/templates/app/page/search/results/sunburst/sunburst-label.html',
    'text!find/templates/app/page/loading-spinner.html'
], function (ParametricResultsView, DependentParametricCollection, _, $, i18n, Sunburst, FieldSelectionView, template, labelTemplate, loadingSpinnerTemplate) {
    'use strict';

    var SUNBURST_NAME_ATTR = 'text';
    var SUNBURST_SIZE_ATTR = 'count';
    var SUNBURST_FILTER_NUMBER = 'hiddenFilterCount';
    var STROKE_COLOUR = '#f0f0f0';

    var sunburstLabelIcon = '<i class="icon-zoom-out"></i>';
    var sunburstLabelTemplate = _.template(labelTemplate);

    function drawSunburst($el, data, onClick) {
        var color = d3.scale.category20c();
        $el.empty();

        this.sunburst = new Sunburst($el, {
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
            strokeColour: STROKE_COLOUR,
            colorFn: function (data) {
                if (!data.parent) {
                    // set the centre of the sunburst to always be white
                    return 'white';
                }

                if (data.hidden || data.parent.hidden) {
                    return STROKE_COLOUR;
                }

                if (!data.parent.parent) {
                    return data.color = data[SUNBURST_SIZE_ATTR] ? color(data.parent.children.indexOf(data)) : 'black';
                }

                return data.color = color(data[SUNBURST_NAME_ATTR]);
            },
            labelFormatter: function (data, prevClicked) {
                var zoomedOnRoot = !prevClicked || prevClicked.depth === 0;
                var hoveringCenter = prevClicked ? data === prevClicked.parent : data.depth === 0;

                var templateArguments = {
                    size: data[SUNBURST_SIZE_ATTR],
                    icon: !zoomedOnRoot && hoveringCenter ? sunburstLabelIcon : '',
                    noVal: false
                };

                if (data[SUNBURST_NAME_ATTR] === '') {
                    templateArguments.name = i18n['search.sunburst.noValue'](data[SUNBURST_FILTER_NUMBER]);
                    templateArguments.italic = true;
                    templateArguments.noVal = true;
                } else {
                    templateArguments.name = data[SUNBURST_NAME_ATTR];
                    templateArguments.italic = false;
                }

                return sunburstLabelTemplate(templateArguments);
            },
            onClick: onClick,
            outerRingAnimateSize: 15,
            hoverAnimation: function (d, arc, outerRingAnimateSize, arcEls, arcData, paper) {
                _.chain(_.zip(arcData, arcEls))
                    .filter(function(dataEl) {
                        var data = dataEl[0];

                        // TODO Assumes depth=2 is the outer ring - will need to change if this changes
                        return data.text !== '' && data.depth === 2 && data.text === d.text;
                    })
                    .each(function(dataEl) {
                        var el = dataEl[1];
                        paper.set(el).animate({path: arc(outerRingAnimateSize)(dataEl[0])}, 100);
                    });
            }
        });

        return this.sunburst;
    }

    return ParametricResultsView.extend({
        template: _.template(template),
        loadingHtml: _.template(loadingSpinnerTemplate)({i18n: i18n, large: true}),

        update: function () {
            drawSunburst.call(this, this.$sunburst, this.dependentParametricCollection.toJSON(), _.bind(this.onClick, this));
        },

        render: function () {
            ParametricResultsView.prototype.render.apply(this, arguments);

            $(window).resize(_.bind(function() {
                if (this.sunburst) {
                    this.sunburst.resize();
                }
            }, this));
        },

        uiUpdate: function () {
            ParametricResultsView.prototype.uiUpdate.apply(this, arguments);

            if (this.sunburst) {
                this.sunburst.resize();
            }
        }
        
    });

});
