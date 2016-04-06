/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'backbone'
], function(_, Backbone) {

    var ModelRegistry = function(modelData) {
        this.modelData = modelData;
    };

    ModelRegistry.prototype.get = function(name) {
        var data = this.modelData[name];

        if (!data.instance) {
            var options = data.options || {};

            _.each(data.dependencies, function(dependency) {
                options[dependency] = this.get(dependency);
            }, this);

            var Constructor = data.Constructor;
            data.instance = new Constructor(data.attributes || (Constructor instanceof Backbone.Model ? {} : []), options);

            if (data.fetch !== false) {
                data.instance.fetch(data.fetchOptions || undefined);
            }
        }

        return data.instance;
    };

    return ModelRegistry;

});
