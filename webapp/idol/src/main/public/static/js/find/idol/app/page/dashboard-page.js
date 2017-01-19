/*
 * Copyright 2014-2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'js-whatever/js/base-page',
    'text!find/idol/templates/app/page/dashboard-page.html'
], function(BasePage, template) {
    'use strict';

    return BasePage.extend({

        className: 'container-fluid',

        template: _.template(template),

        initialize: function(options) {
            this.dashboardName = options.dashboardName;
        },

        render: function() {
            this.$el.html(this.template({
                dashboardName: this.dashboardName
            }));
        }

    });

});