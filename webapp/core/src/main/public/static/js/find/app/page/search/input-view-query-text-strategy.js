/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'i18n!find/nls/bundle',
    'jquery'
], function(i18n, $) {
    'use strict';

    return function(model) {
        return {
            inputClass: 'search-controls',
            placeholder: i18n['app.searchPlaceholder'],

            initialize: function(view) {
                view.listenTo(model, 'change:inputText', view.updateText);
            },
            onTextUpdate: function(updatedText) {
                model.set({inputText: updatedText});
            },
            onExternalUpdate: function() {
                return model.get('inputText');
            },
            inFocus: $.noop,
            onBlur: $.noop
        }
    }
});
