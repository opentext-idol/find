/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/settings/aci-widget',
    'text!find/templates/app/page/settings/view-widget.html'
], function(AciWidget, template) {

    return AciWidget.extend({
        viewTemplate: _.template(template),

        render: function() {
            AciWidget.prototype.render.call(this);

            this.$('button[name="validate"]').parent().after(this.viewTemplate({
                strings: this.strings
            }));

            this.$referenceField = this.$('[name="referenceField"]');
        },

        updateConfig: function(config) {
            AciWidget.prototype.updateConfig.apply(this, arguments);

            this.$referenceField.val(config.referenceField);
        },

        getConfig: function() {
            var config = AciWidget.prototype.getConfig.call(this);

            return _.extend({
                referenceField: this.$referenceField.val()
            }, config);
        },

        handleValidation: function(config, response) {
            if (_.isEqual(config, this.lastValidationConfig)) {
                if(response.data == "REFERENCE_FIELD_BLANK") {
                    this.updateInputValidation(this.$referenceField, false);
                }
                else {
                    AciWidget.prototype.handleValidation.apply(this, arguments);
                }
            }
        },

        validateInputs: function() {
            var isValid = AciWidget.prototype.validateInputs.apply(this, arguments);

            if (this.$referenceField.val() === '') {
                this.updateInputValidation(this.$referenceField, false);
                return false;
            }

            return isValid;
        }
    });

});