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
    'settings/js/validate-on-save-modal',
    'text!find/templates/app/page/settings/validate-on-save-modal.html',
    'text!find/templates/app/page/settings/validation-error-message.html',
    'underscore'
], function(SaveModal, template, errorTemplate, _) {
    'use strict';

    return SaveModal.extend({
        className: 'modal fade',
        template: _.template(template),

        initialize: function () {
            SaveModal.prototype.initialize.apply(this, arguments);

            this.errorTemplate = _.template(errorTemplate, undefined, {variable: 'ctx'});
        }
    });
});
