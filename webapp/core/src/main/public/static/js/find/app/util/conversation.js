/*
 * Copyright 2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    'underscore',
    'i18n!find/nls/bundle',
    'find/app/util/chart',
    'find/app/util/url-manipulator',
    'text!find/templates/app/util/conversation-button.html',
    'text!find/templates/app/util/conversation-dialog.html'
], function($, _, i18n, chart, urlManipulator, buttonTemplate, dialogTemplate) {

    const chatUrl = 'api/public/answer/converse';

    let contextId, lastQuery;

    function autoLink(value) {
        // Automatically convert plain HTTP/HTTPS links to <a> tags.
        // We use lookahead to ignore the trailing 'dot' if present, since that's placed as punctuation in an
        //  answer server response.
        const regex = /(https?:\/\/\S+(?=\.?(\s|$)))/gi;

        let lastIndex = 0, match, escaped = '';
        while (match = regex.exec(value)) {
            escaped += _.escape(value.slice(lastIndex, match.index));

            const url = match[1];
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
                escaped += toAdd;
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
        const $dialog = $(_.template(dialogTemplate)({i18n: i18n}));
        const $messages = $dialog.find('.conversation-dialog-messages');

        $dialog.appendTo(target)
        $button.appendTo(target)

        function sendQuery(query) {
            if(contextId) {
                lastQuery = query;
            }

            $.post(chatUrl, {
                text: query,
                sessionId: contextId
            }).done(function (resp) {
                const prompts = resp.prompts;

                const response = _.map(prompts, function(prompt){
                    let text = prompt.prompt;

                    if (prompt.validChoices) {
                        const validChoices = prompt.validChoices.validChoice;

                        if (validChoices && validChoices.length) {
                            text += '\n' + validChoices.map(function(choice){
                                return '<suggest query="' + _.escape(choice) + '">'
                            }).join(' ')
                        }
                    }

                    if (prompt.suggestions) {
                        const suggestions = prompt.suggestions.suggestion;

                        if (suggestions && suggestions.length) {
                            text += '\n' + i18n['conversation.suggestions'];
                            text += suggestions.map(function(choice){
                                return '<suggest query="' + _.escape(choice) + '">'
                            }).join(' ')
                        }
                    }

                    return text;
                }).join('\n');
                contextId = resp.sessionId;

                const parsedResponse = escapeNonImages(response);
                const $newEl = $('<div class="conversation-dialog-server">' + parsedResponse + ' </div>');

                if (parsedResponse && $.trim(parsedResponse)) {
                    $newEl.appendTo($messages);
                    chart($newEl);
                }

                scrollDown();
            }).fail(function(xhr){
                let error = xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message
                    : i18n['conversation.error'];

                $('<div class="conversation-dialog-server conversation-dialog-error">').text(error).appendTo($messages)
                    .on('click', function(e){
                        $(e.currentTarget).remove();
                    });
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
            contextId = lastQuery = undefined;
            sendQuery('');
            $input.focus();
        }

        $dialog.find('.conversation-hide').on('click', function(){
            $dialog.addClass('conversation-dialog-dismissed')
        })

        $dialog.find('.conversation-reset').on('click', function(){
            endConversation();

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
                $messages.append('<div class="conversation-dialog-user">'+_.escape(query)+' </div>');
                scrollDown();
                sendQuery(query);
                this.query.value = '';
                this.query.focus();
            }

            return false;
        })

        $dialog.on('click', '.question-answer-suggestion', function(evt){
            const $el = $(evt.currentTarget);
            $form[0].query.value = $el.data('query') || $el.text();
            $form.submit();
        });

        function endConversation() {
            if (contextId) {
                $.post(chatUrl + '/' + contextId + '/end');
                contextId = null;
            }
        }

        $(window).on('beforeunload', endConversation);
    };
});
