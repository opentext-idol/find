/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module js-whatever/js/autoload
 */
define([
    '../../../backbone/backbone',
    'js-whatever/js/autoload-methods'
], function(Backbone, autoloadMethods) {

    /**
     * @name module:js-whatever/js/autoload.Autoload
     * @desc A Backbone.Model which incorporates {@link module:js-whatever/js/autoload-methods|AutoloadMethods}
     * @constructor
     * @extends Backbone.Model
     * @abstract
     */
    return Backbone.Model.extend(_.chain(autoloadMethods).clone().extend({
        eventName: 'change'
    }).value());
}); 
