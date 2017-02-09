define([
    './static-content',
    'i18n!find/nls/bundle'
], function(StaticContentWidget, i18n) {
    'use strict';

    return StaticContentWidget.extend({

        initialize: function(options) {
            StaticContentWidget.prototype.initialize.call(this, _.defaults({
                widgetSettings: {
                    html: i18n['dashboards.widget.notFound'](options.type)
                }
            }, options))
        }

    });

});