/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery'
], function($) {
    'use strict';

    // holds all previously generated modules
    var cache = {};

    // holds the last used module
    var active = null;

    // holds a default module to use for the case when no id is supplied and no active module is available
    var defaultModule;

    var log = function(event) {
        $.ajax('../api/public/stats', {
            contentType: 'application/json',
            data: JSON.stringify(event),
            method: 'POST'
        });
    };

    var types = {
        ABANDONMENT: 'abandonment',
        CLICK_THROUGH: 'clickthrough',
        PAGE: 'page'
    };

    var clickTypes = {
        FULL_PREVIEW: 'full_preview',
        ORIGINAL: 'original',
        PREVIEW: 'preview'
    };

    var EventsModule = function(id) {
        var search = '';
        var position = -1;
        var abandonments = null;

        var unAbandon = function(type) {
            abandonments[type] = false;

            log({
                'click-type': type,
                position: position,
                search: search,
                type: types.CLICK_THROUGH
            })
        };

        var logAbandonments = function() {
            if (abandonments != null) {
                _.each(abandonments, function(value, type) {
                    if (value) {
                        log({
                            'click-type': type,
                            search: search,
                            type: types.ABANDONMENT
                        })
                    }
                });
            }
        };

        $(window).on('unload', logAbandonments);

        this.reset = function(text, abandon) {
            // allows argument to be omitted
            if (abandon !== false) {
                logAbandonments();
            }

            search = text;
            position = -1;

            abandonments = _.chain(clickTypes)
                .invert()
                .mapObject(_.constant(true))
                .value();
        };

        this.preview = function(newPosition) {
            position = newPosition;

            unAbandon(clickTypes.PREVIEW);
        };

        this.fullPreview = function() {
            if (position !== -1) {
                unAbandon(clickTypes.FULL_PREVIEW);
            }
        };

        this.original = function() {
            if (position !== -1) {
                unAbandon(clickTypes.ORIGINAL);
            }
        };

        this.page = function(page) {
            log({
                page: page,
                search: search,
                type: types.PAGE
            })
        };

        // if calling this don't retain a reference to this object
        this.abandon = function() {
            logAbandonments();

            delete cache[id];

            if (this === active) {
                active = defaultModule;
            }

            // we've logged these abandonments, so don't log them again on unload
            if (this !== defaultModule) {
                $(window).off('unload', logAbandonments);
            }
        }
    };

    defaultModule = new EventsModule();

    return function(id) {
        if (id) {
            // if we've not seen this id before, create a new module
            if (!cache[id]) {
                cache[id] = new EventsModule(id);
            }

            active = cache[id];
        }

        // if no active module, return the default module
        if (!active) {
            active = defaultModule;
        }

        return active;
    };

});
