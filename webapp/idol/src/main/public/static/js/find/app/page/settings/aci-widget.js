/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'settings/js/widgets/aci-widget',
    'text!find/templates/app/page/settings/widget.html',
    'text!find/templates/app/page/settings/server-widget.html',
    'text!find/templates/app/page/settings/aci-widget.html',
    'underscore'
], function(AciWidget, widgetTemplate, serverTemplate, aciTemplate, _) {
    'use strict';

    return AciWidget.extend({
        className: 'panel-group',
        controlGroupClass: 'form-group',
        formControlClass: 'form-control',
        errorClass: 'has-error',
        successClass: 'has-success',

        aciTemplate: _.template(aciTemplate),
        serverTemplate: _.template(serverTemplate),
        widgetTemplate: _.template(widgetTemplate)
    });
});
