/*
 * Copyright 2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

define([
    'backbone',
    'underscore',
    'jquery'
], function(Backbone, _, $) {
    'use strict';

    const dispatcher = _.clone(Backbone.Events);

    $(document).on('keydown', function(evt){
        if (evt.keyCode === 27) {
            // evt.key is 'Escape' in Chrome but 'Esc' in IE11, easier to just work with keycodes.
            dispatcher.trigger('escape', evt);
        }
    })

    return dispatcher;
})
