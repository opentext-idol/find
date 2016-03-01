/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/settings/aci-widget',
    'find/app/page/settings/enable-view',
    'text!find/templates/app/page/settings/query-manipulation-widget.html'
], function(AciWidget, EnableView, queryManipulationTemplate) {

    return AciWidget.extend({

        queryManipulationTemplate: _.template(queryManipulationTemplate),

        initialize: function() {
            AciWidget.prototype.initialize.apply(this, arguments);

            this.enableView = new EnableView({
                enableIcon: 'fa fa-file',
                strings: this.strings
            })
        },

        render: function() {
            AciWidget.prototype.render.apply(this, arguments);

            this.enableView.render();
            var $validateButtonParent = this.$('button[name=validate]').parent();
            $validateButtonParent.before(this.enableView.el);

            $validateButtonParent.before(this.queryManipulationTemplate({
                strings: this.strings
            }));

            this.$blacklist = this.$('.blacklist-input');
            this.$typeahead = this.$('.typeahead-input');
            this.$expandQuery = this.$('.expand-query-input');

            this.listenTo(this.enableView, 'change', function() {
                this.$('.settings-required-flag').toggleClass('hide', !this.enableView.getConfig());
            })
        },

        getConfig: function() {
            return {
                blacklist: this.$blacklist.val(),
                expandQuery: this.$expandQuery.prop('checked'),
                enabled: this.enableView.getConfig(),
                server: AciWidget.prototype.getConfig.call(this),
                typeAheadMode: this.$typeahead.val()
            }
        },

        updateConfig: function(config) {
            AciWidget.prototype.updateConfig.call(this, config.server);

            this.enableView.updateConfig(config.enabled);

            this.$blacklist.val(config.blacklist);
            this.$typeahead.val(config.typeAheadMode);
            this.$expandQuery.prop('checked', config.expandQuery);
        },

        shouldValidate: function() {
            return this.enableView.getConfig();
        }
    });

});