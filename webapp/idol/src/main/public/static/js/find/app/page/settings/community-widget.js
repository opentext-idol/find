/*
 * Copyright 2016 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

define([
    'settings/js/widgets/community-widget',
    'text!find/templates/app/page/settings/widget.html',
    'text!find/templates/app/page/settings/server-widget.html',
    'text!find/templates/app/page/settings/aci-widget.html',
    'underscore'
], function(CommunityWidget, widgetTemplate, serverTemplate, aciTemplate, _) {
    'use strict';

    return CommunityWidget.extend({
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
