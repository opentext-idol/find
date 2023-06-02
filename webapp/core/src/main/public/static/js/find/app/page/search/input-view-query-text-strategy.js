/*
 * Copyright 2016 Open Text.
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
