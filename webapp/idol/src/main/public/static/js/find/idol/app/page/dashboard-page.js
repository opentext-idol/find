/*
 * Copyright 2014-2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'js-whatever/js/base-page',
    './dashboard/widget-registry',
    'text!find/idol/templates/app/page/dashboard-page.html'
], function (_, BasePage, widgetRegistry, template) {
    'use strict';

    return BasePage.extend({

        className: 'dashboard',

        template: _.template(template),

        initialize: function (options) {
            this.dashboardName = options.dashboardName;
            this.widgetViews = _.map(options.widgets, function (widget) {
                const WidgetConstructor = widgetRegistry(widget.type).Constructor;
                
                return {
                    view: new WidgetConstructor(widget.widgetSettings),
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