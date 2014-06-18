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