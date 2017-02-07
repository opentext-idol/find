/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
define([
    'find/app/page/search/results/parametric-results-view',
    'underscore',
    'jquery',
    'i18n!find/nls/bundle',
    'sunburst/js/sunburst',
    'find/app/util/generate-error-support-message',
    'text!find/templates/app/page/search/results/sunburst/sunburst-label.html'
], function(ParametricResultsView, _, $, i18n, Sunburst, generateErrorHtml, labelTemplate) {
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
            i18n: i18n,
            nameAttr: SUNBURST_NAME_ATTR,
            sizeAttr: SUNBURST_SIZE_ATTR,
            strokeColour: STROKE_COLOUR,
            comparator: null,
            onClick: onClick,
            outerRingAnimateSize: 15,
            data: {
                text: i18n['search.sunburst.title'],
                children: data,
                count: _.reduce(_.pluck(data, SUNBURST_SIZE_ATTR), function(a, b) {
                    return a + b;
                })
            },
            colorFn: function(data) {
                if(!data.parent) {
                    // set the centre of the sunburst to always be white
                    return 'white';
                }

                if(data.hidden || data.parent.hidden) {
                    return STROKE_COLOUR;
                }

                if(!data.parent.parent) {
                    return data.color = data[SUNBURST_SIZE_ATTR] ? color(data.parent.children.indexOf(data)) : 'black';
                }

                return data.color = color(data[SUNBURST_NAME_ATTR]);
            },
            labelFormatter: function(data, prevClicked) {
                var zoomedOnRoot = !prevClicked || prevClicked.depth === 0;
                var hoveringCenter = prevClicked ? data === prevClicked.parent : data.depth === 0;

                var templateArguments = {
                    size: data[SUNBURST_SIZE_ATTR],
                    icon: !zoomedOnRoot && hoveringCenter ? sunburstLabelIcon : '',
                    noVal: false
                };

                if(data[SUNBURST_NAME_ATTR] === '') {
                    templateArguments.name = i18n['search.sunburst.noValue'](data[SUNBURST_FILTER_NUMBER]);
                    templateArguments.italic = true;
                    templateArguments.noVal = true;
                } else {
                    templateArguments.name = data[SUNBURST_NAME_ATTR];
                    templateArguments.italic = false;
                }

                return sunburstLabelTemplate(templateArguments);
            },
            hoverAnimation: function(d, arc, outerRingAnimateSize, arcEls, arcData, paper) {
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
        initialize: function(options) {
            ParametricResultsView.prototype.initialize.call(this, _.defaults({
                emptyDependentMessage: i18n['search.resultsView.sunburst.error.noDependentParametricValues'],
                emptyMessage: generateErrorHtml({errorLookup: 'emptySunburstView'}),
                errorMessageArguments: {messageToUser: i18n['search.resultsView.sunburst.error.query']}
            }, options));
        },

        update: function() {
            if (!this.parametricCollection.isEmpty()) {
                drawSunburst.call(this, this.$content, this.dependentParametricCollection.toJSON(), _.bind(this.onClick, this));

                var noValidChildren = _.chain(this.dependentParametricCollection.pluck('children'))
                    .compact()
                    .flatten()
                    .isEmpty()
                    .value();

                if (this.fieldsCollection.at(1).get('field') !== '' && noValidChildren) {
                    this.$message.text(i18n['search.resultsView.sunburst.error.noSecondFieldValues']);
                } else {
                    this.$message.empty();
                }
            }
        },

        render: function() {
            ParametricResultsView.prototype.render.apply(this, arguments);

            this.$('.col-md-12').prepend('<a class="btn btn-default pull-right sunburst-pptx" href="#"><i class="hp-icon hp-document-download"></i> PPTX</a>');

            this.$content.addClass('sunburst');

            this.$el.on('click', '.sunburst-pptx', _.bind(function(evt){
                evt.preventDefault();

                var $form = $('<form class="hide" enctype="multipart/form-data" method="post" target="_blank" action="api/bi/export/ppt/sunburst"><input name="title"><textarea name="data"></textarea><input type="submit"></form>');

                $form[0].title.value = i18n['search.resultsView.sunburst.breakdown.by'](this.fieldsCollection.at(0).get('displayValue'));

                var categories = [];
                var values = [];

                this.dependentParametricCollection.each(function(model){
                    categories.push(model.get('text') || i18n['search.resultsView.sunburst.others']);
                    values.push(model.get('count'));
                });

                $form[0].data.value = JSON.stringify({
                    categories: categories,
                    values: values
                });

                $form.appendTo(document.body).submit().remove();
            }, this))

            $(window).resize(_.bind(function() {
                if(this.sunburst) {
                    this.sunburst.resize();
                }
            }, this));
        }
    });
});
