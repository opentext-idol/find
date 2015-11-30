/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module js-whatever/js/autoload-collection
 */
define([
    'backbone',
    'js-whatever/js/autoload-methods'
], function(Backbone, autoloadMethods) {

    /**
     * @name module:js-whatever/js/autoload-collection.AutoloadCollection
     * @desc A Backbone.Collection which incorporates {@link module:js-whatever/js/autoload-methods|AutoloadMethods}
     * @constructor
     * @extends Backbone.Collection
     * @abstract
     */
    return Backbone.Collection.extend(_.chain(autoloadMethods).clone().extend({
        eventName: 'reset add change remove'
    }).value());
});
