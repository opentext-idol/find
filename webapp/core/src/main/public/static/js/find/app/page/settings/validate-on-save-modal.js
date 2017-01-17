/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'settings/js/validate-on-save-modal',
    'text!find/templates/app/page/settings/validate-on-save-modal.html',
    'underscore'
], function(SaveModal, template, _) {
    'use strict';

    return SaveModal.extend({
        className: 'modal fade',
        template: _.template(template)
    });
});
