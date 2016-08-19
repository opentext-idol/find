/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'backbone-base',
    'find/app/util/logout'
], function(_, Backbone, logout) {
    var oldSync = Backbone.sync;

    Backbone.sync = _.wrap(oldSync, function(original, method, model, options) {
        options = options || {};
        var error = options.error;

        options.error = function (jqXHR, status, message) {
            if (jqXHR.status === 401) {
                logout('../logout');
            }
            else if (jqXHR.status === 403) {
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
