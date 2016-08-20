/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'backbone',
    'jquery',
    'find/app/vent',
    'find/app/configuration',
    'i18n!find/nls/bundle',
    'text!find/templates/app/navigation.html'
], function(_, Backbone, $, vent, configuration, i18n, template) {

    return Backbone.View.extend({

        template: _.template(template, {variable: 'data'}),

        menuItems: _.constant(''),

        events: {
            'click a[data-pagename]': function(event) {
                if (event.which !== 2) {
                    event.preventDefault();
                    var pageName = $(event.target).attr('data-pagename');
                    vent.navigate('find/' + pageName);
                }
            }
        },

        initialize: function(options) {
            this.pageData = options.pageData;
            this.listenTo(options.router, 'route:find', this.selectPage);
        },

        render: function() {
            var pages = _.chain(this.pageData)
                .map(function(data, name) {
                    return _.extend({pageName: name}, data);
                })
                .sortBy('order')
                .value();

            this.$el.html(this.template({
                i18n: i18n,
                menuItems: this.menuItems,
                pages: pages,
                username: configuration().username
            }));
        },

        selectPage: function(pageName) {
            this.$('li').removeClass('active');
            this.$('li[data-pagename="' + pageName + '"]').addClass('active');
        }
    });

});
