/*
 * Copyright 2017 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    'find/app/util/global-key-listener',
    'find/idol/app/model/answer-bank/idol-answered-questions-collection',
    'find/idol/app/model/entitysearch/entity-search-collection',
    'text!find/templates/app/page/loading-spinner.html',
    'i18n!find/nls/bundle'
], function(_, $, globalKeyListener, AnsweredQuestionsCollection, EntitySearchCollection, loadingSpinnerTemplate, i18n) {
    'use strict';

    const loadingHtml = _.template(loadingSpinnerTemplate)({i18n: i18n, large: false});

    function SelectionEntitySearch(options) {
        const documentRenderer = options.documentRenderer;
        // You can control which elements the popup will appear on by adjusting this selector.
        const selector = options.selector || '.main-results-container,.parametric-value-element,.dt-bootstrap,.trending-chart,.sunburst,.entity-topic-map,.leaflet-popup-content,.document-detail-tabs-content';
        const debounceMillis = options.debounceMillis || 250;
        let element = options.element || document.body;

        let $hover;

        const answeredQuestionsCollection = new AnsweredQuestionsCollection();
        answeredQuestionsCollection.url = 'api/public/answer/ask-demo';

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
            }).done(function(){
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

            const top = bounds.bottom + 10;
            const left = bounds.left;

            $hover = $('<div class="selection-entity">').css({
                top: top,
                left: left
            }).html(html).appendTo(element);

            function reposition(){
                if (!$hover) {
                    return;
                }

                if ($hover.height() + top > window.innerHeight) {
                    // If the popup goes below the page, show it above the selection instead of below.
                    $hover.css({
                        top: 'auto',
                        bottom: window.innerHeight - bounds.top + 10
                    })
                }

                if ($hover.width() + left > window.innerWidth) {
                    // If the selection is off the right edge of the screen, lock it to the right edge of the screen.
                    $hover.css({
                        left: 'auto',
                        right: 10
                    })
                }
            }

            reposition();

            $hover.find('img').on('load', reposition);
            $hover.find('input.entity-search-question').closest('form').on('submit', function(evt){
                const $input = $(evt.currentTarget).find('input.entity-search-question');
                const text = $input.val().trim();

                const $answerEl = $hover.find('.entity-search-messages');

                if (text && $answerEl.length) {
                    $input.val('');
                    $('<div class="entity-search-user">').text(text).appendTo($answerEl);
                    scrollDown();
                    reposition();

                    const questionText = /^(what|who|how|where|why)/i.exec(text) ? text : 'what is the ' + text + ' of ' + $input.data('context')

                    answeredQuestionsCollection.fetch({
                        data: {
                            text: questionText,
                            maxResults: 1
                        },
                        reset: true,
                        success: _.bind(function() {
                            const answer = answeredQuestionsCollection.map('answer').join('');
                            $('<div class="entity-search-server">').text(answer || i18n['entitySearch.template.question.answerMissing']).appendTo($answerEl);
                            scrollDown();
                            reposition();
                        }, this),
                        error: _.bind(function() {
                            $('<div class="entity-search-server">').text(i18n['entitySearch.template.question.answerError']).appendTo($answerEl);
                            scrollDown();
                            reposition();
                        }, this)
                    }, this);


                }

                function scrollDown() {
                    const dom = $answerEl[0];
                    if (dom.scrollHeight) {
                        dom.scrollTop = dom.scrollHeight;
                    }
                }

                return false;
            })
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
                const $summary = $selectEnd.closest(selector);

                if ($summary.length && $(sel.anchorNode).closest(selector).is($summary)) {
                    // We're in a summary, try fetching stuff
                    loadModel(text, $summary, range.getBoundingClientRect());
                    return;
                }
            }

            clearIndicator();
        }

        const debounced = _.debounce(onSelectionChange, debounceMillis);

        $(document)
            .on('selectionchange', debounced)
            .on('click', '.selection-entity-close', clearIndicator)

        globalKeyListener.on('escape', clearIndicator);

        this.stopListening = function(){
            $(document)
                .off('selectionchange', debounced)
                .off('click', '.selection-entity-close', clearIndicator);
            globalKeyListener.off('escape', clearIndicator);
            clearIndicator();
        }

        this.setElement = function(dom) {
            element = dom;
        }
    }

    return SelectionEntitySearch;
});
