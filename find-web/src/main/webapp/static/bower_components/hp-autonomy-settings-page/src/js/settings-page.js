/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module settings/js/settings-page
 */
define([
    'js-whatever/js/base-page',
    'settings/js/validate-on-save-modal',
    'js-whatever/js/confirm',
    'js-whatever/js/ensure-array',
    'js-whatever/js/listenable',
    'text!settings/templates/settings-page.html'
], function(BasePage, SaveModal, confirm, ensureArray, listenable, template) {

    /**
     * @name module:settings/js/settings-page.SettingsPage
     * @desc A page which can be configured with widgets
     * @constructor
     * @extends BasePage
     */
    return BasePage.extend(/** @lends module:settings/js/settings-page.SettingsPage.prototype */ {
        /**
         * @typedef ConfigModel
         * @desc Model containing configuration. The properties here are Backbone attributes accessed via the get method
         * @property {object} config The config
         * @extends Autoload
         */
        /**
         * @desc Model to use for configuration
         * @default {}
         * @type ConfigModel
         */
        configModel: {},

        /**
         * @desc CSS class to use for widget groups
         * @default span4
         * @type String
         */
        groupClass: 'span4',

        /**
         * @desc Icon for the page
         * @default icon-cog
         * @type String
         */
        icon: 'icon-cog',

        /**
         * @desc Method for initializing this.widgetGroups
         * @abstract
         * @method
         */
        initializeWidgets: $.noop,

        /**
         * @desc Backbone Router used for navigating to specific widgets
         * @type Backbone.Router
         * @default {}
         */
        router: {},

        /**
         * @desc Event name to observe on {@link module:settings/js/settings-page.SettingsPage#router|router}
         * @type String
         * @default route:settings
         */
        routeEvent: 'route:settings',

        /**
         * @desc {@link module:settings/js/settings-page.SettingsPage#vent|vent}
         * @type String
         * @default ''
         */
        routeRoot: '',

        /**
         * @desc Page element to scroll
         * @type String
         * @default ''
         */
        scrollSelector: '',

        /**
         * @callback module:settings/js/settings-page.SettingsPage~DescriptionCallback
         * @param systemProperty The name of the system property containing the config file
         * @param path The path to the config file
         */
        /**
         * @typedef SettingsPageStrings
         * @desc Strings for the settings page. May vary based on template used.
         * @property {string} cancelCancel Cancel button label for confirmation modal when restore changes is pressed
         * @property {string} cancelMessage Text for confirmation modal when restore changes is pressed
         * @property {string} cancelOk OK button label for confirmation modal when restore changes is pressed
         * @property {string} cancelTitle Title for confirmation modal when restore changes is pressed
         * @property {string} confirmUnload Message displayed when unloading the page with unsaved changes
         * @property {module:settings/js/settings-page.SettingsPage~DescriptionCallback} description Function for
         * generating a description
         * @property {string} requiredFields Label for asterisk denoting required fields
         * @property {string} restoreButton Label for restore button
         * @property {string} saveButton Label for save button
         * @property {SaveModalStrings} saveModal Strings for the save modal
         * @property {string} title Title for the page
         */
        /**
         * @desc Strings for the settings page.
         * @type SettingsPageStrings
         * @default {}
         * @abstract
         */
        strings: {},

        /**
         * @desc Url to which config will be posted for validation
         * @type String
         * @default ''
         */
        validateUrl: '',

        /**
         * @desc Instance of Vent used for navigation
         * @type Vent
         * @abstract
         */
        vent: {},

        /**
         * @desc Array containing all of the widgets.
         * @type module:settings/js/widget.Widget[]
         * @default []
         */
        widgets: [],

        /**
         * @desc Array of groups of widgets, where each widget group is itself an array. With the default settings each
         * group should contain at most three widgets. widgetGroups can also be initialized in initializeWidgets
         * @type Array<Array<module:settings/js/widget.Widget>>
         * @default []
         */
        widgetGroups: [],

        /**
         * @desc CSS selector for the DOM element that the widget groups will be attached to
         * @type String
         */
        widgetGroupParent: 'form .row-fluid',

        /**
         * @desc Constructor function for the save modal
         * @type Backbone.View
         * @default ValidateOnSaveModal
         */
        SaveModalConstructor: SaveModal,

        /**
         * @desc Template for the view
         * @type function
         * @param {SettingsPageStrings} strings Strings for the template
         */
        template: _.template(template),

        events: {
            'click .settings-restore': 'handleCancelButton',
            'click button[type="submit"]': 'handleSubmit'
        },

        initialize: function() {
            BasePage.prototype.initialize.apply(this, arguments);

            // TODO: Listenable should take a context
            this.listenTo(listenable(window), 'beforeunload', _.bind(this.handleBeforeUnload, this));
            this.initializeWidgets();
            this.widgets = _.flatten(this.widgetGroups);

            _.each(this.widgets, function(widget) {
                widget.on('validate', function() {
                    this.validate(widget);
                }, this);
            }, this);

            this.router.on(this.routeEvent, this.handleRouting, this);
        },

        /**
         * @desc Renders the settings page. This will render all the widgets and append them to the page
         */
        render: function() {
            this.$el.html(this.template({icon: this.icon, strings: this.strings}));
            this.$form = this.$(this.widgetGroupParent);
            this.$scrollElement = $(this.scrollSelector);

            _.invoke(this.widgets, 'render');

            _.each(this.widgetGroups, function(row) {
                var $newGroup = $('<div></div>').addClass(this.groupClass);

                _.each(row, function(widget) {
                    $newGroup.append(widget.el)
                }, this);

                this.$form.append($newGroup);
            }, this);

            this.configModel.onLoad(this.loadFromConfig, this);
            this.configModel.loaded || this.configModel.fetch();
            this.hasRendered = true;
        },

        /**
         * @desc Returns the combined configuration from all of the widgets
         * @returns {Object}
         */
        getConfig: function() {
            var config = {};

            _.each(this.widgets, function(widget) {
                config[widget.configItem] = widget.getConfig();
            });

            return config;
        },

        /**
         * @returns {boolean} Returns true if the user has unsaved changes, or false otherwise
         */
        canLeavePage: function() {
            if (this.lastSavedConfig) {
                return _.isEqual(this.getConfig(), this.lastSavedConfig);
            }

            return true;
        },

        /**
         * @desc Event listener for window unload events
         * @returns {String|undefined} this.strings.confirmUnload if the user has unsaved changes, or undefined
         * otherwise
         */
        handleBeforeUnload: function() {
            if (!this.canLeavePage()) {
                setTimeout(_.bind(function() {
                    this.vent.navigate(this.routeRoot, {trigger: true});
                }, this), 100);

                return this.strings.confirmUnload;
            }
        },

        /**
         * @desc Opens a confirm modal which will restore changes to configuration if accepted.
         * Called when the user presses the restore changes button
         * @returns {boolean} false
         */
        handleCancelButton: function() {
            confirm({
                cancelClass: '',
                cancelIcon: 'icon-remove',
                cancelText: this.strings.cancelCancel,
                okText: this.strings.cancelOk,
                okClass: 'btn-warning',
                okIcon: 'icon-undo',
                message: this.strings.cancelMessage,
                title: this.strings.cancelTitle,
                okHandler: _.bind(function() {
                    this.loadFromConfig();
                    this.$scrollElement.scrollTop(0);
                }, this)
            });

            return false;
        },

        /**
         * @desc called when navigating to the page.
         * @param configItem The configItem whose widget should be opened and scrolled to
         */
        handleRouting: function(configItem) {
            if (configItem) {
                this.hasRendered || this.render();

                var widget = _.find(this.widgets, function(widget) {
                    return widget.configItem === configItem;
                });

                widget && this.scrollToWidget(widget);
                this.vent.navigate(this.routeRoot, {replace: true, trigger: false});
            }
        },

        /**
         * @desc Opens a {@link module:settings/js/validate-on-save-modal.ValidateOnSaveModal|modal} which allows the
         * user to submit changes to the configuration.
         * @param {Event} e The jQuery event object
         * @returns {boolean} false
         */
        handleSubmit: function(e) {
            e.preventDefault();

            var currentConfig = this.getConfig();
            var passedClientValidation = true;
            var hasScrolled = false;

            _.each(this.widgets, function(widget) {
                var isValid = widget.validateInputs();

                if (!isValid) {
                    passedClientValidation = false;
                    if (!hasScrolled) {
                        this.scrollToWidget(widget);
                        hasScrolled = true;
                    }
                }
            }, this);

            if (passedClientValidation) {
                var serversToValidate = [];

                _.each(this.widgets, function(widget) {
                    if (widget.shouldValidate()) {
                        serversToValidate.push(widget.configItem);
                        widget.lastValidationConfig = currentConfig[widget.configItem];
                    }
                });

                var saveModal = new this.SaveModalConstructor({
                    config: currentConfig,
                    configModel: this.configModel,
                    success: _.bind(function() {
                        this.lastSavedConfig = currentConfig;
                        this.$scrollElement.scrollTop(0);
                        this.hasSavedSettings = true;
                    }, this),
                    strings: this.strings.saveModal
                });

                // ensures validation on save is propagated to associated widget
                saveModal.on('validation', function(validation) {
                    if (validation === 'SUCCESS') {
                        validation = {};

                        _.each(serversToValidate, function(serverName) {
                            validation[serverName] = {valid: true};
                        });
                    }

                    _.each(validation, function(response, serverName) {
                        var widget = _.find(this.widgets, function(widget) {
                            return widget.configItem === serverName;
                        });

                        widget.handleValidation(currentConfig[serverName], response);
                    }, this);

                }, this);
            }

            return false;
        },

        /**
         * @desc Updates each widget with the appropriate config item from the configModel. Called when the config model
         * loads
         */
        loadFromConfig: function() {
            var config = this.configModel.get('config');
            var serversToValidate = [];
            this.$('.settings-description').text(this.strings.description(this.configModel.get('configEnvVariable'), this.configModel.get('configPath')));

            _.each(this.widgets, function(widget) {
                widget.updateConfig(config[widget.configItem]);

                if (widget.shouldValidate()) {
                    serversToValidate.push(widget);
                }
            });

            this.validate(serversToValidate);
            this.lastSavedConfig = this.getConfig();
        },

        /**
         * @desc Scrolls the given widget into view
         * @param {module:settings/js/widget.Widget} widget The widget to scroll to
         */
        scrollToWidget: function(widget) {
            widget.$('.collapse-' + widget.configItem).collapse('show').on('shown', _.bind(function() {
                this.$scrollElement.scrollTop(this.$scrollElement.scrollTop() + widget.$el.position().top - this.$scrollElement.offset().top);
            }, this));
        },

        /**
         * @desc Validate the config in the given widgets by posting to validateUrl.
         * @param {module:settings/js/widget.Widget|module:settings/js/widget.Widget[]} widgets The widgets to validate
         */
        validate: function(widgets) {
            widgets = ensureArray(widgets);
            var config = {};

            _.each(widgets, function(widget) {
                widget.lastValidationConfig = config[widget.configItem] = widget.getConfig();
            });

            $.ajax(this.validateUrl, {
                contentType: 'application/json',
                dataType: 'json',
                type: 'POST',
                data: JSON.stringify(config),
                success: function(response) {
                    _.each(widgets, function(widget) {
                        widget.handleValidation(config[widget.configItem], response[widget.configItem]);
                    });
                }
            });
        }
    });
});
