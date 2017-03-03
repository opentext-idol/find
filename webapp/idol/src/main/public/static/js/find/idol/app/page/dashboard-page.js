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
    './dashboard/update-tracker-model',
    'text!find/idol/templates/page/dashboard-page.html',
    'i18n!find/nls/bundle'
], function(_, $, BasePage, vent, widgetRegistry, WidgetNotFoundWidget, UpdateTrackerModel, template, i18n) {
    'use strict';

    return BasePage.extend({
        className: 'dashboard',
        template: _.template(template),

        events: {
            'click .report-pptx-checkbox': function(evt){
                evt.stopPropagation();
            },
            'click .report-pptx': function(evt){
                evt.preventDefault();
                evt.stopPropagation();

                var reports = [],
                    scaleX = 0.01 * this.widthPerUnit,
                    scaleY = 0.01 * this.heightPerUnit,
                    $el = $(evt.currentTarget),
                    multipage = $el.is('.report-pptx-multipage'),
                    $group = $el.closest('.btn-group'),
                    labels = $group.find('.report-pptx-labels:checked').length,
                    padding = $group.find('.report-pptx-padding:checked').length;

                _.each(this.widgetViews, function(widget) {
                    if (widget.view.exportPPTData) {
                        var data = widget.view.exportPPTData();

                        // this may be a promise, or an actual object
                        if (data) {
                            reports.push($.when(data).then(function(data){
                                var pos = widget.position;

                                return _.defaults(data, {
                                    title: labels ? widget.view.name : undefined,
                                    x: multipage ? 0 : pos.x * scaleX,
                                    y: multipage ? 0 : pos.y * scaleY,
                                    width: multipage ? 1 : pos.width * scaleX,
                                    height: multipage ? 1 : pos.height * scaleY,
                                    margin: padding ? 3 : 0
                                })
                            }));
                        }
                    }
                }, this);

                if (reports.length) {
                    $.when.apply($, reports).done(function(){
                        var children = _.compact(arguments);

                        // Since it's an async action, we have to keep it as target: _self to avoid the popup blocker.
                        var $form = $('<form class="hide" enctype="multipart/form-data" method="post" action="api/bi/export/ppt/report"><input name="multipage"><textarea name="data"></textarea><input type="submit"></form>');

                        $form[0].data.value = JSON.stringify({
                            children: children
                        })

                        $form[0].multipage.value = multipage;

                        $form.appendTo(document.body).submit().remove()
                    })
                }
            }
        },

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
            this.$el.html(this.template({
                dashboardName: this.dashboardName,
                powerpointSingle: i18n['powerpoint.export.single'],
                powerpointMultiple: i18n['powerpoint.export.multiple'],
                powerpointLabels: i18n['powerpoint.export.labels'],
                powerpointPadding: i18n['powerpoint.export.padding'],
            }));

            _.each(this.widgetViews, function(widget) {
                const $div = this.generateWidgetDiv(widget.position);
                this.$el.append($div);
                widget.view.setElement($div).render();
            }.bind(this));

            var $exportBtn = this.$('.report-pptx-group');

            $.when.apply($, _.map(this.widgetViews, function(widget){
                return widget.view.savedSearchPromise
            })).done(function(){
                $exportBtn.removeClass('hide');
            })

            this.listenTo(vent, 'vent:resize', this.onResize);
            this.listenTo(this.sidebarModel, 'change:collapsed', this.onResize);
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

        show: function() {
            BasePage.prototype.show.call(this);

            if(this.updateInterval) {
                this.periodicUpdate = setInterval(this.update, this.updateInterval);
            }
        },

        hide: function() {
            if(this.updateTracker) {
                this.updateTracker.set('cancelled', true);
                this.stopListening(this.updateTracker);
            }

            if(this.periodicUpdate) {
                clearInterval(this.periodicUpdate);
            }

            BasePage.prototype.hide.call(this);
        },

        update: function() {
            if(this.isVisible()) {
                // cancel pending update
                if(this.updateTracker && !this.updateTracker.get('complete')) {
                    this.updateTracker.set('cancelled', true);
                    this.stopListening(this.updateTracker);
                }

                // find updating views
                const updatingViews = _.chain(this.widgetViews)
                    .pluck('view')
                    .filter(function(view) {
                        return view.savedSearch
                            ? view.isUpdating() && view.savedSearch.type === 'QUERY'
                            : view.isUpdating();
                    })
                    .value();

                // don't set up this listener if no work to do
                const total = updatingViews.length;

                if(total > 0) {
                    // set up tracker
                    this.updateTracker = new UpdateTrackerModel({total: total});

                    _.each(updatingViews, function(view) {
                        view.update(this.updateTracker)
                    }, this);

                    // handle completion
                    this.listenTo(this.updateTracker, 'change:count', function(model, count) {
                        if(count === total) {
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
