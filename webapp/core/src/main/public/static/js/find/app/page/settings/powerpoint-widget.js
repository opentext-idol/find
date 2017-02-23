/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    'settings/js/widget',
    'text!find/templates/app/page/settings/widget.html',
    'text!find/templates/app/page/settings/powerpoint-widget.html',
    'underscore'
], function($, Widget, widgetTemplate, template, _) {
    'use strict';

    return Widget.extend({
        widgetTemplate: _.template(widgetTemplate),
        template: _.template(template),

        className: 'panel-group',
        controlGroupClass: 'form-group',
        formControlClass: 'form-control',
        errorClass: 'has-error',
        successClass: 'has-success',

        events: _.extend({
            'click button[name=validate]': 'triggerValidation',
            'change .template-input-margin': 'updateMarginIndicator',
            'input .template-input-margin': 'updateMarginIndicator'
        }, Widget.prototype.events),

        initialize: function(options) {
            Widget.prototype.initialize.apply(this, arguments);
            this.pptxTemplateUrl = options.pptxTemplateUrl;
        },

        render: function() {
            Widget.prototype.render.apply(this, arguments);

            this.$content.html(this.template({
                strings: this.strings,
                pptxTemplateUrl: this.pptxTemplateUrl
            }));

            this.$templateFile = this.$('.template-file-input');
            this.$validity = this.$('.settings-client-validation');

            this.$marginLeft = this.$('.template-input-marginLeft');
            this.$marginTop = this.$('.template-input-marginTop');
            this.$marginRight = this.$('.template-input-marginRight');
            this.$marginBottom = this.$('.template-input-marginBottom');

            this.$marginIndicator = this.$('.powerpoint-margins-indicator');
        },

        handleValidation: function(config, response) {
            if (response.valid) {
                this.setValidationFormatting(this.successClass);
                this.hideValidationInfo();
            } else {
                this.setValidationFormatting(this.errorClass);
                this.$validity.text(this.strings[response.data])
                    .stop()
                    .animate({opacity: 1})
                    .removeClass('hide');
            }
        },

        triggerValidation: function() {
            this.setValidationFormatting('clear');
            this.hideValidationInfo();

            if (this.validateInputs()) {
                this.trigger('validate');
            }
        },

        getConfig: function() {
            return {
                templateFile: this.$templateFile.val(),
                marginLeft: this.$marginLeft.val(),
                marginTop: this.$marginTop.val(),
                marginRight: this.$marginRight.val(),
                marginBottom: this.$marginBottom.val()
            }
        },

        updateConfig: function(config) {
            if (config) {
                this.$templateFile.val(config.templateFile);
                this.$marginLeft.val(config.marginLeft);
                this.$marginTop.val(config.marginTop);
                this.$marginRight.val(config.marginRight);
                this.$marginBottom.val(config.marginBottom);
                this.updateMarginIndicator();
            }
        },

        updateMarginIndicator: function() {
            this.$marginIndicator.css({
                left: 100 * Math.min(1, Math.max(0, this.$marginLeft.val())) + '%',
                top: 100 * Math.min(1, Math.max(0, this.$marginTop.val())) + '%',
                right: 100 * Math.min(1, Math.max(0, this.$marginRight.val())) + '%',
                bottom: 100 * Math.min(1, Math.max(0, this.$marginBottom.val())) + '%'
            })
        }
    });
});
