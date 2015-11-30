/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module settings/js/widgets/community-widget
 */
define([
    'settings/js/widgets/aci-widget',
    'settings/js/models/security-types',
    'text!settings/templates/widgets/community-widget.html'
], function(AciWidget, SecurityTypesModel, template) {

    /**
     * @typedef CommunityWidgetStrings
     * @desc Extends AciWidgetStrings
     * @property {string} loginTypeLabel Label for the login types dropdown
     * @property {string} fetchSecurityTypes An instruction saying that the test connection button will retrieve valid
     * security types
     * @property {string} invalidSecurityType Message which states a valid security type has not been selected
     */
    /**
     * @typedef CommunityWidgetOptions
     * @desc Extends AciWidgetOptions
     * @property {string} securityTypesUrl Url for fetching security types from
     * @property {CommunityWidgetStrings} strings Strings for the widget
     */
    /**
     * @name module:settings/js/widgets/community-widget.CommunityWidget
     * @desc Widget for configuring an IDOL Community server. As AciWidget, but allows the configuration of a repository
     * to use for authentication
     * @param {CommunityWidgetOptions} options Options for the widget
     * @constructor
     * @extends module:settings/js/widgets/aci-widget.AciWidget
     */
    return AciWidget.extend(/** @lends settings/js/widgets/community-widget.CommunityWidget.prototype */{
        currentSecurityType: null,

        /**
         * @typedef CommunityWidgetTemplateParameters
         * @property {CommunityWidgetStrings} strings Strings for the widget
         */
        /**
         * @callback module:settings/js/widgets/community-widget.CommunityWidget~CommunityWidgetTemplate
         * @param {CommunityWidgetTemplateParameters} parameters
         */
        /**
         * @desc Base template for the widget. Override if using Bootstrap 3
         * @type module:settings/js/widgets/community-widget.CommunityWidget~CommunityWidgetTemplate
         */
        communityTemplate: _.template(template),

        initialize: function(options) {
            AciWidget.prototype.initialize.call(this, options);

            _.bindAll(this, 'fetchNewSecurityTypes', 'getCommunity', 'handleNewSecurityTypes', 'toggleSecurityTypesInput', 'updateSecurityTypes');

            this.securityTypesModel = new SecurityTypesModel({}, {
                url: options.securityTypesUrl
            });

            this.securityTypesModel.on('change', this.handleNewSecurityTypes);
        },

        /**
         * @desc Renders the widget by first calling {@link module:settings/js/widgets/aci-widget.AciWidget#render|AciWidget#render}
         * and then rendering the security types
         */
        render: function() {
            AciWidget.prototype.render.call(this);

            this.$('button[name="validate"]').parent().after(this.communityTemplate({
                strings: this.strings
            }));

            this.$loginType = this.$('select[name="login-type"]');
            this.$aciDetails = this.$('div.'+this.controlGroupClass).eq(0);
            this.$typesSpan = this.$('.fetch-security-types');
        },

        /**
         * @returns {boolean} True if the configuration has changed since the widget was last saved; false otherwise
         */
        communityHasChanged: function() {
            return !_.isEqual(this.getCommunity(), this.lastValidationConfig.community);
        },

        /**
         * @desc Fetches security types for the given Community server
         * @param {AciWidgetConfig} community
         */
        fetchNewSecurityTypes: function(community) {
            this.securityTypesModel.unset('securityTypes', {silent: true});

            this.securityTypesModel.fetch({
                data: {
                    host: community.host,
                    port: community.port,
                    protocol: community.protocol
                }
            });
        },

        /**
         * @returns {AciWidgetConfig} The configuration of the Community server
         */
        getCommunity: function() {
            return AciWidget.prototype.getConfig.call(this);
        },


        /**
         * @typedef CommunityWidgetConfiguration
         * @property {AciWidgetConfig} community configuration of the Community server
         * @property {string} method The login method to use with the Community server
         */
        /**
         * @desc Returns the configuration of the widget
         * @returns {CommunityWidgetConfiguration}
         */
        getConfig: function() {
            //noinspection JSValidateTypes
            return {
                community: this.getCommunity(),
                method: this.$loginType.val() || this.currentSecurityType
            };
        },

        handleInputChange: function() {
            this.hideValidationInfo();

            if (!_.isUndefined(this.lastValidation) && !this.communityHasChanged()) {
                this.setValidationFormatting(this.lastValidation ? this.successClass : this.errorClass);
                this.toggleSecurityTypesInput(this.lastValidation && this.securityTypesModel.get('securityTypes'));
            } else {
                this.setValidationFormatting('clear');
                this.toggleSecurityTypesInput(false);
            }
        },

        /**
         * @desc Handler for changes in the list of available security types
         */
        handleNewSecurityTypes: function() {
            this.updateSecurityTypes();
            this.toggleSecurityTypesInput(!this.communityHasChanged());
        },

        /**
         * @desc Handles the results of server side validation. Fetches new security types if the config is valid
         */
        handleValidation: function(config, response) {
            if (_.isEqual(config.community, this.lastValidationConfig.community)) {
                this.lastValidation = response.valid;
                this.lastValidation && this.fetchNewSecurityTypes(config.community);
                this.hideValidationInfo();

                this.displayValidationMessage(!this.communityHasChanged(), response);
            }
        },

        /**
         * @desc Sets the validation formatting to the given state
         * @param {string} state If 'clear', removes validation formatting. Otherwise should be one of this.successClass
         * or this.errorClass, and will set the formatting accordingly
         */
        setValidationFormatting: function(state) {
            if (state === 'clear') {
                this.$aciDetails.removeClass(this.successClass + ' ' + this.errorClass);
                this.$loginType.parent().removeClass(this.errorClass);
                this.$('.fetch-security-types').removeClass('hide');
            } else {
                this.$aciDetails.addClass(state)
                    .removeClass(state === this.successClass ? this.errorClass : this.successClass);
            }
        },

        /**
         * @desc Hides or shows the security types input and corresponding instructions
         * @param (boolean} isEnabled True if login types should be enabled; false otherwise
         */
        toggleSecurityTypesInput: function(isEnabled) {
            this.$typesSpan.toggleClass('hide', isEnabled);
            this.$loginType.attr('disabled', !isEnabled);
        },

        /**
         * @desc Validation button handler. Tests the connection to the ACI server
         * @fires validate Event indicating that validation has occurred
         */
        triggerValidation: function() {
            this.setValidationFormatting('clear');
            this.hideValidationInfo();

            if (AciWidget.prototype.validateInputs.apply(this, arguments)) {
                this.trigger('validate');
            }
        },

        /**
         * @desc Updates the widget with the given configuration
         * @param {CommunityWidgetConfiguration} config
         */
        updateConfig: function(config) {
            AciWidget.prototype.updateConfig.call(this, config.community);
            this.currentSecurityType = config.method;
            this.securityTypesModel.unset('securityTypes', {silent: true});
            this.toggleSecurityTypesInput(false);
            this.updateSecurityTypes();
        },

        /**
         * @desc Updates the security types select dropdown with the values in the securityTypesModel. Will also add the
         * 'cas' and 'external' types if either of them is currently in use.
         * @protected
         */
        updateSecurityTypes: function() {
            var types = this.securityTypesModel.get('securityTypes');
            var currentType = this.$loginType.val() || this.currentSecurityType;
            this.$loginType.empty();

            if (types) {
                if (currentType === 'cas' || currentType === 'external') {
                    // If we are using a method unknown to community, append it to the dropdown.
                    this.$loginType.append(new Option(currentType, currentType, true, true));
                }

                _.each(types, function(type) {
                    this.$loginType.append(new Option(type, type, false, type === currentType));
                }, this);

                var currentOption = this.$loginType.find('[value="' + currentType + '"]');

                if(currentOption.length) {
                    this.$loginType.val(currentType);
                } else {
                    this.$loginType.val(_.first(types));
                }
            } else if (currentType) {
                this.$loginType.append(new Option(currentType, currentType, true, true));
            }
        },

        /**
         * @desc Validates the widget and applies formatting if necessary
         * @returns {boolean} True if the login type is not 'default' and all the AciWidget requirements are met; false
         * otherwise
         */
        validateInputs: function() {
            var isLoginTypeValid = this.getConfig().method !== 'default';

            if (!isLoginTypeValid) {
                this.updateInputValidation(this.$loginType, false);
                this.$('.fetch-security-types').addClass('hide');
            }

            // This is in this order so that formatting from the super method is applied
            return AciWidget.prototype.validateInputs.apply(this, arguments) && isLoginTypeValid;
        }
    });

});
