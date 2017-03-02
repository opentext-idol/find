/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    './static-content',
    'i18n!find/nls/bundle'
], function(_, StaticContentWidget, i18n) {
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
