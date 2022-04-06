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
    'i18n!find/nls/bundle',
    'text!find/templates/app/old-browser-modal.html',
    'underscore',
    'bowser',
    'jquery'
], function(i18n, oldBrowserModal, _, bowser, $) {
    'use strict';

    // do any required feature detection for your app config page here
    // you may wish to update the template to state which features are missing
    function testBrowser() {
        return !(bowser.msie && bowser.version <= 10);
    }

    return function() {
        var deferred = $.Deferred();

        if(!testBrowser()) {
            $(function() {
                var template = _.template(oldBrowserModal, {variable: 'ctx'});

                $(template({i18n: i18n}))
                    .modal({
                        backdrop: 'static',
                        keyboard: false,
                        show: true
                    })
                    .on('hidden.bs.modal', function() {
                        deferred.reject();
                    });
            });
        }
        else {
            deferred.resolve();
        }

        return deferred.promise();
    }
});
