define([
    'backbone',
    'jquery',
    'underscore',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/filters/geography/geography-editor-view.html'
], function (Backbone, $, _, i18n, template) {
    'use strict';

    return Backbone.View.extend({
        template: _.template(template),
        className: 'full-height',

        events: {
        },

        initialize: function (options) {
            options.geography;
        },

        render: function () {
            this.$el.html(this.template({
                i18n: i18n
            }));
        }
    });
});
