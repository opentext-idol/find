define([
    'find/app/page/search/filters/parametric/parametric-select-modal',
    'backbone'
], function(ParametricSelectModal, Backbone) {

    describe('Parametric Select Modal', function() {
        beforeEach(function() {
            this.selectedParametricValues = new Backbone.Collection([
                {field: 'AUTHOR', value: 'Matthew'},
                {field: 'AUTN_DATE', range: [1435281816, 1444974627]}
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
                    {id: '/DOCUMENT/AUTHOR', displayName: 'Author'},
                    {id: '/DOCUMENT/PLACE'},
                    {id: '/DOCUMENT/CATEGORY'}
                ])
            })
        });

        it('initializes correctly', function() {
            const $tabs = this.modal.$('.fields-list a');
            expect($tabs).toHaveLength(3);
            expect($tabs.eq(0)).toContainText('Author');
            expect($tabs.eq(1)).toContainText('Category');
            expect($tabs.eq(2)).toContainText('Place');
        });
    });

});
