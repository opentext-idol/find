/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module login-page/js/login
 */
define([
    '../../../backbone/backbone',
    'js-whatever/js/location',
    'text!login-page/templates/login.html',
    'underscore',
    'jquery'
], function(Backbone, location, template, _) {

    var expandTemplate = _.template('<i class="<%-icon%>"></i> <%-string%>');

    var more = function() {
        return expandTemplate({
            icon: this.iconPlusClass,
            string: this.options.strings.more
        })
    };

    var less = function() {
        return expandTemplate({
            icon: this.iconMinusClass,
            string: this.options.strings.less
        })
    };

    /**
     * @typedef LoginStrings
     * @desc Strings used by the default template. Different templates may require different strings
     * @property {object} error Object containing error strings. Any value taken by the error parameter should be a key
     * in this object, with the value being the message to display, which will be passed to the template
     * @property {string} defaultLogin Informs the user that they need to login with default credentials
     * @property {string} important Prefix for newCreds and defaultLogin
     * @property {string} infoSearchConfig First line of additional info
     * @property {string} infoDefaultLogin Second line of additional info
     * @property {string} infoPasswordCopyPaste Third line of additional info
     * @property {string} less Label for button which hides additional info
     * @property {string} login Label for the login button
     * @property {string} more Label for button which shows additional info
     * @property {string} newCreds Informs the user that they should login with new credentials
     * @property {string} password Label for the password field
     * @property {string} title Title for the form
     * @property {string} username Label for the username field
     */
    /**
     * @typedef LoginOptions
     * @property {string} configUrl Url that users visit to initially configure the system
     * @property {LoginStrings} strings Strings passed to the template
     * @property {string} url Form submission url passed to the template
     */
    /**
     * @name module:login-page/js/login.Login
     * @desc Creates a new LoginPage. Calls {@link module:login-page/js/login.Login#render render}
     * @constructor
     * @param {LoginOptions} options
     */
    return Backbone.View.extend(/** @lends module:login-page/js/login.Login.prototype */{

        /**
         * @desc Backbone el property
         * @default body
         */
        el: 'body',

        /**
         * @typedef TemplateOptions
         * @desc Options that are passed to the template
         * @property {string} [defaultUsername] Username to pre-populate the username field with. Set to the value of
         * the defaultLogin query parameter
         * @property {string} [error] Error message to display. Set to strings.error[&lt;value of error query parameter&gt;]
         * @property {string} [expandTemplate] Initial state of the more info button, which in the default template will
         * be rendered if isDefaultLogin is true
         * @property {boolean} [isDefaultLogin=false] If true, displays additional instructions about the default login
         * and makes the username input readonly. Should be used in conjunction with defaultUser. Set to true if the
         * defaultLogin query parameter exists
         * @property {boolean} [isNewLogin=false] Displays a message if set to true. Takes priority over isDefaultLogin.
         * Set to true if document.referrer contains {@link LoginOptions#configUrl}
         * @property {boolean} [isZkConfig=false] Displays a message if set to true. Set to true if the isZkConfig query
         * parameter is equal to true. Not used by the default template
         * @property {LoginStrings} strings Strings passed to the template
         * @property {string} url Url that the form will be posted to
         * @property {string} [username] Username to pre-populate the username field with if defaultUsername is not
         * defined. Set to the username query parameter
         */
        /**
         * @desc Template function.
         * @function
         * @param {TemplateOptions} options Options passed to the template at render time
         */
        template: _.template(template),

        /**
         * @desc CSS class used for the show less button when in default login mode
         * @default icon-minus
         */
        iconMinusClass: 'icon-minus',

        /**
         * @desc CSS class used for the show more button when in default login mode
         * @default icon-plus
         */
        iconPlusClass: 'icon-plus',

        /**
         * @desc CSS class used for grouping controls. Set this to form-group if using Bootstrap 3
         * @default control-group
         */
        controlGroupClass: 'control-group',

        /**
         * @desc CSS class used to indicate errors. Set to has-error if using Bootstrap 3
         * @default error
         */
        errorClass: 'error',

        initialize: function(options) {
            _.bindAll(this, 'login');

            this.options = options;

            this.render();
        },

        /**
         * @desc Renders the login view. You should not need to call this manually.
         */
        render: function() {
            var usernameParam = /[&?]username=([^&]+)/.exec(location.search());
            var errorParam = /[&?]error=([^&]+)/.exec(location.search());
            var isDefaultLogin = /[&?]defaultLogin=([^&]+)/.exec(location.search());
            var isZkConfig = /[&?]isZkConfig=([^&]+)/.exec(location.search()) && /[&?]isZkConfig=([^&]+)/.exec(location.search())[1] === 'true';
            var defaultUsername = isDefaultLogin ? isDefaultLogin[1] : '';
            var previousUrl = document.referrer;
            var isConfigUrl = previousUrl.indexOf(this.options.configURL) !== -1;

            this.$el.html(this.template({
                defaultUsername: defaultUsername,
                error: errorParam && this.options.strings.error[errorParam[1]],
                expandTemplate: more.call(this),
                isDefaultLogin: isDefaultLogin,
                isNewLogin: isConfigUrl,
                isZkConfig: isZkConfig,
                strings: this.options.strings,
                url: this.options.url,
                username: usernameParam && decodeURIComponent(usernameParam[1])
            }));

            this.$('input').on('keypress change', _.bind(function(e) {
                var element = $(e.currentTarget);

                if(element.val()) {
                    element.closest('.' + this.controlGroupClass).removeClass(this.errorClass)
                        .closest('form').find('.alert-' + this.errorClass).remove();
                }
            }, this));

            this.$('button').on('click', this.login);

            var $focusElement = isDefaultLogin ? this.$('#password') : this.$('#username');

            if(this.options.hasAnimation) {
                this.$('.loginscreen').on('animationend webkitAnimationEnd', function() {
                    $focusElement.focus();
                });
            } else {
                $focusElement.focus();
            }

            //for expanding more info on config.json
            this.$('.config-info a[href="#"]').click(_.bind(function (e) {
                e.preventDefault();
                this.expand = !this.expand;

                var html;

                if(this.expand) {
                    html = less.call(this)
                }
                else {
                    html = more.call(this)
                }

                this.$('.config-info a').html(html);
                this.$('.config-info .more-info').toggleClass('hide');
            }, this));
        },

        /**
         * @desc Called when the login button is clicked. Submits the form if all of the inputs are not empty, or adds
         * {@link module:login-page/js/login.Login#errorClass errorClass} to empty inputs
         * @param {Event} e jQuery event object
         */
        login: function(e) {
            e.preventDefault();

            var element = $(e.currentTarget);
            var valid = true;

            _.each(this.$('input'), function(input) {
                input = $(input);

                if(!input.val()) {
                    input.closest('.' + this.controlGroupClass).addClass(this.errorClass);
                    valid = false;
                }
            }, this);

            if(valid) {
                element.closest('form').submit();
            }
        }

    });

});