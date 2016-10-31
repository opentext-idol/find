/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'moment',
    'find/idol/app/page/search/snapshots/snapshot-data-view'
], function(Backbone, _, moment, SnapshotFilterView) {

    var CREATED_DATE = 1454668000000;

    describe('Snapshot data view', function() {
        beforeEach(function() {
            this.savedSearchModel = new Backbone.Model({
                title: 'Quantum Cat',
                relatedConcepts: ['Copenhagen', 'Schrödinger'],
                minDate: null,
                maxDate: null,
                dateCreated: moment(CREATED_DATE),
                resultCount: 0,
                indexes: [
                    {name: 'Wikipedia', domain: null},
                    {name: 'Arxiv', domain: null}
                ],
                parametricValues: [
                    {field: 'CATEGORY', value: 'science'},
                    {field: 'CATEGORY', value: 'history'},
                    {field: 'FILE_TYPE', value: 'html'}
                ]
            });

            this.view = new SnapshotFilterView({
                savedSearchModel: this.savedSearchModel
            });

            this.view.render();
        });

        it('displays the date created', function() {
            expect(this.view.$el).toContainText('2016/02/05');
        });
    });

});
