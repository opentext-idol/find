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

    /**
     * Add popovers to the given element(s). When the popover is inserted into the DOM, the callback will be called with
     * the popover content element and the trigger element. A load indicator will be displayed in the content element
     * until it is overwritten by the callback.
     * @param {Jquery} $el
     * @param {Function} callback
     */
    return function($el, callback) {
        $el.popover({
            content: smallLoadingTemplate,
            html: true,
            placement: 'bottom',
            trigger: 'hover'
        }).on('inserted.bs.popover', function(e) {
            var $target = $(e.currentTarget);
            callback($target.siblings('.popover').find('.popover-content'), $target);
        });
    };

});
