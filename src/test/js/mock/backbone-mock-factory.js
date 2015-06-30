/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone'
], function(Backbone) {

    var getStubHash = function(methods) {
        var output = {};

        _.each(methods, function(method) {
            output[method] = jasmine.createSpy(method);
        });

        return output;
    };

    var getMockConstructor = function(Parent) {
        return function(stubMethods, prototypeProperties) {
            var prototypeStubs = getStubHash(stubMethods);

            var constructor = function() {
                if (constructor === this.constructor) {
                    Constructor.instances.push(this);
                    this.constructorArgs = _.toArray(arguments);
                    _.extend(this, getStubHash(stubMethods));
                }

                Parent.apply(this, arguments);
            };

            var Constructor = Parent.extend(_.extend({
                constructor: constructor
            }, prototypeStubs, prototypeProperties), {
                reset: function() {
                    Constructor.instances = [];

                    _.each(stubMethods, function(method) {
                        Constructor.prototype[method].calls.reset();
                    });
                }
            });

            Constructor.reset();
            return Constructor;
        };
    };

    return {
        getView: getMockConstructor(Backbone.View),
        getModel: getMockConstructor(Backbone.Model),
        getCollection: getMockConstructor(Backbone.Collection)
    };

});