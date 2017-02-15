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
    'text!find/idol/templates/page/dashboard-page.html'
], function (_, BasePage, Backbone, vent, widgetRegistry, WidgetNotFoundWidget, UpdateTrackerModel, template) {
    'use strict';

    return BasePage.extend({

        className: 'dashboard',

        template: _.template(template),

        events: {
            'click .report-pptx': function(evt){
                evt.preventDefault();
                evt.stopPropagation();

                var reports = [],
                    scaleX = 0.01 * this.widthPerUnit,
                    scaleY = 0.01 * this.heightPerUnit

                _.each(this.widgetViews, function(widget) {
                    if (widget.view.exportPPTData) {
                        var data = widget.view.exportPPTData();

                        // this may be a promise, or an actual object
                        if (data) {
                            reports.push($.when(data).then(function(data){
                                var pos = widget.position;

                                return _.defaults(data, {
                                    title: widget.view.name,
                                    x: pos.x * scaleX,
                                    y: pos.y * scaleY,
                                    width: pos.width * scaleX,
                                    height: pos.height * scaleY
                                })
                            }));
                        }
                    }
                }, this);

                if (reports.length) {
                    $.when.apply($, reports).done(function(){
                        var children = [].slice.call(arguments, 0);

                        // Since it's an async action, we have to keep it as target: _self to avoid the popup blocker.
                        var $form = $('<form class="hide" enctype="multipart/form-data" method="post" action="api/bi/export/ppt/report"><textarea name="data"></textarea><input type="submit"></form>');

                        $form[0].data.value = JSON.stringify({
                            children: children
                        })

                        $form.appendTo(document.body).submit().remove()
                    })
                }
            }
        },

        initialize: function (options) {
            _.bindAll(this, 'update');

            this.dashboardName = options.dashboardName;
            this.updateInterval = 1000 * options.updateInterval;
            this.sidebarModel = options.sidebarModel;

            this.widgetViews = _.map(options.widgets, function (widget) {
                const widgetDefinition = widgetRegistry(widget.type);
                const WidgetConstructor = widgetDefinition ? widgetDefinition.Constructor : WidgetNotFoundWidget;

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
                    return view.savedSearch ? view.isUpdating() && view.savedSearch.type === 'QUERY' : view.isUpdating();
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