/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
define([
    'underscore',
    'settings/js/widgets/answer-server-widget',
    'find/app/page/settings/enable-view',
    'settings/js/controls/aci-widget-dropdown-view-no-op',
    'text!find/templates/app/page/settings/widget.html',
    'text!find/templates/app/page/settings/server-widget.html',
    'text!find/templates/app/page/settings/aci-widget.html'
], function (_, AnswerServerWidget, EnableView, DropdownViewNoOp, widgetTemplate, serverTemplate, aciTemplate) {

    return AnswerServerWidget.extend({
        className: 'panel-group',

        formControlClass: 'form-control',
        controlGroupClass: 'form-group',
        successClass: 'has-success',
        errorClass: 'has-error',

        aciTemplate: _.template(aciTemplate),
        serverTemplate: _.template(serverTemplate),
        widgetTemplate: _.template(widgetTemplate),
        
        DropdownView: DropdownViewNoOp,
        EnableView: EnableView
    });
});
