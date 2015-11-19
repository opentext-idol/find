/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone'
], function(Backbone) {

    // Check if we are using jasmine 2; if we are, spies have different behaviour
    var jasmine2 = window.jasmine && parseInt(window.jasmine.version, 10) >= 2;

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
                        var prototypeMethod = Constructor.prototype[method];

                        if (jasmine2) {
                            prototypeMethod.calls.reset();
                        } else {
                            prototypeMethod.reset();
                        }
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