define([
    'js-whatever/js/confirm-view',
    'text!find/templates/app/page/settings/confirm-modal.html'
], function(Confirm, confirmTemplate) {

    return Confirm.extend({
        className: 'modal fade',
        template: _.template(confirmTemplate)
    });

});