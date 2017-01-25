/*
 * Copyright 2014-2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'js-whatever/js/base-page',
    'backbone',
    'find/app/vent',
    'find/idol/app/page/dashboard/widget-registry',
    './dashboard/widgets/widget-not-found',
    './dashboard/update-tracker-model',
    'text!find/idol/templates/app/page/dashboard-page.html'
], function (_, BasePage, Backbone, vent, widgetRegistry, WidgetNotFoundWidget, UpdateTrackerModel, template) {
    'use strict';

    return BasePage.extend({

        className: 'dashboard',

        template: _.template(template),

        initialize: function (options) {
            _.bindAll(this, 'update');

            this.dashboardName = options.dashboardName;
            this.updateInterval = 1000 * options.updateInterval;
            this.sidebarModel = options.sidebarModel;

            this.widgetViews = _.map(options.widgets, function (widget) {
                const widgetDefinition = widgetRegistry(widget.type);
                const WidgetConstructor = widgetDefinition ? widgetDefinition.Constructor : WidgetNotFoundWidget;
                
                return {
                    view: new WidgetConstructor(widget),
                    position: {
                        x: widget.x,
                        y: widget.y,
                        width: widget.width,
                        height: widget.height
                    }
                };
            });

            this.widthPerUnit = 100 / options.width;
            this.heightPerUnit = 100 / options.height;
        },

        render: function () {
            this.$el.html(this.template({
                dashboardName: this.dashboardName
            }));

            _.each(this.widgetViews, function (widget) {
                const $div = this.generateWidgetDiv(widget.position);
                this.$el.append($div);
                widget.view.setElement($div).render();
            }.bind(this));

            this.listenTo(vent, 'vent:resize', this.onResize);
            this.listenTo(this.sidebarModel, 'change:collapsed', this.onResize);

            if (this.updateInterval) {
                setInterval(this.update, this.updateInterval);
            }
        },

        generateWidgetDiv: function (position) {
            const widgetElement = $('<div class="widget"></div>');
            widgetElement.css({
                'left': position.x * this.widthPerUnit + '%',
                'top': position.y * this.heightPerUnit + '%',
                'width': position.width * this.widthPerUnit + '%',
                'height': position.height * this.heightPerUnit + '%'
            });

            return widgetElement;
        },

        onResize: function() {
            _.each(this.widgetViews, function(widget) {
                widget.view.onResize();
            });
        },

        update: function() {
            // cancel pending update
            if (this.updateTracker && !this.updateTracker.get('complete')) {
                this.updateTracker.set('cancelled', true);
                this.stopListening(this.updateTracker);
            }

            // set up tracker
            this.updateTracker = new UpdateTrackerModel();

            // update views
            const updatingViews = _.chain(this.widgetViews)
                .pluck('view')
                .filter(function(view) {
                    return view.isUpdating();
                })
                .value();

            // don't set up this listener if no work to do
            if (updatingViews.length > 0) {
                this.updateTracker.set('total', updatingViews.length);

                _.each(updatingViews, function(view) {
                    view.update(this.updateTracker)
                }, this);

                // handle completion
                this.listenTo(this.updateTracker, 'change:count', function(model, count) {
                    if (count === updatingViews.length) {
                        // publish completion
                        this.updateTracker.set('complete', true);
                        this.stopListening(this.updateTracker);
                    }
                });
            }
        }
    });

});