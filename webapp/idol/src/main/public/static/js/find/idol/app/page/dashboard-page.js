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
    'text!find/idol/templates/page/dashboards/powerpoint-export-form.html',
    'i18n!find/nls/bundle'
], function(_, $, BasePage, vent, widgetRegistry, WidgetNotFoundWidget, UpdateTrackerModel,
            template, exportFormTemplate, i18n) {
    'use strict';

    const FULLSCREEN_CLASS = 'fullscreen';

    function fullscreenHandlerFactory(fullScreenElement) {
        return function() {
            this.toggleKeepAlive(!this.$widgets.hasClass(FULLSCREEN_CLASS));
            this.$widgets.toggleClass(FULLSCREEN_CLASS, fullScreenElement);
            this.onResize();
        }.bind(this);
    }

    return BasePage.extend({
        template: _.template(template),
        formTemplate: _.template(exportFormTemplate),

        events: function() {
            const events = {};
            events['click .' + FULLSCREEN_CLASS] = 'toggleFullScreen';
            events['click .report-pptx'] = 'exportDashboard';
            return events;
        },

        initialize: function(options) {
            _.bindAll(this, 'update');

            this.dashboardName = options.dashboardName;
            this.updateInterval = 1000 * options.updateInterval;
            this.sidebarModel = options.sidebarModel;
            this.displayWidgetNames = options.displayWidgetNames || 'never';

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

            this.defaultFullscreenEventHandler = fullscreenHandlerFactory.call(this, document.currentFullScreenElement);
            this.webkitFullscreenEventHandler = fullscreenHandlerFactory.call(this, document.webkitCurrentFullScreenElement);
            this.mozillaFullscreenEventHandler = fullscreenHandlerFactory.call(this, document.mozCurrentFullScreenElement);
            this.ie11FullscreenEventHandler = fullscreenHandlerFactory.call(this, document.msCurrentFullScreenElement);
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n
            }));

           const $widgets = $(document.createDocumentFragment());

            _.each(this.widgetViews, function(widget) {
                const $div = this.generateWidgetDiv(widget.position);
                $widgets.append($div);
                widget.view.setElement($div);
            }.bind(this));

            this.$widgets = this.$('.widgets').append($widgets);

            _.each(this.widgetViews, function(widget) {
                widget.view.render();
            }.bind(this));

            $.when
                .apply($, _.map(this.widgetViews, function(widget) {
                    return widget.view.initialiseWidgetPromise
                }))
                .done(function() {
                    this.$('.report-pptx').removeClass('hide');
                }.bind(this));
        },

        generateWidgetDiv: function(position) {
            return $('<div class="widget p-xs widget-name-' + this.displayWidgetNames + '"' + '></div>')
                .css({
                    'left': 'calc(' + position.x * this.widthPerUnit + '% + 20px)',
                    'top': 'calc(' + position.y * this.heightPerUnit + '% + 20px)',
                    'width': 'calc(' + position.width * this.widthPerUnit + '% - 10px)',
                    'height': 'calc(' + position.height * this.heightPerUnit + '% - 10px)'
                });
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

            this.listenTo(vent, 'vent:resize', this.onResize);
            this.listenTo(this.sidebarModel, 'change:collapsed', this.onResize);
            this.toggleFullScreenListener(true);
        },

        hide: function() {
            if(this.updateTracker) {
                this.updateTracker.set('cancelled', true);
                this.stopListening(this.updateTracker);
            }

            if(this.periodicUpdate) {
                clearInterval(this.periodicUpdate);
            }

            this.stopListening(vent, 'vent:resize');
            this.stopListening(this.sidebarModel, 'change:collapsed');
            this.toggleFullScreenListener(false);

            this.toggleKeepAlive(false);

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
        },

        toggleFullScreenListener: function(bool) {
            const onOrOff = bool ? 'on' : 'off';
            const addOrRemove = bool ? 'addEventListener' : 'removeEventListener';

            if(this.el.requestFullscreen) {
                this.$widgets[onOrOff]('fullscreenchange', this.defaultFullscreenEventHandler);
            } else if(this.el.webkitRequestFullscreen) {
                this.$widgets[onOrOff]('webkitfullscreenchange', this.webkitFullscreenEventHandler);
            } else if(this.el.mozRequestFullScreen) {
                document[addOrRemove]('mozfullscreenchange', this.mozillaFullscreenEventHandler);
            } else if(this.el.msRequestFullscreen) {
                document[addOrRemove]('MSFullscreenChange', this.ie11FullscreenEventHandler);
            }
        },

        toggleKeepAlive: function(bool) {
            if(bool) {
                this.keepAlivePromise = $.post('/api/bi/dashboards/keep-alive')
                    .done(function(response) {
                        const sessionLengthInMs = response * 1000;

                        // Schedule a server call two minutes before scheduled session timeout
                        this.keepAliveTimeout = setTimeout(function() {
                            this.keepAliveTimeout = null;
                            this.toggleKeepAlive(true);
                        }.bind(this), Math.ceil(sessionLengthInMs * 0.7));
                    }.bind(this))
                    .fail(function() {
                        this.keepAliveTimeout = setTimeout(function() {
                            this.keepAliveTimeout = null;
                            this.toggleKeepAlive(true);
                        }.bind(this), Math.ceil(sessionLengthInMs * 0.05))
                    }.bind(this))
                    .always(function() {
                        this.keepAlivePromise = null;
                    }.bind(this));
            } else {
                if(this.keepAliveTimeout) {
                    clearTimeout(this.keepAliveTimeout);
                    this.keepAliveTimeout = null;
                }

                if(this.keepAlivePromise) {
                    this.keepAlivePromise.abort();
                    this.keepAlivePromise = null;
                }
            }
        },

        toggleFullScreen: function() {
            const element = this.$widgets.get(0);

            if(element.requestFullscreen && !document.currentFullScreenElement) {
                element.requestFullscreen();
            } else if(element.webkitRequestFullscreen && !document.webkitCurrentFullScreenElement) {
                element.webkitRequestFullscreen();
            } else if(element.mozRequestFullScreen && !document.mozCurrentFullScreenElement) {
                element.mozRequestFullScreen();
            } else if(element.msRequestFullscreen && !document.msCurrentFullScreenElement) {
                element.msRequestFullscreen();
            }
        },

        exportDashboard: function(event) {
            event.preventDefault();
            event.stopPropagation();

            const reports = [];
            const scaleX = 0.01 * this.widthPerUnit;
            const scaleY = 0.01 * this.heightPerUnit;
            const $el = $(event.currentTarget);
            const multiPage = $el.is('.report-pptx-multipage');
            const labels = true;
            const padding = true;

            this.widgetViews.forEach(function(widget) {
                if(widget.view.exportData) {
                    const data = widget.view.exportData();

                    // this may be a promise, or an actual object
                    if(data) {
                        reports.push($.when(data)
                            .then(function(data) {
                                const pos = widget.position;

                                return _.defaults(data, {
                                    title: labels ? widget.view.name : undefined,
                                    x: multiPage ? 0 : pos.x * scaleX,
                                    y: multiPage ? 0 : pos.y * scaleY,
                                    width: multiPage ? 1 : pos.width * scaleX,
                                    height: multiPage ? 1 : pos.height * scaleY,
                                    margin: padding ? 3 : 0
                                })
                            }));
                    }
                }
            });

            if(reports.length) {
                $.when.apply($, reports)
                    .done(function() {
                        const children = _.compact(arguments);
                        const $form = $(this.formTemplate({
                            data: JSON.stringify({
                                children: children
                            }),
                            multiPage: multiPage
                        }));
                        $form.appendTo(document.body).submit().remove()
                    }.bind(this))
            }
        }
    });
});
