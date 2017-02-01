/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'i18n!find/nls/bundle'
], function(i18n) {
    'use strict';

    return function(collection) {
        return {
            inputClass: 'concepts-controls',
            placeholder: i18n['app.conceptBoxPlaceholder'],

            initialize: function(view) {
                view.listenTo(collection, 'update change', view.updateText);
            },
            onTextUpdate: function(updatedText) {
                if(updatedText && updatedText !== '*') {
                    collection.unshift({concepts: [updatedText]});
                }
            },
            onExternalUpdate: function() {
                return '';
            },
            inFocus: function($button) {
                $button.addClass('in-focus');
            },
            onBlur: function($button) {
                $button.removeClass('in-focus');
            }
        }
    }
});
