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
    'jquery',
    'backbone',
    'find/app/page/search/time-bar-view',
    'find/app/model/bucketed-numeric-collection'
], function($, Backbone, TimeBarView, BucketedParametricCollection) {
    'use strict';

    describe('TimeBarView', function() {
        beforeEach(function() {
            this.queryModel = new Backbone.Model();
            this.selectedParametricValues = new Backbone.Collection();
            this.parametricFieldsCollection = new Backbone.Collection([
                {id: 'autn_date', displayName: 'Autn Date', type: 'NumericDate'}
            ]);

            this.timeBarModel = new Backbone.Model({
                graphedFieldId: 'autn_date',
                graphedFieldName: 'Autn Date',
                graphedDataType: 'NumericDate'
            });

            this.view = new TimeBarView({
                queryModel: this.queryModel,
                parametricFieldsCollection: this.parametricFieldsCollection,
                timeBarModel: this.timeBarModel,
                queryState: {
                    selectedParametricValues: this.selectedParametricValues
                }
            });

            this.view.render();
        });

        afterEach(function() {
            BucketedParametricCollection.Model.reset();
        });

        it('displays the prettified field name', function() {
            expect(this.view.$('h4')).toContainText('Autn Date');
        });

        it('clears the time bar model on clicking the cross', function() {
            this.view.$('.time-bar-container-icon').click();

            expect(this.timeBarModel.get('graphedFieldName')).toBeNull();
        });
    });
});
