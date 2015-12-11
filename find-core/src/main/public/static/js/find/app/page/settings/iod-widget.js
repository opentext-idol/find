/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'settings/js/server-widget',
    'find/app/model/indexes-collection',
    'text!find/templates/app/page/settings/server-widget.html',
    'text!find/templates/app/page/settings/iod-widget.html',
    'text!find/templates/app/page/settings/indexes-list.html'
], function(ServerWidget, IndexesCollection, serverWidget, template, indexesTemplate) {

    template = _.template(template);
    indexesTemplate = _.template(indexesTemplate);

    return ServerWidget.extend({
        serverTemplate: _.template(serverWidget),

        render: function() {
            ServerWidget.prototype.render.call(this);

            this.$validateButtonParent = this.$('button[name="validate"]').parent();
            this.$validateButtonParent.before(template({strings: this.strings}));

            this.$apikey = this.$('input[name="apikey"]');
            this.$application = this.$('input[name="application"]');
            this.$domain = this.$('input[name="domain"]');
            this.$apiKeyControlGroup = this.$('.form-group').eq(0);
        },

        getConfig: function() {
            var $indexCheckboxes = this.$('input[type="checkbox"]:checked');

            var activeIndexes;

            var domain = this.$domain.val();

            if ($indexCheckboxes.length) {
                var selectedIndexes = _.map($indexCheckboxes, function (input) {
                    return $(input).val();
                });

                activeIndexes = _.chain(this.indexes)
                    .filter(function (index) {
                        return _.contains(selectedIndexes, index.resource);
                    })
                    .map(function(index) {
                        return {
                            domain: index.private ? domain : 'PUBLIC_INDEXES',
                            name: index.resource
                        }
                    })
                    .value();
            }
            else if(this.$('input[type="checkbox"]').length) {
                // no checkboxes ticked
                activeIndexes = [];
            }
            else {
                // checkboxes have not rendered, use previous value
                activeIndexes = this.indexes;
            }

            return {
                apiKey: this.$apikey.val(),
                application: this.$application.val(),
                domain: domain,
                activeIndexes: activeIndexes
            };
        },

        updateConfig: function(config) {
            ServerWidget.prototype.updateConfig.apply(this, arguments);

            this.$apikey.val(config.apiKey);
            this.$application.val(config.application);
            this.$domain.val(config.domain);
            this.indexes = config.activeIndexes;
        },

        validateInputs: function() {
            var isValid = true;

            if (this.shouldValidate()) {
                var config = this.getConfig();

                if (config.apiKey === '') {
                    isValid = false;
                    this.updateInputValidation(this.$apikey);
                }

                if (config.application === '') {
                    isValid = false;
                    this.updateInputValidation(this.$application);
                }

                if (config.domain === '') {
                    isValid = false;
                    this.updateInputValidation(this.$domain);
                }
            }

            return isValid;
        },

        handleValidation: function(config, response) {
            if (_.isEqual(config.iod, this.lastValidationConfig.iod)) {
                this.lastValidation = response.valid;

                if(this.lastValidation && response.data && response.data.indexes) {
                    var privateIndexes = response.data.indexes.resources;

                    _.each(privateIndexes, function(privateIndex) {
                        privateIndex.private = true;
                    });

                    this.indexes = response.data.indexes.publicResources.concat(privateIndexes);
                }
                else {
                    this.indexes = [];
                }

                this.hideValidationInfo();

                this.toggleIndexes();

                this.displayValidationMessage(true, response);

                if(response.data) {
                    _.each(response.data.activeIndexes, function(activeIndex){
                        this.$('[value="' + activeIndex.index + '"]').prop('checked', true);
                    })
                }
            }
        },

        setValidationFormatting: function(state) {
            if (state === 'clear') {
                this.$apiKeyControlGroup.removeClass('success error');
            } else {
                this.$apiKeyControlGroup.addClass(state)
                    .removeClass(state === 'success' ? 'error' : 'success');
            }
        },

        toggleIndexes: function() {
            this.$('.indexes-list-group').remove();

            if(this.indexes) {
                if(this.indexes.length && this.lastValidation) {
                    this.$validateButtonParent.after(indexesTemplate({
                        indexes: this.indexes
                    }));
                }
            }
        }
    });

});
