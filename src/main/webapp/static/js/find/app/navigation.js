define([
    'js-utils/js/navigation',
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
                i18n: i18n,
                pages: this.pages.pages
            }
        }

    })

});