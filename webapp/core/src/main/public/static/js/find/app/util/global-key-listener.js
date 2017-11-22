/*
 * Copyright 2017 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'jquery'
], function(Backbone, _, $) {
    'use strict';

    const dispatcher = _.clone(Backbone.Events);

    $(document).on('keydown', function(evt){
        if (evt.key === 'Escape') {
            dispatcher.trigger('escape', evt);
        }
    })

    return dispatcher;
})