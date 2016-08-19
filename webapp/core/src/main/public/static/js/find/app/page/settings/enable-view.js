/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'settings/js/controls/enable-view',
    'text!find/templates/app/page/settings/enable-view.html'
], function(EnableView, template) {

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