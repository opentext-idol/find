define([
    'underscore',
    'backbone-base'
], function(_, Backbone) {
    var oldSync = Backbone.sync;

    Backbone.sync = _.wrap(oldSync, function(original, method, model, options) {
        options = options || {};
        var error = options.error;

        options.error = function (jqXHR, status, message) {
            if (jqXHR.status === 403) {
                // refresh the page - the filters should then redirect to the login screen
                window.location.reload();
            }
            else if (error) {
                error(jqXHR, status, message);
            }
        };

        return original.call(Backbone, method, model, options);
    });

    return Backbone;
});