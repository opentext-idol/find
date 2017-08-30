/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    'underscore',
    'find/app/util/chart',
    'find/app/util/url-manipulator',
    'find/app/page/search/template-helpers/stars-helper',
    'text!find/templates/app/util/conversation-button.html',
    'text!find/templates/app/util/conversation-dialog.html'
], function($, _, chart, urlManipulator, starsHelper, buttonTemplate, dialogTemplate) {

    const prefix = 'api/public/conversation';
    const chatUrl = prefix + '/chat';
    const saveUrl = prefix + '/save';
    const helpUrl = prefix + '/help';
    const starRatingClass = 'conversation-star-rating';
    const starRatingSelector = '.' + starRatingClass;

    // Test for a key phrase to prompt the user if they want a phone call.
    const unrecognized = /I did not understand that|I didn't understand what you meant|I can guide you through the topics I know about/i;
    let unrecognizedCount = 0;
    const CALL_THRESHOLD = 2;

    // Test for a terminal phrase to determine if the conversation has ended.
    const terminalPhrase = /Thank you for using HPE Virtual Assistant. I look forward to talking again soon!/i;

    // How long to wait before indexing the document, five minutes.
    const idleIndexDelay = 5 * 60e3;

    let contextId, lastQuery, needsIndex, idleIndexTimeout, lastRating, helpRequired;

    const hackUrl = urlManipulator.hackUrl;

    function autoLink(value) {
        // Automatically convert plain HTTP/HTTPS links to <a> tags.
        // We use lookahead to ignore the trailing 'dot' if present, since that's placed as punctuation in an
        //  answer server response.
        const regex = /(https?:\/\/\S+(?=\.?(\s|$)))/gi;

        let lastIndex = 0, match, escaped = '';
        while (match = regex.exec(value)) {
            escaped += _.escape(value.slice(lastIndex, match.index));

            const url = hackUrl(match[1]);
            escaped += '<a href="' + _.escape(url) + '" target="_blank">' + _.escape(url) + '</a>'

            lastIndex = match.index + match[0].length
        }

        escaped += _.escape(value.slice(lastIndex));

        return escaped;
    }

    function escapeNonImages(value) {
        if (!value) {
            return value;
        }

        let escaped = '';

        const regex = /(<(img|chart|suggest|cite|help) )([^<>]+>)|(<(table|a|sup|span)[^<>]*>[\s\S]*?<\/\5>)/g;

        let lastIndex = 0, match;
        while (match = regex.exec(value)) {
            escaped += autoLink(value.slice(lastIndex, match.index));

            if (match[4]) {
                // <table> and <a> are placed verbatim, without any escaping.
                let toAdd = match[4];

                if (match[5] === 'a') {
                    // we want to add a target=blank to the href if it doesn't already have an
                    if (!/\btarget=/.exec(toAdd)) {
                        toAdd = toAdd.slice(0, 2) + ' target="_blank" ' + toAdd.slice(2);
                    }
                }
                escaped += hackUrl(toAdd);
            }
            else if (match[2] === 'suggest') {
                const $tmp = $(match[0]);
                const opts = ($tmp.attr('options') || '').trim();
                if (opts) {
                    escaped += '<br>' + _.map(opts.split('|'), function(str){
                        return '<span class="btn btn-primary btn-sm question-answer-suggestion">'+ _.escape(str)+'</span>';
                    }).join(' ')
                }
                else {
                    const query = ($tmp.attr('query') || '').trim();
                    if (query) {
                        const label = ($tmp.attr('label') || '').trim() || query;
                        escaped += '<span class="btn btn-primary btn-sm question-answer-suggestion" data-query="'+_.escape(query)+'">'+ _.escape(label)+'</span>'
                    }
                }
            }
            else if (match[2] === 'help') {
                const $tmp = $(match[0]);
                helpRequired =  $tmp.attr('topic') || true;
            }
            else if (match[2] !== 'cite') {
                escaped += match[1] + ' class="safe-image" ' + match[3];
            }

            lastIndex = match.index + match[0].length
        }

        escaped += autoLink(value.slice(lastIndex));

        return escaped.replace(/\n/g, '<br>').trim()
    }

    return function(target) {
        const $button = $(buttonTemplate);
        const $dialog = $(dialogTemplate);
        const $messages = $dialog.find('.conversation-dialog-messages');

        $dialog.appendTo(target)
        $button.appendTo(target)

        function sendQuery(query) {
            if(contextId) {
                lastQuery = query;
            }

            $.post(chatUrl, {
                query: query,
                contextId: contextId
            }).done(function (resp) {
                const response = resp.response;
                contextId = resp.contextId;

                helpRequired = false;
                const parsedResponse = escapeNonImages(response);
                const $newEl = $('<div class="conversation-dialog-server">' + parsedResponse + '</div>');

                if (parsedResponse && $.trim(parsedResponse)) {
                    $newEl.appendTo($messages);
                    chart($newEl);
                }

                if (helpRequired) {
                    askHelp(helpRequired === true ? undefined : helpRequired, $newEl)
                }

                if (unrecognized.exec(response)) {
                    unrecognizedCount++;
                    if (unrecognizedCount >= CALL_THRESHOLD) {
                        $newEl.append('<br><span class="btn btn-secondary btn-sm question-answer-help">Need to talk?</span>');
                    }
                }
                else {
                    unrecognizedCount = 0;
                }

                if (terminalPhrase.exec(response)) {
                    saveConversation(undefined);
                    $newEl.find('.question-answer-suggestion').remove();
                    $newEl.append(' <br><div class="btn btn-secondary btn-sm question-answer-save">Rate this conversation: ' + starsHelper(0, '', '', starRatingClass) + '</div>');
                }

                scrollDown();
            })
        }

        function scrollDown(){
            const dom = $messages[0];
            if (dom.scrollHeight) {
                dom.scrollTop = dom.scrollHeight;
            }
        }

        $button.on('click', function(){
            $dialog.toggleClass('conversation-dialog-dismissed');

            if (!$dialog.hasClass('conversation-dialog-dismissed') && !$messages.find('div').length) {
                startNewConversation();
            }
        })

        function startNewConversation() {
            contextId = lastQuery = lastRating = helpRequired = undefined;
            unrecognizedCount = 0;
            needsIndex = false;

            sendQuery('');
            $input.focus();
        }

        $dialog.find('.conversation-hide').on('click', function(){
            $dialog.addClass('conversation-dialog-dismissed')
        })

        $dialog.find('.conversation-reset').on('click', function(){
            saveConversationIfRequired();

            $messages.find('div').remove();
            startNewConversation();
        })

        const $form = $dialog.find('form');
        const $input = $($form[0].query).on('keyup', function(evt){
            if (evt.keyCode === 38 && lastQuery) {
                this.value = lastQuery;
            }
        })

        $form.on('submit', function(){
            const query = this.query.value;

            if (query) {
                $messages.append('<div class="conversation-dialog-user">'+_.escape(query)+'</div>');
                scrollDown();
                sendQuery(query);
                this.query.value = '';
                this.query.focus();
                needsIndex = true;

                if (idleIndexTimeout) {
                    clearTimeout(idleIndexTimeout);
                }

                idleIndexTimeout = setTimeout(saveConversationIfRequired, idleIndexDelay);
            }

            return false;
        })

        $dialog.on('click', '.question-answer-suggestion', function(evt){
            const $el = $(evt.currentTarget);
            $form[0].query.value = $el.data('query') || $el.text();
            $form.submit();
        })

        function askHelp(topic, $before) {
            $.post(helpUrl, {
                contextId: contextId,
                topic: topic
            }).done(function (experts) {
                const $newEl = $('<div class="conversation-dialog-server">' + experts.map(function (expert) {
                    return '<a class="btn btn-secondary btn-sm" href="sip:' + _.escape(expert.email) + '">Chat - ' + _.escape(expert.name) + ' (' + _.escape(expert.area) + ')</a>';
                }).join(' ') + '</div>');

                if ($before) {
                    $newEl.insertBefore($before);
                }
                else {
                    $newEl.appendTo($messages);
                }

                scrollDown();
            })
        }

        $dialog.on('click', '.question-answer-help', function(evt){
            askHelp(undefined);
        })

        function saveConversationIfRequired() {
            if (needsIndex) {
                saveConversation(undefined);
            }
        }

        function saveConversation(rating) {
            needsIndex = false;

            if (rating) {
                lastRating = rating;
            }

            return $.post(saveUrl, {
                contextId: contextId,
                rating: lastRating
            })
        }

        $dialog.on('click', '.question-answer-save ' + starRatingSelector, function(evt){
            const $el = $(evt.currentTarget);
            const rating = $el.data('rating');
            saveConversation(rating);
            $el.siblings(starRatingSelector).remove();
            $el.replaceWith(starsHelper(rating, '', '', starRatingClass) + '');
        })

        $(window).on('beforeunload', saveConversationIfRequired);
    };
});
