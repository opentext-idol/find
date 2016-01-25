define([
    'backbone',
    'underscore',
    'jquery',
    'find/app/util/confirm-view',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/saved-searches/saved-search-options.html'
], function(Backbone, _, $, Confirm, i18n, template) {
    "use strict";

    return Backbone.View.extend({
        template: _.template(template),

        events: {
            'click .saved-search-option-delete': function(e) {
                e.preventDefault();

                new Confirm({
                    cancelClass: 'btn-default',
                    cancelIcon: '',
                    cancelText: i18n['app.cancel'],
                    okText: i18n['app.delete'],
                    okClass: 'btn-danger',
                    okIcon: '',
                    message: i18n['search.savedSearches.confirm.deleteMessage'](this.model.get('title')),
                    title: i18n['search.savedSearches.confirm.deleteMessage.title'],
                    hiddenEvent: 'hidden.bs.modal',
                    okHandler: _.bind(function() {
                        this.model.destroy();
                    }, this)
                });

            },
            'click .saved-search-option-reset': function(e) {
                e.preventDefault();

                new Confirm({
                    cancelClass: 'btn-default',
                    cancelIcon: '',
                    cancelText: i18n['app.cancel'],
                    okText: i18n['app.reset'],
                    okClass: 'btn-danger',
                    okIcon: '',
                    message: i18n['search.savedSearches.confirm.resetMessage'](this.model.get('title')),
                    title: i18n['search.savedSearches.confirm.resetMessage.title'],
                    hiddenEvent: 'hidden.bs.modal',
                    okHandler: _.bind(function() {
                        //TODO: populate when we have more info
                    }, this)
                });

            }
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n,
                model: this.model
            }));
        }
    });
});
