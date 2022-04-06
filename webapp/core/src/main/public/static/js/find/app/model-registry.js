/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

define([
    'underscore',
    'backbone',
    'jquery'
], function(_, Backbone, $) {

    var ModelRegistry = function(modelData) {
        this.modelData = modelData;
    };

    ModelRegistry.prototype.get = function(name) {
        return getInternal.call(this, name).instance;
    };

    function getInternal(name) {
        var data = this.modelData[name];

        if (!data.instance) {
            var options = data.options || {};
            var dependencyPromises = [];

            _.each(data.dependencies, function(dependencyName) {
                var output = getInternal.call(this, dependencyName);

                if (output.fetchPromise) {
                    dependencyPromises.push(output.fetchPromise);
                }

                options[dependencyName] = output.instance;
            }, this);

            var Constructor = data.Constructor;
            data.instance = new Constructor(data.attributes || (Constructor instanceof Backbone.Model ? {} : []), options);

            if (data.fetch !== false) {
                data.fetchPromise = $.when.apply($, dependencyPromises)
                    .then(function() {
                        return data.instance.fetch(data.fetchOptions || undefined);
                    });
            }
        }

        return {
            instance: data.instance,
            fetchPromise: data.fetchPromise
        };
    }

    return ModelRegistry;

});
