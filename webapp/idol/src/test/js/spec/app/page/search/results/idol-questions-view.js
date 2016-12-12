define([
    'backbone',
    'underscore',
    'jquery',
    'i18n!find/nls/bundle',
    'find/idol/app/page/search/results/idol-questions-view',
    'find/idol/app/model/answer-bank/idol-answered-questions-collection'
], function(Backbone, _, $, i18n, IdolQuestionsView, IdolAnsweredQuestionsCollection) {
    describe('Idol Questions View', function() {
        beforeEach(function() {
            this.view = new IdolQuestionsView({
                queryModel: new Backbone.Model({
                    queryText: 'This is some query text'
                }),
                loadingModel: {
                    questionsFinished: true
                },
                clearLoadingSpinner: _.noop
            });
            this.collection = IdolAnsweredQuestionsCollection.instances[0];
        });

        afterEach(function() {
            IdolAnsweredQuestionsCollection.reset();
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
                expect(this.collection.fetch.calls.mostRecent().args[0].data).toEqual({
                    maxResults: 1,
                    text: 'This is some query text'
                })
            });

            describe('and the fetch returns successfully', function(){
                beforeEach(function() {
                    this.collection.add(new Backbone.Model({
                        question: 'Is this the question?',
                        answer: 'Yes it is'
                    }));
                    this.collection.fetch.calls.mostRecent().args[0].success();
                });

                it('should have displayed the answered question', function() {
                    expect(this.view.$('.result-header').text().trim()).toEqual(i18n['search.answeredQuestion.question'] + 'Is this the question?');
                    expect(this.view.$('.summary-text').text().trim()).toEqual(i18n['search.answeredQuestion.answer'] + 'Yes it is');
                });
            });
        });
    });
});