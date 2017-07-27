/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    'underscore',
    'text!find/templates/app/util/conversation-button.html',
    'text!find/templates/app/util/conversation-dialog.html'
], function($, _, buttonTemplate, dialogTemplate) {

    const url = 'api/public/conversation/chat';

    let contextId, lastQuery;

    function escapeNonImages(value) {
        if (!value) {
            return value;
        }

        let escaped = '';

        const regex = /(<(img|chart|suggest|cite) )([^>]+>)/g;

        let lastIndex = 0, match;
        while (match = regex.exec(value)) {
            escaped += _.escape(value.slice(lastIndex, match.index));

            if (match[2] === 'suggest') {
                const $tmp = $(match[0]);
                const opts = $tmp.attr('options').trim();
                if (opts) {
                    escaped += _.map(opts.split('|'), function(str){
                        return '<span class="btn btn-primary btn-sm question-answer-suggestion">'+ _.escape(str)+'</span>';
                    }).join(' ')
                }
            }
            else if (match[2] !== 'cite') {
                escaped += match[1] + ' class="safe-image" ' + match[3];
            }

            lastIndex = match.index + match[0].length
        }

        escaped += _.escape(value.slice(lastIndex));

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

            $.post(url, {
                query: query,
                contextId: contextId
            }).done(function (resp) {
                const response = resp.response;
                contextId = resp.contextId;

                $messages.append('<div class="conversation-dialog-server">' + escapeNonImages(response) + '</div>');
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
                sendQuery('');
                $input.focus();
            }
        })

        $dialog.find('.conversation-dialog-title').on('click', function(){
            $dialog.addClass('conversation-dialog-dismissed')
        })

        const $form = $dialog.find('form');
        const $input = $($form[0].query).on('keyup', function(evt){
            if (evt.keyCode === 38) {
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
            }

            return false;
        })

        $dialog.on('click', '.question-answer-suggestion', function(evt){
            $form[0].query.value = $(evt.currentTarget).text();
            $form.submit();
        })
    };
});
