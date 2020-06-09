/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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
