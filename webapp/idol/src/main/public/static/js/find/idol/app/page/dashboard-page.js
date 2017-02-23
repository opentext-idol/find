/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    'js-whatever/js/base-page',
    'find/app/vent',
    'find/idol/app/page/dashboard/widget-registry',
    './dashboard/widgets/widget-not-found',
    './dashboard/update-tracker-model'
], function(_, $, BasePage, vent, widgetRegistry, WidgetNotFoundWidget, UpdateTrackerModel) {
    'use strict';

    return BasePage.extend({
        className: 'dashboard',

        initialize: function(options) {
            _.bindAll(this, 'update');

            this.dashboardName = options.dashboardName;
            this.updateInterval = 1000 * options.updateInterval;
            this.sidebarModel = options.sidebarModel;

            this.widgetViews = _.map(options.widgets, function(widget) {
                const widgetDefinition = widgetRegistry(widget.type);
                const WidgetConstructor = widgetDefinition
                    ? widgetDefinition.Constructor
                    : WidgetNotFoundWidget;

                const widgetOptions = _.extend({
                    updateInterval: this.updateInterval
                }, widget);

                return {
                    view: new WidgetConstructor(widgetOptions),
                    position: {
                        x: widget.x,
                        y: widget.y,
                        width: widget.width,
                        height: widget.height
                    }
                };
            }, this);

            this.widthPerUnit = 100 / options.width;
            this.heightPerUnit = 100 / options.height;
        },

        render: function() {
            this.$el.empty();

            _.each(this.widgetViews, function(widget) {
                const $div = this.generateWidgetDiv(widget.position);

                // Need $.when() because not every widget has a savedSearchPromise.
                // $.when(undefined) returns a resolved promise
                $.when(widget.view.savedSearchPromise).always(function() {
                    $.when(widget.view.initialiseWidgetPromise).always(function() {
                        this.$el.append($div);
                        widget.view.setElement($div).render();
                    }.bind(this));// TODO handle failure
                }.bind(this));// TODO handle failure
            }.bind(this));

            this.listenTo(vent, 'vent:resize', this.onResize);
            this.listenTo(this.sidebarModel, 'change:collapsed', this.onResize);

            if(this.updateInterval) {
                setInterval(this.update, this.updateInterval);
            }
        },

        generateWidgetDiv: function(position) {
            const widgetElement = $('<div class="widget p-xs"></div>');
            widgetElement.css({
                'left': 'calc(' + position.x * this.widthPerUnit + '% + 20px)',
                'top': 'calc(' + position.y * this.heightPerUnit + '% + 20px)',
                'width': 'calc(' + position.width * this.widthPerUnit + '% - 10px)',
                'height': 'calc(' + position.height * this.heightPerUnit + '% - 10px)'
            });

            return widgetElement;
        },

        onResize: function() {
            if(this.isVisible()) {
                _.each(this.widgetViews, function(widget) {
                    widget.view.onResize();
                });
            }
        },

        update: function() {
            if(this.isVisible()) {
                // cancel pending update
                if(this.updateTracker && !this.updateTracker.get('complete')) {
                    this.updateTracker.set('cancelled', true);
                    this.stopListening(this.updateTracker);
                }

                // set up tracker
                this.updateTracker = new UpdateTrackerModel();

                // update views
                const updatingViews = _.chain(this.widgetViews)
                    .pluck('view')
                    .filter(function(view) {
                        return view.savedSearch
                            ? view.isUpdating() && view.savedSearch.type === 'QUERY'
                            : view.isUpdating();
                    })
                    .value();

                // don't set up this listener if no work to do
                if(updatingViews.length > 0) {
                    this.updateTracker.set('total', updatingViews.length);

                    _.each(updatingViews, function(view) {
                        view.update(this.updateTracker)
                    }, this);

                    // handle completion
                    this.listenTo(this.updateTracker, 'change:count', function(model, count) {
                        if(count === updatingViews.length) {
                            // publish completion
                            this.updateTracker.set('complete', true);
                            this.stopListening(this.updateTracker);
                        }
                    });
                }

                this.onResize();
            }
        }
    });
});
