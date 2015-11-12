/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'find/app/page/related-concepts/related-concepts-view',
    'mock/model/indexes-collection',
    'jasmine-jquery'
], function(Backbone, RelatedConceptsView, IndexesCollection) {

    describe('Related Concepts view', function() {
        beforeEach(function() {
            this.indexesCollection = new IndexesCollection();
            this.queryModel = new Backbone.Model();
            this.entityCollection = new Backbone.Collection();

            this.relatedConceptsView = new RelatedConceptsView({
                entityCollection: this.entityCollection,
                indexesCollection: this.indexesCollection,
                queryModel: this.queryModel
            });

            this.relatedConceptsView.render();
        });

        afterEach(function() {
            IndexesCollection.reset();
        });

        it('should only render one loading spinner when multiple request events are triggered', function() {
            this.entityCollection.trigger('request');
            this.entityCollection.trigger('request');

            expect(this.relatedConceptsView.$('.loading-spinner')).toHaveLength(1);
        });
    })

});
