/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone'
], function(Backbone) {

    return Backbone.Collection.extend({

        url: '../api/search/query-text-index',

	    sync: function(method, model, options) {
		    options = options || {}
		    options.traditional = true // Force "traditional" serialization of query parameters, e.g. index=foo&index=bar, for IOD multi-index support.

		    return Backbone.Collection.prototype.sync.call(this, method, model, options)
	    }
    })
});
