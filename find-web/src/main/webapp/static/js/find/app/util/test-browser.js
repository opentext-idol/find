/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'i18n!find/nls/bundle',
    'text!find/templates/app/old-browser-modal.html',
    'bootstrap',
    'underscore'
], function(i18n, oldBrowserModal){

    // do any required feature detection for your app config page here
    // you may wish to update the template to state which features are missing
    // browser detection is bad, mkay
    function testBrowser() {
        return true;
    }

    return function(){
        if(!testBrowser()) {
            $(function() {
                $(_.template(oldBrowserModal, { i18n: i18n }, {variable: 'ctx'})).modal({show: true});
            });
        }
    }
});
