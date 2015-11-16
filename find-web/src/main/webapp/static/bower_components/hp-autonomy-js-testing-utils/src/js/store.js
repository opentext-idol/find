/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define(function(){

    var storage = {};

    return {

        get: function(key){
            return storage[key];
        },

        set: function(key, value){
            storage[key] = value;
        },

        remove: function(key){
            delete storage[key];
        },

        clear: function() {
            storage = {};
        }

    }
});
