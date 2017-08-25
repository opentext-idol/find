/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    'backbone',
    'find/app/util/url-manipulator',
    'find/idol/app/model/answer-bank/idol-answered-questions-collection',
    'js-whatever/js/list-view',
    'text!find/templates/app/page/search/results/questions-container.html',
    'i18n!find/nls/bundle'
], function(_, $, Backbone, urlManipulator, AnsweredQuestionsCollection, ListView, questionsTemplate, i18n) {
    'use strict';

    const MAX_SIZE = 1;
    const CROPPED_SUMMARY_CHAR_LENGTH = 300;
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

    function allowLinks(value) {
        if (!value) {
            return value;
        }

        let escaped = '';

        const regex = /<a\s+href=(['"]?[^'"<>]+['"]?)\s*(?:target="_blank"\s*)?>([^<>]*)<\/a>/g;

        let lastIndex = 0, match;
        while (match = regex.exec(value)) {
            escaped += autoLink(value.slice(lastIndex, match.index));

            escaped += '<a href=' + hackUrl(match[1]) + ' target="_blank">' + match[2] + '</a>';

            lastIndex = match.index + match[0].length
        }

        escaped += autoLink(value.slice(lastIndex));

        return escaped;
    }

    return Backbone.View.extend({
        events: {
            'click .read-more': function(e) {
                const $target = $(e.currentTarget);
                const $summary = $target.siblings('.summary-text');
                $summary.toggleClass('result-summary');

                const isResultSummary = $summary.hasClass('result-summary');
                $target.text(isResultSummary ? i18n['app.more'] : i18n['app.less']);
                $summary.children('.extended-answer')
                    .toggleClass('hide', isResultSummary);
                $target.siblings('.summary-text')
                    .children('.ellipsis')
                    .toggleClass('hide', !isResultSummary);
            }
        },

        initialize: function(options) {
            this.answeredQuestionsCollection = new AnsweredQuestionsCollection();
            this.queryModel = options.queryModel;
            this.loadingTracker = options.loadingTracker;
            this.clearLoadingSpinner = options.clearLoadingSpinner;

            this.template = _.template(questionsTemplate);
        },

        render: function() {
            this.$('[data-toggle="tooltip"]').tooltip('destroy');

            const html = this.answeredQuestionsCollection.map(function(answeredQuestion) {
                const croppedAnswer = answeredQuestion.get('answer').slice(0, CROPPED_SUMMARY_CHAR_LENGTH);
                const extendedAnswer = answeredQuestion.get('answer').slice(CROPPED_SUMMARY_CHAR_LENGTH);

                return this.template({
                    i18n: i18n,
                    model: answeredQuestion,
                    croppedAnswer: croppedAnswer,
                    extendedAnswer: extendedAnswer,
                    showMoreButton: answeredQuestion.get('answer').length > CROPPED_SUMMARY_CHAR_LENGTH,
                    allowLinks: allowLinks,
                    hackUrl: hackUrl
                });
            }, this).join('');

            this.$el.html(html);
            this.$('[data-toggle="tooltip"]').tooltip({
                container: 'body',
                placement: 'top'
            });

            return this;
        },

        fetchData: function() {
            this.loadingTracker.questionsFinished = false;
            this.$el.empty();

            this.answeredQuestionsCollection.fetch({
                data: {
                    text: this.queryModel.get('queryText'),
                    maxResults: MAX_SIZE
                },
                reset: true,
                success: _.bind(function() {
                    this.render();
                    this.loadingTracker.questionsFinished = true;
                    this.clearLoadingSpinner();
                }, this),
                error: _.bind(function() {
                    this.loadingTracker.questionsFinished = true;
                    this.clearLoadingSpinner();
                }, this)
            }, this);
        },

        remove: function() {
            this.$('[data-toggle="tooltip"]').tooltip('destroy');
            Backbone.View.prototype.remove.call(this);
        }
    });
});
