/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'settings/js/widget',
    'find/app/page/settings/enable-view',
    'text!find/templates/app/page/settings/widget.html',
    'text!find/templates/app/page/settings/map-widget.html'
], function(Widget, EnableView, widgetTemplate, template) {

    return Widget.extend({
        widgetTemplate: _.template(widgetTemplate),
        template: _.template(template),

        initialize: function() {
            Widget.prototype.initialize.apply(this, arguments);

            this.enableView = new EnableView({
                className: 'form-group m-t-sm',
                enableIcon: 'fa fa-file',
                strings: this.strings
            })
        },

        render: function() {
            Widget.prototype.render.apply(this, arguments);

            this.$content.html(this.template({
                strings: this.strings
            }));

            this.enableView.render();

            this.$content.append(this.enableView.$el);

            this.$url = this.$('.tileserver-url-input');
            this.$attribution = this.$('.attribution-input');
        },

        getConfig: function() {
            return {
                attribution: this.$attribution.val(),
                enabled: this.enableView.getConfig(),
                tileUrlTemplate: this.$url.val()
            }
        },

        updateConfig: function(config) {
            this.enableView.updateConfig(config.enabled);

            this.$attribution.val(config.attribution);
            this.$url.val(config.tileUrlTemplate);
        }
    });

});