define([
    'find/app/page/search/filters/parametric/parametric-select-modal-list-view',
    'find/app/configuration',
    'backbone'
], function(SelectModalListView, configuration, Backbone) {

    describe('Parametric Select Modal list view constructed with a loading Paginator', function() {
        beforeEach(function() {
            configuration.and.returnValue({});

            this.paginator = {
                fetchNext: jasmine.createSpy('fetchNext'),
                toggleSelection: jasmine.createSpy('toggleSelection'),
                valuesCollection: new Backbone.Collection(),
                stateModel: new Backbone.Model({
                    empty: false,
                    loading: true,
                    error: null
                })
            };

            this.view = new SelectModalListView({paginator: this.paginator});
            this.view.render();
        });

        it('shows the loading indicator', function() {
            expect(this.view.$('.loading-spinner')).not.toHaveClass('hide');
        });

        it('hides the error message', function() {
            expect(this.view.$('.parametric-select-error')).toHaveClass('hide');
        });

        it('hides the empty message', function() {
            expect(this.view.$('.parametric-select-empty')).toHaveClass('hide');
        });

        describe('when the Paginator loads the first page', function() {
            beforeEach(function() {
                this.paginator.stateModel.set('loading', false);

                this.paginator.valuesCollection.add([
                    {value: 'MONKEY', count: 5, selected: false},
                    {value: 'CAT', count: 3, selected: true}
                ]);
            });

            it('hides the loading indicator', function() {
                expect(this.view.$('.loading-spinner')).toHaveClass('hide');
            });

            it('renders the values', function() {
                expect(this.view.$('li')).toHaveLength(2);
            });
        });

        describe('when the Paginator is empty', function() {
            beforeEach(function() {
                this.paginator.stateModel.set({
                    loading: false,
                    empty: true
                });
            });

            it('hides the loading indicator', function() {
                expect(this.view.$('.loading-spinner')).toHaveClass('hide');
            });

            it('shows the empty message', function() {
                expect(this.view.$('.parametric-select-empty')).not.toHaveClass('hide');
            });
        });

        describe('when the Paginator has an error', function() {
            beforeEach(function() {
                this.paginator.stateModel.set('error', {
                    message: 'It went wrong',
                    uuid: '515d7be3-7003-4440-9e5a-8a5205d50a56',
                    backendErrorCode: 'UNKNOWN ERROR'
                });
            });

            it('shows the error message', function() {
                const $error = this.view.$('.parametric-select-error');
                expect($error).not.toHaveClass('hide');
                expect($error).toContainText('515d7be3-7003-4440-9e5a-8a5205d50a56');
            });
        });
    });

});
