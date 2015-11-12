define([
    'settings/js/validate-on-save-modal',
    'text!find/templates/app/page/settings/validate-on-save-modal.html'
], function(SaveModal, template) {

    return SaveModal.extend({
        className: 'modal fade',

        template: _.template(template)
    });
});