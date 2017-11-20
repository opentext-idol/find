/*
 * Copyright 2017 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    'find/idol/app/model/entitysearch/entity-search-collection',
    'text!find/templates/app/page/loading-spinner.html',
    'i18n!find/nls/bundle'
], function(_, $, EntitySearchCollection, loadingSpinnerTemplate, i18n) {
    'use strict';

    const loadingHtml = _.template(loadingSpinnerTemplate)({i18n: i18n, large: false});

    function SelectionEntitySearch(options) {
        const documentRenderer = options.documentRenderer;
        let element = options.element || document.body;

        let $hover;

        const entityModels = new EntitySearchCollection();
        let lastQueryText, lastFetch;

        function loadModel(text, $summary, bounds) {
            if (lastFetch && lastQueryText !== text) {
                lastFetch.abort();
            }

            lastQueryText = text;

            updateIndicator(loadingHtml, bounds);

            lastFetch = entityModels.fetch({
                data: { text: text }
            }).done(function(models){
                if (text === lastQueryText && entityModels.length) {
                    const result = entityModels.first();
                    const html = documentRenderer.renderEntity(result);

                    updateIndicator(html, bounds);
                }
                else {
                    clearIndicator()
                }
            }).fail(function(){
                clearIndicator();
            })
        }

        function clearIndicator() {
            if ($hover) {
                $hover.remove();
                $hover = null;
            }
        }

        function updateIndicator(html, bounds){
            clearIndicator();

            $hover = $('<div class="selection-entity" style=" z-index:1; max-width: 500px; position: fixed; border: solid 1px darkgreen; background: rgba(0,255,0,0.1)">').css({
                top: bounds.bottom + 10,
                left: bounds.left
            }).html(html).appendTo(element);
        }

        function onSelectionChange() {
            const sel = window.getSelection();

            if (sel.rangeCount !== 1) {
                return;
            }

            const range = sel.getRangeAt(0);
            const text = range.toString().trim();

            const $selectEnd = $(sel.focusNode);

            if ($selectEnd.closest('.selection-entity').length) {
                // We're doing selection stuff on the selection popup, we don't want to trigger changes
                //  otherwise the selection popup will disappear.
                return;
            }

            if (text && text.length >= 2) {
                const $summary = $selectEnd.closest('.result-summary');

                if ($summary.length && $(sel.anchorNode).closest('.result-summary').is($summary)) {
                    // We're in a summary, try fetching stuff
                    loadModel(text, $summary, range.getBoundingClientRect());
                    return;
                }
            }

            clearIndicator();
        }

        $(document).on('selectionchange', onSelectionChange)

        this.stopListening = function(){
            $(document).off('selectionchange', onSelectionChange);
            clearIndicator();
        }

        this.setElement = function(dom) {
            element = dom;
        }
    }

    return SelectionEntitySearch;
});
