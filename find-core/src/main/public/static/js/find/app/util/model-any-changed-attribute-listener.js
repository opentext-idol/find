/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define(function() {

    /**
     * Listen to change events on the given model, calling the callback if one of the given attributes has changed. The
     * callback is called with standard Backbone change event arguments in the context of the listener.
     * @param {Backbone.Events} listener
     * @param {Backbone.Model} model
     * @param {String[]} attributes
     * @param {Function} callback
     * @returns {Function} The change callback added as a listener on the model; useful for removing the callback later
     */
    return function(listener, model, attributes, callback) {
        var inTargetAttributes = _.partial(_.contains, attributes);

        function changeCallback() {
            var changedAttributes = _.keys(model.changedAttributes());

            if (_.any(changedAttributes, inTargetAttributes)) {
                callback.apply(listener, arguments);
            }
        }

        listener.listenTo(model, 'change', changeCallback);
        return changeCallback;
    };

});
