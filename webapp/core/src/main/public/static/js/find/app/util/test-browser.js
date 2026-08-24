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

const i18n = require('find/nls/bundle');
const oldBrowserModal = require('find/templates/app/old-browser-modal.html');
const _ = require('underscore');
const bowser = require('bowser');
const $ = require('jquery');

// do any required feature detection for your app config page here
// you may wish to update the template to state which features are missing
function testBrowser() {
    return !(bowser.msie && bowser.version <= 10);
}

module.exports = function() {
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
    };

