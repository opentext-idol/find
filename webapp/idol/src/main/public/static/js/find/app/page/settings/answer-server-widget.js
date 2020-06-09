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
    'underscore',
    'settings/js/widgets/answer-server-widget',
    'find/app/page/settings/enable-view',
    'settings/js/controls/aci-widget-dropdown-view-no-op',
    'text!find/templates/app/page/settings/answer-server-widget-extensions.html',
    'text!find/templates/app/page/settings/widget.html',
    'text!find/templates/app/page/settings/server-widget.html',
    'text!find/templates/app/page/settings/aci-widget.html'
], function (_, AnswerServerWidget, EnableView, DropdownViewNoOp, extensionsTemplate, widgetTemplate, serverTemplate, aciTemplate) {

    return AnswerServerWidget.extend({
        className: 'panel-group',

        formControlClass: 'form-control',
        controlGroupClass: 'form-group',
        successClass: 'has-success',
        errorClass: 'has-error',

        aciTemplate: _.template(aciTemplate),
        serverTemplate: _.template(serverTemplate),
        widgetTemplate: _.template(widgetTemplate),
        extensionsTemplate: _.template(extensionsTemplate),
        
        DropdownView: DropdownViewNoOp,
        EnableView: EnableView,

        render: function() {
            AnswerServerWidget.prototype.render.apply(this, arguments);

            this.enableView.$el.before(this.extensionsTemplate({
                strings: this.strings
            }));

            this.$conversationSystem = this.$('.conversation-system-input');
            this.$conversationSystemControls = this.$conversationSystem.closest('div.' + this.controlGroupClass);

            this.$systemNames = this.$('.system-names');
            this.$systemNamesControls = this.$systemNames.closest('div.' + this.controlGroupClass);
        },

        getConfig: function() {
            const systemNames = this.$systemNames.val();
            return _.extend({
                conversationSystemName: this.$conversationSystem.val(),
                systemNames: systemNames ? systemNames.split(/,\s*/) : []
            }, _.omit(AnswerServerWidget.prototype.getConfig.call(this), 'systemName'))
        },

        updateConfig: function(config) {
            AnswerServerWidget.prototype.updateConfig.call(this, config);
            this.$conversationSystem.val(config.conversationSystemName || '');
            this.$systemNames.val((config.systemNames || []).join(','));
        },

        setValidationFormatting: function (state) {
            AnswerServerWidget.prototype.setValidationFormatting.apply(this, arguments);

            if (state === 'clear') {
                this.$conversationSystemControls.add(this.$systemNamesControls).removeClass(this.successClass + ' ' + this.errorClass);
            } else {
                this.$conversationSystemControls.add(this.$systemNamesControls).addClass(state)
                    .removeClass(state === this.successClass ? this.errorClass : this.successClass);
            }
        },
    });
});
