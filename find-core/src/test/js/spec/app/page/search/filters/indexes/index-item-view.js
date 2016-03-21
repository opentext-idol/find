/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/filters/indexes/index-item-view',
    'backbone'
], function(IndexItemView, Backbone) {

    describe('IndexItemView with a valid database', function() {
        beforeEach(function() {
            this.model = new Backbone.Model({
                name: 'Wikipedia',
                deleted: false
            });

            this.view = new IndexItemView({model: this.model});
            this.view.render();
        });

        it('is not disabled', function() {
            expect(this.view.$el).not.toHaveClass('disabled-index');
        });

        it('is disabled once the database has been deleted', function() {
            this.model.set('deleted', true);
            this.view.updateDeleted();
            expect(this.view.$el).toHaveClass('disabled-index');
        });
    });

});
