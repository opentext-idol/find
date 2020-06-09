/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
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
    'js-whatever/js/base-page',
    'text!find/templates/app/page/default/default-page.html',
    'i18n!find/nls/bundle',
    'underscore'
], function(BasePage, template, i18n, _) {
    'use strict';

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
