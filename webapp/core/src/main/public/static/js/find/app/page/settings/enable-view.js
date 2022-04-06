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
    'settings/js/controls/enable-view',
    'text!find/templates/app/page/settings/enable-view.html',
    'underscore'
], function(EnableView, template, _) {
    'use strict';

    return EnableView.extend({
        className: 'm-t-sm',
        template: _.template(template),

        updateFormatting: function() {
            this.$button.toggleClass('button-primary', !this.enabled)
                .toggleClass('button-warning', this.enabled)
                .html(this.enabled ? '<i class="fa fa-remove"></i> ' + this.strings.disable
                    : '<i class="' + this.icon + '"></i> ' + this.strings.enable)
                .siblings('label').text(
                this.enabled ? this.strings.enabled
                    : this.strings.disabled);
        }
    });
});
