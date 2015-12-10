/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'js-whatever/js/navigation',
    'find/app/router',
    'find/app/configuration',
    'i18n!find/nls/bundle',
    'text!find/templates/app/public-navigation.html'
], function(Navigation, router, configuration, i18n, template) {

    return Navigation.extend({

        event: 'route:find',

        router: router,

        template: _.template(template, {variable: 'data'}),

        menuItems: $.noop,

        getTemplateParameters: function() {
            return {
                i18n: i18n,
                menuItems: this.menuItems,
                pages: this.pages.pages,
                username: configuration().username
            }
        }
    })

});
