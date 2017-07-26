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

    let contextId;

    return function(target) {
        const $button = $(buttonTemplate);
        const $dialog = $(dialogTemplate);
        const $messages = $dialog.find('.conversation-dialog-messages');

        $dialog.appendTo(target)
        $button.appendTo(target)

        function sendQuery(query) {
            $.post(url, {
                query: query,
                contextId: contextId
            }).done(function (resp) {
                const response = resp.response;
                contextId = resp.contextId;

                $messages.append('<div class="conversation-dialog-server">' + _.escape(response) + '</div>');
            })
        }

        $button.on('click', function(){
            $dialog.toggleClass('conversation-dialog-dismissed');

            if (!$dialog.hasClass('conversation-dialog-dismissed') && !$messages.find('div').length) {
                sendQuery('');
            }
        })

        $dialog.find('.conversation-dialog-title').on('click', function(){
            $dialog.addClass('conversation-dialog-dismissed')
        })

        $dialog.find('form').on('submit', function(){
            const query = this.query.value;
            $messages.append('<div class="conversation-dialog-user">'+_.escape(query)+'</div>');
            sendQuery(query);
            this.query.value = '';
            this.query.focus();
            return false;
        })
    };
});
