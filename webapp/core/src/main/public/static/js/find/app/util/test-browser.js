/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'i18n!find/nls/bundle',
    'text!find/templates/app/old-browser-modal.html',
    'underscore',
    'bowser'
], function(i18n, oldBrowserModal, _, bowser){

    // do any required feature detection for your app config page here
    // you may wish to update the template to state which features are missing
    function testBrowser() {
        return !(bowser.msie && bowser.version <= 10);
    }

    return function(){
        var deferred = $.Deferred();

        if(!testBrowser()) {
            $(function() {
                var template = _.template(oldBrowserModal, { variable: 'ctx' });

                $(template({ i18n: i18n }))
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
