/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'jquery',
    // 'find/app/model/query-model',QueryModel, DocumentsCollection,
    // 'find/app/model/documents-collection',
    'find/app/page/search/spellcheck-view'
], function(Backbone, $, SpellCheckView) {

    describe('Spellcheck View', function() {
        beforeEach(function() {
            // this.documentsCollection = new Backbone.Collection({autoCorrection: null});
            this.documentsCollection = new Backbone.Collection();
            this.documentsCollection.autoCorrection = null;

            this.documentsCollection.getAutoCorrection = function() {
                return this.autoCorrection;
            };

            this.queryModel = new Backbone.Model();
            this.spellCheckView = new SpellCheckView({
                queryModel: this.queryModel,
                documentsCollection: this.documentsCollection
            });
            this.spellCheckView.render();
        });

        it('is only visible if the documentsCollection.autoCorrection is true', function() {
            expect(this.spellCheckView.$el).toHaveClass('hidden');
        });
    });
});