/*
 * Copyright 2014-2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'js-whatever/js/base-page',
    'find/app/vent',
    './dashboard/widget-registry',
    './dashboard/widgets/widget-not-found',
    'text!find/idol/templates/app/page/dashboard-page.html'
], function (_, BasePage, vent, widgetRegistry, WidgetNotFoundWidget, template) {
    'use strict';

    return BasePage.extend({

        className: 'dashboard',

        template: _.template(template),

        initialize: function (options) {
            this.dashboardName = options.dashboardName;
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

            this.listenTo(vent, 'vent:resize', function() {
                _.each(this.widgetViews, function(widget) {
                    widget.view.onResize();
                });
            });
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
        }
    });

});