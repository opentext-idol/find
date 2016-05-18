/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery'
], function($) {

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

    var EventsModule = function() {
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
    };

    var eventsModule = new EventsModule();

    return function() {
        return eventsModule;
    };

});
