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
    'find/app/page/search/filters/parametric/parametric-select-modal'
], function(Backbone, ParametricSelectModal) {
    'use strict';

    describe('Parametric Select Modal', function() {
        beforeEach(function() {
            this.selectedParametricValues = new Backbone.Collection([
                {field: 'AUTHOR', displayName: 'Author', value: 'Matthew', displayValue: 'Matthew', type: 'Parametric'},
                {field: 'AUTN_DATE', displayName: 'Autn Date', range: [1435281816, 1444974627], type: 'NumericDate'}
            ]);

            const queryModel = new Backbone.Model({
                indexes: ['WIKIPEDIA'],
                autoCorrect: false,
                queryText: 'cat',
                fieldText: null,
                minScore: 50,
                stateTokens: []
            });

            queryModel.getIsoDate = jasmine.createSpy('getIsoDate').and.returnValue(null);

            this.modal = new ParametricSelectModal({
                initialField: '/DOCUMENT/AUTHOR',
                queryModel: queryModel,
                selectedParametricValues: this.selectedParametricValues,
                indexesCollection: new Backbone.Collection([
                    {name: 'BROADCAST'},
                    {name: 'WIKIPEDIA'}
                ]),
                parametricFieldsCollection: new Backbone.Collection([
                    {id: '/DOCUMENT/AUTHOR', displayName: 'Author', type: 'Parametric'},
                    {id: '/DOCUMENT/PLACE', displayName: 'Place', type: 'Parametric'},
                    {id: '/DOCUMENT/CATEGORY', displayName: 'Category', type: 'Parametric'}
                ])
            })
        });

        afterEach(function() {
            this.modal.hide()
        });

        it('initializes correctly', function() {
            const $tabs = this.modal.$('.fields-list a');
            expect($tabs).toHaveLength(3);
            expect($tabs.eq(0)).toContainText('Author');
            expect($tabs.eq(1)).toContainText('Place');
            expect($tabs.eq(2)).toContainText('Category');
        });
    });
});
