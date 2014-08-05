define([
    'find/app/page/find-search',
    'text!find/templates/app/page/search/settings-link.html'
], function(FindSearch, settingsLinkTemplate) {

    return FindSearch.extend({

        settingsLinkTemplate: _.template(settingsLinkTemplate),

        render: function() {
            FindSearch.prototype.render.apply(this, arguments);

            this.$('.find-form').append($(this.settingsLinkTemplate()));
        }

    });

});