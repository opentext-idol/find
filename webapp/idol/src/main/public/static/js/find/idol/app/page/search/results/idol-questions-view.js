define([
    'backbone',
    'find/idol/app/model/answer-bank/idol-answered-questions-collection',
    'js-whatever/js/list-view',
    'text!find/templates/app/page/search/results/questions-container.html',
    'i18n!find/nls/bundle'
], function (Backbone, AnsweredQuestionsCollection, ListView, questionsTemplate, i18n) {
    const MAX_SIZE = 1;
    const CROPPED_SUMMARY_CHAR_LENGTH = 300;

    return Backbone.View.extend({
        events: {
            'click .read-more': function (e) {
                let $target = $(e.currentTarget);
                let summary = $target.siblings('.summary-text');
                let extendedAnswer = summary.children('.extendedAnswer');

                summary.toggleClass('result-summary');
                if (summary.hasClass('result-summary')) {
                    $target.text(i18n['app.more']);
                    extendedAnswer.addClass('hide');
                    $target.siblings('.summary-text').children('.ellipsis').removeClass('hide');
                } else {
                    $target.text(i18n['app.less']);
                    extendedAnswer.removeClass('hide');
                    $target.siblings('.summary-text').children('.ellipsis').addClass('hide');
                }
            }
        },

        initialize: function (options) {
            this.answeredQuestionsCollection = new AnsweredQuestionsCollection();
            this.queryModel = options.queryModel;
            this.loadingModel = options.loadingModel;
            this.clearLoadingSpinner = options.clearLoadingSpinner;

            this.template = _.template(questionsTemplate);
        },

        render: function () {
            const html = this.answeredQuestionsCollection.map(function (answeredQuestion) {
                let croppedAnswer = answeredQuestion.get('answer').slice(0, CROPPED_SUMMARY_CHAR_LENGTH);
                let extendedAnswer = answeredQuestion.get('answer').slice(CROPPED_SUMMARY_CHAR_LENGTH);

                return this.template({
                    i18n: i18n,
                    model: answeredQuestion,
                    croppedAnswer: croppedAnswer,
                    extendedAnswer: extendedAnswer,
                    showMoreButton: answeredQuestion.get('answer').length > CROPPED_SUMMARY_CHAR_LENGTH
                });
            }, this).join('');

            this.$el.html(html);
            return this;
        },

        fetchData: function () {
            this.loadingModel.questionsFinished = false;

            let questionsRequestData = {
                text: this.queryModel.get('queryText'),
                maxResults: MAX_SIZE
            };

            this.answeredQuestionsCollection.fetch({
                data: questionsRequestData,
                reset: true,
                success: _.bind(function() {
                    this.render();
                    this.loadingModel.questionsFinished = true;
                    this.clearLoadingSpinner();
                }, this),
                error: _.bind(function() {
                    this.loadingModel.questionsFinished = true;
                    this.clearLoadingSpinner();
                })
            }, this);
        }
    });
});
