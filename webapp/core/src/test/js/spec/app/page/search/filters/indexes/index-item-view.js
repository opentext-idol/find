/*
 * Copyright 2016-2017 Open Text.
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
    'backbone',
    'find/app/page/search/filters/indexes/index-item-view'
], function(Backbone, IndexItemView) {
    'use strict';

    describe('IndexItemView', function() {

        describe('with a valid database', function() {

            beforeEach(function() {
                this.parametricCollection = new Backbone.Collection([
                    { id: 'AUTHOR', values: [
                        { value: 'Wikipedia', count: 123 }
                    ] },
                    { id: 'AUTN_DATABASE', values: [
                        { value: 'Other', count: 456 },
                        { value: 'wikipedia', count: 789 }
                    ] }
                ]);

                this.model = new Backbone.Model({
                    name: 'Wikipedia',
                    deleted: false
                });

                this.view = new IndexItemView({
                    parametricCollection: this.parametricCollection,
                    model: this.model
                });
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

            it('shows count', function() {
                expect(this.view.$('.database-doc-count').text()).toEqual(' (789)');
            });

            describe('with no count', function () {

                beforeEach(function () {
                    this.parametricCollection.reset([
                        { id: 'AUTN_DATABASE', values: [
                            { value: 'Other', count: 456 }
                        ] }
                    ]);
                    this.view.render();
                });

                it('count is 0', function() {
                    expect(this.view.$('.database-doc-count').text()).toEqual(' (0)');
                });

            });

        });

    });

});
