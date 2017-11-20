/*
 * Copyright 2017 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    'i18n!find/nls/bundle',
    'find/app/configuration'
], function(_, $, i18n, configuration) {
    'use strict';

    function SelectionEntitySearch() {
        let $hover;

        function onSelectionChange(evt) {
            const sel = window.getSelection();

            if (sel.rangeCount !== 1) {
                return;
            }

            const range = sel.getRangeAt(0);
            const text = range.toString();

            const $selectEnd = $(sel.focusNode);

            if ($selectEnd.closest('.selection-entity').length) {
                // We're doing selection stuff on the selection popup, we don't want to trigger changes
                //  otherwise the selection popup will disappear.
                return;
            }

            if (text) {
                const $summary = $selectEnd.closest('.result-summary');

                if ($summary.length && $(sel.anchorNode).closest('.result-summary').is($summary)) {
                    // We're in a summary, try fetching stuff
                    const mocktext = 'this is some mock text about ' + text;

                    if ($hover) {
                        $hover.remove();
                    }


                    const bounds = range.getBoundingClientRect();

                    const html = _.escape(mocktext) + '<a href="www.google.com" style="pointer-events: all">google</a>';

                    $hover = $('<div class="selection-entity" style=" z-index:9999; max-width: 500px; position: fixed; border: solid 1px darkgreen; background: rgba(0,255,0,0.1)">').css({
                        top: bounds.bottom,
                        left: bounds.left
                    }).html(html)
                        .appendTo($summary);

                    return;
                }
            }

            if ($hover) {
                $hover.remove();
                $hover = null;
            }
        }

        $(document).on('selectionchange', onSelectionChange)

        this.stopListening = function(){
            $(document).off('selectionchange', onSelectionChange);

            if ($hover) {
                $hover.remove();
                $hover = null;
            }
        }
    }

    return SelectionEntitySearch;
});
