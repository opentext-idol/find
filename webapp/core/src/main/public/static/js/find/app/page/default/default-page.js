/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this project except in compliance with the License.
 */

define([
    'js-whatever/js/base-page',
    'text!find/templates/app/page/default/default-page.html',
    'i18n!find/nls/bundle'
], function(BasePage, template, i18n) {

    return BasePage.extend({
        template: _.template(template),

        initialize: function(options) {
            this.options = options;
        },

        render: function() {
            this.$el.html(this.template({i18n: i18n}));
        }

    });
});
