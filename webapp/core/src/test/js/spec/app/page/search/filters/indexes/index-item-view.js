/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

define([
    'backbone',
    'find/app/page/search/filters/indexes/index-item-view'
], function(Backbone, IndexItemView) {
    'use strict';

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
