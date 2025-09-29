/*
 * Copyright 2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

define([
    'underscore',
    'backbone',
    'i18n!find/nls/bundle',
    'find/idol/app/page/search/results/idol-questions-view',
    'find/idol/app/model/answer-bank/idol-answered-questions-collection'
], function(_, Backbone, i18n, IdolQuestionsView, IdolAnsweredQuestionsCollection) {
    'use strict';

    const createView = function (questionText) {
        return new IdolQuestionsView({
            queryModel: new Backbone.Model({
                questionText,
                fieldText: ''
            }),
            loadingTracker: {
                questionsFinished: true
            },
            clearLoadingSpinner: _.noop
        });
    }

    describe('Idol Questions View', function() {
        afterEach(function() {
            IdolAnsweredQuestionsCollection.reset();
        });

        describe('with questionText', function () {
            beforeEach(function() {
                this.view = createView('This is some query text');
                this.collection = IdolAnsweredQuestionsCollection.instances[0];
            });

            it('should initialize a document collection', function() {
                expect(IdolAnsweredQuestionsCollection.instances.length).toBeGreaterThan(0);
                expect(this.collection.fetch).not.toHaveBeenCalled();
            });

            describe('and when prompted to fetch', function() {
                beforeEach(function() {
                    this.view.fetchData();
                });

                it('should call fetch on the answered questions collection', function() {
                    expect(this.collection.fetch).toHaveBeenCalled();
                    expect(this.collection.fetch.calls.mostRecent().args[0].data).toEqual(
                        {
                            maxResults: 1,
                            text: 'This is some query text',
                            fieldText: ''
                        }
                    )
                });

                describe('and the fetch returns successfully', function() {
                    beforeEach(function() {
                        this.collection.add(new Backbone.Model({
                            question: 'Is this the question?',
                            answer: 'Yes it is',
                            systemName: 'answerbank0'
                        }));
                        this.collection.fetch.calls.mostRecent().args[0].success();
                    });

                    it('should have displayed the answered question', function() {
                        expect(this.view.$('.result-header').text().trim())
                            .toEqual(i18n['search.answeredQuestion.question'] + 'Is this the question?');
                        expect(this.view.$('.summary-text').text().trim())
                            .toEqual(i18n['search.answeredQuestion.answer'] + 'Yes it is');
                    });

                    it('the title attribute should contain the system name', function() {
                        expect(this.view.$('.answered-question').attr('data-original-title'))
                            .toEqual(i18n['search.answeredQuestion.systemName']('answerbank0'));
                    });
                });
            });
        });

        describe('fetch with null questionText', function() {
            beforeEach(function() {
                this.view = createView(null);
                this.collection = IdolAnsweredQuestionsCollection.instances[0];
                this.view.fetchData();
            });

            it('should not call fetch on the answered questions collection', function() {
                expect(this.collection.fetch).toHaveBeenCalledTimes(0);
            });

            it('should hide the answer box', function() {
                expect(this.view.$('.results-contents').length).toEqual(0);
                expect(this.view.$('.result-header').length).toEqual(0);
                expect(this.view.$('.summary-text').length).toEqual(0);
            });
        });
    });
});
