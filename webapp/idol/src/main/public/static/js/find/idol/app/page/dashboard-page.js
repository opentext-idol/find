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
    'text!find/idol/templates/page/dashboards/dashboard-page.html',
    'i18n!find/nls/bundle'
], function(_, $, BasePage, vent, widgetRegistry, WidgetNotFoundWidget, UpdateTrackerModel, template, i18n) {
    'use strict';

    return BasePage.extend({
        template: _.template(template),

        events: {
            'click .fullscreen': 'toggleFullScreen'
        },

        initialize: function (options) {
            _.bindAll(this, 'update');

            this.dashboardName = options.dashboardName;
            this.updateInterval = 1000 * options.updateInterval;
            this.sidebarModel = options.sidebarModel;
            this.displayWidgetNames = options.displayWidgetNames || 'never';

            this.widgetViews = _.map(options.widgets, function (widget) {
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

            this.mozillaFullscreenEventHandler = function () {
                this.$('.widgets').toggleClass('fullscreen', document.mozCurrentFullScreenElement);
                this.onResize();
            }.bind(this);

            this.ie11FullscreenEventHandler = function () {
                this.$('.widgets').toggleClass('fullscreen', document.msCurrentFullScreenElement);
                this.onResize();
            }.bind(this);
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n,
                dashboardName: this.dashboardName,
                powerpointExportButton: i18n['export.powerpoint.button'],
                powerpointSingle: i18n['export.powerpoint.single'],
                powerpointMultiple: i18n['export.powerpoint.multiple'],
                powerpointLabels: i18n['export.powerpoint.labels'],
                powerpointPadding: i18n['export.powerpoint.padding'],
            }));

            _.each(this.widgetViews, function (widget) {
                const $div = this.generateWidgetDiv(widget.position);
                $widgets.append($div);
                widget.view.setElement($div).render();
            }.bind(this));

            const $exportBtn = this.$('.report-pptx-group');
            $.when.apply($, _.map(this.widgetViews, function(widget){
                return widget.view.initialiseWidgetPromise
            })).done(function(){
                $exportBtn.removeClass('hide');
            });

            this.addFullScreenListener();

            this.listenTo(vent, 'vent:resize', this.onResize);
            this.listenTo(this.sidebarModel, 'change:collapsed', this.onResize);
        },

        generateWidgetDiv: function (position) {
            return $('<div class="widget p-xs widget-name-' + this.displayWidgetNames + '"' + '></div>').css({
                'left': 'calc(' + position.x * this.widthPerUnit + '% + 20px)',
                'top': 'calc(' + position.y * this.heightPerUnit + '% + 20px)',
                'width': 'calc(' + position.width * this.widthPerUnit + '% - 10px)',
                'height': 'calc(' + position.height * this.heightPerUnit + '% - 10px)'
            });
        },

        onResize: function () {
            if (this.isVisible()) {
                _.each(this.widgetViews, function (widget) {
                    widget.view.onResize();
                });
            }
        },

        show: function () {
            BasePage.prototype.show.call(this);

            if (this.updateInterval) {
                this.periodicUpdate = setInterval(this.update, this.updateInterval);
            }
        },

        hide: function () {
            if (this.updateTracker) {
                this.updateTracker.set('cancelled', true);
                this.stopListening(this.updateTracker);
            }

            if (this.periodicUpdate) {
                clearInterval(this.periodicUpdate);
            }

            BasePage.prototype.hide.call(this);
        },

        update: function () {
            if (this.isVisible()) {
                // cancel pending update
                if (this.updateTracker && !this.updateTracker.get('complete')) {
                    this.updateTracker.set('cancelled', true);
                    this.stopListening(this.updateTracker);
                }

                // find updating views
                const updatingViews = _.chain(this.widgetViews)
                    .pluck('view')
                    .filter(function (view) {
                        return view.savedSearch
                            ? view.isUpdating() && view.savedSearch.type === 'QUERY'
                            : view.isUpdating();
                    })
                    .value();

                // don't set up this listener if no work to do
                const total = updatingViews.length;

                if (total > 0) {
                    // set up tracker
                    this.updateTracker = new UpdateTrackerModel({total: total});

                    _.each(updatingViews, function (view) {
                        view.update(this.updateTracker)
                    }, this);

                    // handle completion
                    this.listenTo(this.updateTracker, 'change:count', function (model, count) {
                        if (count === total) {
                            // publish completion
                            this.updateTracker.set('complete', true);
                            this.stopListening(this.updateTracker);
                        }
                    });
                }

                this.onResize();
            }
        },

        remove: function () {
            BasePage.prototype.remove.call(this);
            if (this.el.mozRequestFullScreen) {
                document.removeEventListener('mozfullscreenchange', this.mozillaFullscreenEventHandler);
            } else if (this.el.msRequestFullscreen) {
                document.addEventListener('MSFullscreenChange', this.ie11FullscreenEventHandler);
            }
        },

        addFullScreenListener: function () {
            if (this.el.requestFullscreen) {
                this.$('.widgets').on('fullscreenchange', function () {
                    this.$('.widgets').toggleClass('fullscreen', document.currentFullScreenElement);
                    this.onResize();
                }.bind(this));
            } else if (this.el.webkitRequestFullscreen) {
                this.$('.widgets').on('webkitfullscreenchange', function () {
                    this.$('.widgets').toggleClass('fullscreen', document.webkitCurrentFullScreenElement);
                    this.onResize();
                }.bind(this));
            } else if (this.el.mozRequestFullScreen) {
                document.addEventListener('mozfullscreenchange', this.mozillaFullscreenEventHandler);
            } else if (this.el.msRequestFullscreen) {
                document.addEventListener('MSFullscreenChange', this.ie11FullscreenEventHandler);
            }
        },

        toggleFullScreen: function () {
            const element = this.$('.widgets').get(0);

            if (element.requestFullscreen) {
                if (!document.currentFullScreenElement) {
                    element.requestFullscreen();
                }
            } else if (element.webkitRequestFullscreen) {
                if (!document.webkitCurrentFullScreenElement) {
                    element.webkitRequestFullscreen();
                }
            } else if (element.mozRequestFullScreen) {
                if (!document.mozCurrentFullScreenElement) {
                    element.mozRequestFullScreen();
                }
            } else if (element.msRequestFullscreen) {
                if (!document.msCurrentFullScreenElement) {
                    element.msRequestFullscreen();
                }
            }
        }
    });
});
