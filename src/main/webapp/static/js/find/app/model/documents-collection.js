/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore'
], function(Backbone, _) {

    return Backbone.Collection.extend({

        url: '../api/public/search/query-text-index/results',

        initialize: function(models, options) {
            this.indexesCollection = options.indexesCollection;
        },

        sync: function(method, model, options) {
            options = options || {};
            options.traditional = true; // Force "traditional" serialization of query parameters, e.g. index=foo&index=bar, for IOD multi-index support.

            return Backbone.Collection.prototype.sync.call(this, method, model, options);
        },

        parse: function(response) {
            return _.map(response.documents, function(document) {
                document.index = this.indexesCollection.findWhere({name: document.index});

                return document;
            }, this);
        },

        model: Backbone.Model.extend({
            parse: function(response) {
                if (!response.title) {
                    // If there is no title, use the last part of the reference (assuming the reference is a file path)
                    // C:\Documents\file.txt -> file.txt
                    // /home/user/another-file.txt -> another-file.txt
                    var splitReference = response.reference.split(/\/|\\/);
                    var lastPart = _.last(splitReference);

                    if (/\S/.test(lastPart)) {
                        // Use the "file name" if it contains a non whitespace character
                        response.title = lastPart;
                    } else {
                        response.title = response.reference;
                    }
                }

                return response;
            }
        })
    })
});
