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
    'find/app/page/search/template-helpers/pretty-print-number-helper',
    'text!find/templates/app/page/loading-spinner.html',
    'i18n!find/nls/bundle'
], function(_, $, globalKeyListener, AnsweredQuestionsCollection, EntitySearchCollection, prettyPrintNumberHelper, loadingSpinnerTemplate, i18n) {
    'use strict';

    const loadingHtml = _.template(loadingSpinnerTemplate)({i18n: i18n, large: false});

    function SelectionEntitySearch(options) {
        const documentRenderer = options.documentRenderer;
        const answerServer = options.answerServer;
        // You can control which elements the popup will appear on by adjusting this selector.
        const selector = options.selector || '.main-results-container,.parametric-value-element,.dt-bootstrap,.trending-chart,.sunburst,.entity-topic-map,.leaflet-popup-content,.document-detail-tabs-content,.entity-search-messages';
        const debounceMillis = options.debounceMillis || 250;
        let element = options.element || document.body;

        const entityModels = new EntitySearchCollection();
        let lastQueryText, lastFetch;

        function loadModel(text, bounds) {
            if (lastFetch && lastQueryText !== text) {
                lastFetch.abort();
            }

            lastQueryText = text;

            const $hover = $('<div class="selection-entity first-appearance">');
            $hover.one('mouseover', function(){
                $hover.removeClass('first-appearance');
            })

            updateIndicator($hover, loadingHtml, bounds, null);

            lastFetch = entityModels.fetch({
                data: { text: text }
            }).done(function(){
                if (text === lastQueryText && entityModels.length) {
                    const result = entityModels.first();
                    const html = documentRenderer.renderEntity(result);

                    $hover.addClass('first-appearance').one('mouseover', function(){
                        $hover.removeClass('first-appearance');
                    })

                    updateIndicator($hover, html, bounds, result.get('title'));
                }
                else {
                    clearIndicator($hover)
                }
            }).fail(function(){
                clearIndicator($hover);
            })
        }

        function clearIndicator($hover) {
            $hover.remove();
        }

        function clearAllIndicators() {
            // We have to clear all selections which were triggered by other selections
            $('.selection-entity').remove();
        }

        function clearClickedIndicator(e) {
            const $closest = $(e.currentTarget).closest('.selection-entity');

            $closest.remove();
        }

        function updateIndicator($hover, html, bounds, context){
            const top = bounds.bottom + 10;
            const left = bounds.left;

            let dragLastX, dragLastY, dragX, dragY, dragging = false;

            function moveElement(evt) {
                if (!dragging) {
                    $hover.addClass('selection-entity-drag');

                    const offset = $hover.offset();
                    dragX = offset.left;
                    dragY = offset.top;
                    dragLastX = evt.pageX;
                    dragLastY = evt.pageY;
                    $hover.css({
                        bottom: 'auto',
                        right: 'auto'
                    })
                }

                dragging = true;

                dragX += evt.pageX - dragLastX;
                dragY += evt.pageY - dragLastY;
                dragLastX = evt.pageX;
                dragLastY = evt.pageY;

                $hover.addClass('selection-entity-drag')
                    .css({
                        left: dragX,
                        top: dragY
                    });
            }

            function onMouseUp() {
                dragging = false;
                $(document)
                    .off('mousemove', moveElement)
                    .off('mouseup', onMouseUp)
            }

            $hover.css({
                top: top,
                left: left
            }).html(html).appendTo(element).on('mousedown', function(evt){
                if ($(evt.target).closest('.entity-search-controls,.entity-search-messages').length) {
                    // Prevent drag start if they're trying to select text in the conversation history.
                    return
                }

                dragLastX = evt.screenX;
                dragLastY = evt.screenY;
                $(document)
                    .on('mousemove', moveElement)
                    .on('mouseup', onMouseUp);
            });

            function reposition(){
                if ($hover.hasClass('selection-entity-drag')) {
                    return;
                }

                if ($hover.height() + top > window.innerHeight) {
                    // If the popup goes below the page, show it above the selection instead of below,
                    // except if that would take it off the top edge of the screen, in which case just lock it to the bottom.
                    const desiredBottom = window.innerHeight - bounds.top + 10;

                    if (desiredBottom + $hover.height() < window.innerHeight) {
                        $hover.css({
                            top: 'auto',
                            bottom: desiredBottom
                        })
                    }
                    else {
                        $hover.css({
                            top: 'auto',
                            bottom: 10
                        })
                    }
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

            $hover.find('img').on('load', reposition).attr('draggable', false);

            if (!answerServer) {
                $hover.find('.entity-search-messages,.entity-search-controls').remove();
            }

            $hover.find('input.entity-search-question').closest('form').on('submit', function(evt){
                const $input = $(evt.currentTarget).find('input.entity-search-question');
                const text = $input.val().trim();

                const $answerEl = $hover.find('.entity-search-messages');

                if (text && $answerEl.length) {
                    $input.val('');
                    addMessage('entity-search-user', text);

                    const $loading = addHtmlMessage('entity-search-loading', loadingHtml);

                    const questionText = /^(what|who|how|when|where|why)/i.exec(text) ? text : 'what is the ' + text + ' of ' + $input.data('context')

                    const answeredQuestionsCollection = new AnsweredQuestionsCollection();
                    answeredQuestionsCollection.url = 'api/public/answer/entity-search-ask';

                    answeredQuestionsCollection.fetch({
                        data: {
                            text: questionText,
                            maxResults: 1,
                            context: context,
                        },
                        reset: true,
                        success: _.bind(function() {
                            const answer = answeredQuestionsCollection.map(function(model){
                                const text = model.get('answer');
                                const source = model.get('source');
                                const systemName = model.get('systemName');

                                const title = _.compact([systemName, source]).join(': ');
                                const link = /^https?:/i.test(source) ? '<a class="entity-search-cite" target="_blank" href="'+_.escape(source)+'">'+_.escape(source)+'</a>' : '';

                                const formattedText = text && isFinite(text) && (text >= 10000 || text < 0)? prettyPrintNumberHelper(text, { hash: {}}) : text;

                                return '<span title="'+_.escape(title)+'">' + _.escape(formattedText) + ' ' + link + '</span>';
                            }).join('');
                            addHtmlMessage('entity-search-server', answer || _.escape(i18n['entitySearch.template.question.answerMissing']), $loading);
                        }, this),
                        error: _.bind(function() {
                            addMessage('entity-search-server', i18n['entitySearch.template.question.answerError'], $loading);
                        }, this)
                    }, this);
                }

                function addMessage(cssClass, text, $targetEl) {
                    const $el = $('<div class="'+cssClass+'">').text('\n' + text + '\n');
                    insertMessage($el, $targetEl);
                    return $el;
                }

                function addHtmlMessage(cssClass, html, $targetEl) {
                    const $el = $('<div class="'+cssClass+'">').html('\n' + html + '\n');
                    insertMessage($el, $targetEl)
                    return $el;
                }

                function insertMessage($el, $targetEl) {
                    if ($targetEl && $targetEl.length) {
                        $targetEl.replaceWith($el);
                    }
                    else {
                        $el.appendTo($answerEl);
                    }
                    scrollDown($el);
                    reposition();
                    // On IE, we keep losing the focus after the user presses 'Enter', so we reclaim the focus.
                    $input.focus();
                }

                function scrollDown($el) {
                    const dom = $answerEl[0];
                    if (dom.scrollHeight) {
                        dom.scrollTop = $el && $el.length ? ($el[0].offsetTop - $el[0].parentNode.offsetTop) || dom.scrollHeight: dom.scrollHeight;
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

            // We're doing selection stuff on the selection popup, we don't want to trigger load indicator changes
            //  otherwise the selection popup will disappear.
            const isInSelection = $selectEnd.closest('.selection-entity').length;

            if (!isInSelection) {
                clearAllIndicators();
            }

            if (text && text.length >= 2) {
                const $summary = $selectEnd.closest(selector);

                if ($summary.length && $(sel.anchorNode).closest(selector).is($summary)) {
                    // We're in a summary, try fetching stuff
                    loadModel(text, range.getBoundingClientRect());
                }
            }
        }

        const debounced = _.debounce(onSelectionChange, debounceMillis);

        function rearrangePopups(evt) {

        }

        $(document)
            .on('selectionchange', debounced)
            .on('click', '.selection-entity-close', clearClickedIndicator)
            .on('click', '.selection-entity', rearrangePopups)

        globalKeyListener.on('escape', clearAllIndicators);

        this.stopListening = function(){
            $(document)
                .off('selectionchange', debounced)
                .off('click', '.selection-entity-close', clearClickedIndicator)
                .off('click', '.selection-entity', rearrangePopups);
            globalKeyListener.off('escape', clearAllIndicators);
            clearAllIndicators();
        }

        this.setElement = function(dom) {
            element = dom;
        }
    }

    return SelectionEntitySearch;
});
