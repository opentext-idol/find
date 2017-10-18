/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'settings/js/widgets/query-manipulation-widget',
    'find/app/page/settings/enable-view',
    'text!find/templates/app/page/settings/query-manipulation-widget-extensions.html',
    'text!find/templates/app/page/settings/widget.html',
    'text!find/templates/app/page/settings/server-widget.html',
    'text!find/templates/app/page/settings/aci-widget.html'
], function(_, QueryManipulationWidget, EnableView, extensionsTemplate, widgetTemplate, serverTemplate, aciTemplate) {
    'use strict';

    return QueryManipulationWidget.extend({
        className: 'panel-group',
        controlGroupClass: 'form-group',
        formControlClass: 'form-control',
        errorClass: 'has-error',
        successClass: 'has-success',

        aciTemplate: _.template(aciTemplate),
        serverTemplate: _.template(serverTemplate),
        widgetTemplate: _.template(widgetTemplate),

        extensionsTemplate: _.template(extensionsTemplate),

        initialize: function() {
            QueryManipulationWidget.prototype.initialize.apply(this, arguments);

            this.enableView = new EnableView({
                enableIcon: 'fa fa-file',
                strings: this.strings
            })
        },

        render: function() {
            QueryManipulationWidget.prototype.render.apply(this);

            this.enableView.render();
            var $validateButtonParent = this.$('.typeahead-mode').parent();
            $validateButtonParent.before(this.enableView.el);

            $validateButtonParent.before(this.extensionsTemplate({
                strings: this.strings
            }));

            this.$blacklist = this.$('.blacklist-input');
            this.$expandQuery = this.$('.expand-query-input');
            this.$synonymDatabaseMatch = this.$('.synonym-database-match-input');

            this.listenTo(this.enableView, 'change', function() {
                this.$('.settings-required-flag').toggleClass('hide', !this.enableView.getConfig());
            })
        },

        getConfig: function() {
            return _.extend({
                blacklist: this.$blacklist.val(),
                expandQuery: this.$expandQuery.prop('checked'),
                synonymDatabaseMatch: this.$synonymDatabaseMatch.prop('checked'),
                enabled: this.enableView.getConfig()
            }, QueryManipulationWidget.prototype.getConfig.call(this))
        },

        updateConfig: function(config) {
            QueryManipulationWidget.prototype.updateConfig.call(this, config);

            this.enableView.updateConfig(config.enabled);

            this.$blacklist.val(config.blacklist);
            this.$expandQuery.prop('checked', config.expandQuery);
            this.$synonymDatabaseMatch.prop('checked', config.synonymDatabaseMatch !== false);
        },

        shouldValidate: function() {
            return this.enableView.getConfig();
        }
    });
});
