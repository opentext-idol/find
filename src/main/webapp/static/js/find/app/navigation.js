/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'js-whatever/js/navigation',
    'find/app/router',
    'i18n!find/nls/bundle',
    'text!find/templates/app/navigation.html'
], function(Navigation, router, i18n, template) {
    return Navigation.extend({

        event: 'route:find',

        router: router,

        template: _.template(template),

        getTemplateParameters: function() {
            return {
                i18n: i18n
            }
        }
    })
});
