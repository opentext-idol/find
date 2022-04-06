/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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

    const formTemplateFn = _.template(exportFormTemplate);

    const emptyReport = {
        data: {
            text: [
                {
                    text: i18n['export.powerpoint.widgetEmpty'],
                    fontSize: 12
                }
            ],
        },
        type: 'text'
    };

    const errorReport = {
        data: {
            text: [
                {
                    text: i18n['export.powerpoint.widgetError'],
                    fontSize: 12
                }
            ],
        },
        type: 'text'
    };

    function fullscreenHandlerFactory(fullScreenElement) {
        return function() {
            this.toggleKeepAlive(!this.$widgets.hasClass(FULLSCREEN_CLASS));
            this.$widgets.toggleClass(FULLSCREEN_CLASS, fullScreenElement);
            this.onResize();
        }.bind(this);
    }

    return BasePage.extend({
        template: _.template(template),

        events: function() {
            const events = {
                'click .report-pptx': function(e) {
                    e.preventDefault();

                    // false if exporting to single slide
                    const exportMultipleSlides = $(e.currentTarget).is('.report-pptx-multipage');

                    this.exportDashboard(exportMultipleSlides);
                }
            };

            events['click .' + FULLSCREEN_CLASS] = 'toggleFullScreen';
            return events;
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

                const displayWidgetName = widget.displayWidgetName || options.displayWidgetNames || 'never';
                const widgetOptions = _.extend({
                    updateInterval: this.updateInterval,
                    displayWidgetName: displayWidgetName,
                    savedQueryCollection: options.savedQueryCollection
                }, widget);

                return {
                    view: new WidgetConstructor(widgetOptions),
                    displayWidgetName: displayWidgetName,
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
                const $div = this.generateWidgetDiv(widget);
                $widgets.append($div);
                widget.view.setElement($div);
            }.bind(this));

            this.$widgets = this.$('.widgets').append($widgets);

            _.each(this.widgetViews, function(widget) {
                widget.view.render();
            }.bind(this));

            $.when
                .apply($, _.map(this.widgetViews, function(widget) {
                    return widget.view.widgetInitializePromise;
                }))
                .done(function() {
                    this.$('.report-pptx').removeClass('hide');
                }.bind(this));
        },

        generateWidgetDiv: function(widget) {
            return $('<div class="widget p-xs"></div>')
                .addClass('widget-name-' + widget.displayWidgetName)
                .toggleClass('clickable', widget.view.clickable)
                .css({
                    'left': 'calc(' + widget.position.x * this.widthPerUnit + '% + 10px)',
                    'top': 'calc(' + widget.position.y * this.heightPerUnit + '% + 10px)',
                    'width': 'calc(' + widget.position.width * this.widthPerUnit + '% - 20px)',
                    'height': 'calc(' + widget.position.height * this.heightPerUnit + '% - 20px)'
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

            this.widgetViews.forEach(function(widget) {
                widget.view.onHide();
            });

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
            const onOrOff = bool
                ? 'on'
                : 'off';
            const addOrRemove = bool
                ? 'addEventListener'
                : 'removeEventListener';

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
                this.keepAlivePromise = $.post('api/bi/dashboards/keep-alive')
                    .done(function(response) {
                        const sessionLengthInMs = response * 1000;

                        // Schedule a server call before scheduled session timeout
                        this.keepAliveTimeout = setTimeout(function() {
                            this.keepAliveTimeout = null;
                            this.toggleKeepAlive(true);
                        }.bind(this), Math.ceil(sessionLengthInMs * 0.7));
                    }.bind(this))
                    .fail(function() {
                        // We don't know the session timeout, so just try again in a minute
                        this.keepAliveTimeout = setTimeout(function() {
                            this.keepAliveTimeout = null;
                            this.toggleKeepAlive(true);
                        }.bind(this), 60000)
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

        exportDashboard: function(multiPage) {
            const reports = [];
            const scaleX = 0.01 * this.widthPerUnit;
            const scaleY = 0.01 * this.heightPerUnit;
            const labels = true;
            const padding = true;

            this.widgetViews.forEach(function(widget) {
                const isEmpty = widget.view.isEmpty && widget.view.isEmpty();
                const hasError = widget.view.hasError && widget.view.hasError();
                const pos = widget.position;

                function exportCallback(data) {
                    return _.extend({
                            title: labels ? widget.view.name : undefined,
                            margin: padding ? 3 : 0
                        },
                        data,
                        multiPage
                            ? {
                                x: 0,
                                y: 0,
                                width: 1,
                                height: 1
                            }
                            : {
                                x: pos.x * scaleX,
                                y: pos.y * scaleY,
                                width: pos.width * scaleX,
                                height: pos.height * scaleY
                            });
                }

                if(widget.view.exportData) {
                    // Check for error first, as failed fetches produce empty widgets
                    if(hasError) {
                        reports.push($.when(errorReport).then(exportCallback));
                    } else if(isEmpty) {
                        reports.push($.when(emptyReport).then(exportCallback));
                    } else {
                        const data = widget.view.exportData();

                        // this may be a promise, or an actual object
                        if(data) {
                            reports.push($.when(data).then(exportCallback));
                        }
                    }
                }
            });

            if(reports.length > 0) {
                $.when.apply($, reports)
                    .done(function() {
                        const children = _.compact(arguments);
                        const $form = $(formTemplateFn({
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
