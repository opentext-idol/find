define([
    'find/app/page/search/filters/parametric/parametric-paginator',
    'find/app/page/search/filters/parametric/parametric-select-modal-list-view',
    'find/app/configuration',
    'backbone'
], function(ParametricPaginator, SelectModalListView, configuration, Backbone) {

    const testValueSelected = function (view, value, selected) {
        expect(view.$('[data-value="' + value + '"] input').prop('checked')).toBe(selected);
    }

    const testAllUnchecked = function () {
        it('shows unchecked "all" checkbox', function () {
            const input = this.view.$('.parametric-value-all-label input');
            expect(input.prop('checked')).toBe(false);
            expect(input.prop('indeterminate')).toBe(false);
        });
    }

    const testAllChecked = function () {
        it('shows checked "all" checkbox', function () {
            const input = this.view.$('.parametric-value-all-label input');
            expect(input.prop('checked')).toBe(true);
            expect(input.prop('indeterminate')).toBe(false);
        });
    }

    const testAllIndeterminate = function () {
        it('shows indeterminate "all" checkbox', function () {
            const input = this.view.$('.parametric-value-all-label input');
            expect(input.prop('indeterminate')).toBe(true);
            expect(input.prop('checked')).toBe(false);
        });
    }

    describe('Parametric Select Modal list view constructed with a loading Paginator', function() {
        beforeEach(function() {
            configuration.and.returnValue({});

            this.paginator = new ParametricPaginator({
                selectedValues: new Backbone.Collection()
            });

            this.view = new SelectModalListView({paginator: this.paginator});
            this.view.render();
        });

        it('hides the "all" checkbox', function () {
            expect(this.view.$('.parametric-value-all-label')).toHaveClass('hide');
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
                this.paginator.valuesCollection.add([
                    { value: 'MONKEY', count: 5, selected: false },
                    { value: 'CAT', count: 3, selected: false }
                ]);
                this.paginator.stateModel.set('loading', false);
            });

            describe('with no items selected', function () {
                testAllUnchecked();

                describe('then a value is clicked', function () {

                    beforeEach(function () {
                        this.view.$('[data-value="MONKEY"]').click();
                    });

                    testAllIndeterminate();
                });

                describe('then "all" is clicked', function () {

                    beforeEach(function () {
                        this.view.$('.parametric-value-all-label').click();
                    });

                    it('selects all values', function () {
                        expect(this.view.$('.values-list li')).toHaveLength(2);
                        testValueSelected(this.view, 'MONKEY', true);
                        testValueSelected(this.view, 'CAT', true);
                    });

                    testAllChecked();
                });

            });

            describe('with all items selected', function () {

                beforeEach(function () {
                    this.paginator.toggleSelection('MONKEY');
                    this.paginator.toggleSelection('CAT');
                });

                testAllChecked();

                describe('then a value is clicked', function () {

                    beforeEach(function () {
                        this.view.$('[data-value="MONKEY"]').click();
                    });

                    testAllIndeterminate();
                });

                describe('then "all" is clicked', function () {

                    beforeEach(function () {
                        this.view.$('.parametric-value-all-label').click();
                    });

                    it('deselects all values', function () {
                        expect(this.view.$('.values-list li')).toHaveLength(2);
                        testValueSelected(this.view, 'MONKEY', false);
                        testValueSelected(this.view, 'CAT', false);
                    });

                    testAllUnchecked();
                });

            });

            describe('with some items selected', function () {

                beforeEach(function () {
                    this.paginator.toggleSelection('MONKEY');
                });

                it('hides the loading indicator', function() {
                    expect(this.view.$('.loading-spinner')).toHaveClass('hide');
                });

                it('renders the values', function() {
                    expect(this.view.$('.values-list li')).toHaveLength(2);
                    testValueSelected(this.view, 'MONKEY', true);
                    testValueSelected(this.view, 'CAT', false);
                });

                testAllIndeterminate();

                describe('then the unselected value is clicked', function () {

                    beforeEach(function () {
                        this.view.$('[data-value="CAT"]').click();
                    });

                    testAllChecked();
                });

                describe('then the selected value is clicked', function () {

                    beforeEach(function () {
                        this.view.$('[data-value="MONKEY"]').click();
                    });

                    testAllUnchecked();
                });

                describe('then "all" is clicked', function () {

                    beforeEach(function () {
                        this.view.$('.parametric-value-all-label').click();
                    });

                    it('selects all values', function () {
                        expect(this.view.$('.values-list li')).toHaveLength(2);
                        testValueSelected(this.view, 'MONKEY', true);
                        testValueSelected(this.view, 'CAT', true);
                    });

                    testAllChecked();
                });

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

            it('hides the "all" checkbox', function () {
                expect(this.view.$('.parametric-value-all-label')).toHaveClass('hide');
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

            it('hides the "all" checkbox', function () {
                expect(this.view.$('.parametric-value-all-label')).toHaveClass('hide');
            });

        });
    });

});
