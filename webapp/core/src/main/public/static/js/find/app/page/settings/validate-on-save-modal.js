/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
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
