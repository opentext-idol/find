/*
 * (c) Copyright 2014-2015 Micro Focus or one of its affiliates.
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
            // can't use auto placement as we modify the content after popover creation
            // see https://github.com/twbs/bootstrap/issues/1833#issuecomment-17092775
            placement: function (tip, el) {
                var offset = $(el).offset();
                var height = $(document).outerHeight();

                // position after which we use top placement
                var heightThreshold = 0.5 * height;

                return heightThreshold > offset.top ? 'bottom' : 'top';
            },
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
