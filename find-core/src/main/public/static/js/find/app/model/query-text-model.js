/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'find/app/util/search-data-util'
], function (Backbone, searchDataUtil) {

    return Backbone.Model.extend({
        defaults: {
            inputText: '',
            relatedConcepts: []
        },

        makeQueryText: function () {
            return searchDataUtil.makeQueryText(this.get('inputText'), this.get('relatedConcepts'));
        },

        isEmpty: function() {
            return !this.get('inputText') && !this.get('relatedConcepts').length
        }
    });

});
