/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'moment',
    'find/app/page/search/snapshots/snapshot-filter-view'
], function(Backbone, _, moment, SnapshotFilterView) {

    var MAX_DATE = 1454668000000;
    var QUERY_TEXT = 'cat';
    var RELATED_CONCEPTS = ['Copenhagen', 'Schrödinger'];
    var INDEXES = ['Wikipedia', 'Arxiv'];

    var PARAMETRIC_VALUES = [
        {field: 'CATEGORY', value: 'science'},
        {field: 'CATEGORY', value: 'history'},
        {field: 'FILE_TYPE', value: 'html'}
    ];

    describe('Snapshot filter view', function() {
        beforeEach(function() {
            this.savedSearchModel = new Backbone.Model({
                title: 'Quantum Cat',
                queryText: QUERY_TEXT,
                relatedConcepts: RELATED_CONCEPTS,
                indexes: _.map(INDEXES, function(name) {return {domain: null, name: name}}),
                parametricValues: PARAMETRIC_VALUES,
                minDate: null,
                maxDate: moment(MAX_DATE)
            });

            this.view = new SnapshotFilterView({
                savedSearchModel: this.savedSearchModel
            });

            this.view.render();
        });

        it('displays the query text', function() {
            expect(this.view.$el).toContainText(QUERY_TEXT);
        });

        it('displays the related concepts', function() {
            _.each(RELATED_CONCEPTS, function(concept) {
                expect(this.view.$el).toContainText(concept);
            }, this);
        });

        it('displays the indexes', function() {
            _.each(INDEXES, function(index) {
                expect(this.view.$el).toContainText(index);
            },this);
        });

        it('displays the parametric field names and values', function() {
            _.each(PARAMETRIC_VALUES, function(parametricRestriction) {
                expect(this.view.$el).toContainText(parametricRestriction.field);
                expect(this.view.$el).toContainText(parametricRestriction.value);
            }, this);
        });

        it('formats and displays the min date', function() {
            expect(this.view.$el).toContainText('2016/02/05 10:26');
        });

        describe('when the saved search model query text changes', function() {
            var NEW_QUERY_TEXT = 'foo';

            beforeEach(function() {
                this.savedSearchModel.set('queryText', NEW_QUERY_TEXT);
            });

            it('removes the old query text', function() {
                expect(this.view.$el).not.toContainText(QUERY_TEXT);
            });

            it('displays the new query text', function() {
                expect(this.view.$el).toContainText(NEW_QUERY_TEXT);
            });
        });
    });

});
