/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
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
        },

        getConfig: function() {
            return _.extend({
                conversationSystemName: this.$conversationSystem.val(),
            }, AnswerServerWidget.prototype.getConfig.call(this))
        },

        updateConfig: function(config) {
            AnswerServerWidget.prototype.updateConfig.call(this, config);
            this.$conversationSystem.val(config.conversationSystemName || '');
        },

        setValidationFormatting: function (state) {
            AnswerServerWidget.prototype.setValidationFormatting.apply(this, arguments);

            if (state === 'clear') {
                this.$conversationSystemControls.removeClass(this.successClass + ' ' + this.errorClass);
            } else {
                this.$conversationSystemControls.addClass(state)
                    .removeClass(state === this.successClass ? this.errorClass : this.successClass);
            }
        },
    });
});
