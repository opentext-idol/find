/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/loading-spinner.html',
    'bootstrap'
], function(_, $, i18n, loadingTemplate) {

    var smallLoadingTemplate = _.template(loadingTemplate)({i18n: i18n, large: false});
    var initialContent = '<div class="popover-content-inner">' + smallLoadingTemplate + '</div>';

    /**
     * Add popovers to the given element(s). When the popover is inserted into the DOM, the callback will be called with
     * the popover content element and the trigger element. A load indicator will be displayed in the content element
     * until it is overwritten by the callback.
     * @param {Jquery} $el
     * @param {Function} callback
     */
    return function($el, trigger, callback, hideCallback) {
        $el.popover({
            content: initialContent,
            html: true,
            placement: 'bottom',
            trigger: trigger
        }).on('inserted.bs.popover', function(e) {
            var $target = $(e.currentTarget);

            // Don't pass the bootstrap .popover-content element to the caller since this element can be preserved between
            // popover openings. The .popover-content-inner element will be destroyed each time, so we don't have to track
            // the state ourselves.
            callback($target.siblings('.popover').find('.popover-content-inner'), $target);
        }).on('hidden.bs.popover', function() {
            if(hideCallback){
                hideCallback();
            }
        });
    };

});
