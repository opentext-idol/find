define([
    'find/app/page/search/filters/parametric/parametric-select-modal-item-view',
    'backbone'
], function(SelectModalItemView, Backbone) {

    describe('Select field modal item view', function() {
        describe('with an initially unselected value', function() {
            beforeEach(function() {
                this.model = new Backbone.Model({
                    selected: false,
                    value: 'CAT',
                    displayValue: 'Cat',
                    count: 42
                });

                this.view = new SelectModalItemView({model: this.model});
                this.view.render();
            });

            it('sets a data-value attribute', function() {
                expect(this.view.$('label')).toHaveAttr('data-value', 'CAT');
            });

            it('displays the count', function() {
                expect(this.view.$('label')).toContainText('42');
            });

            it('is not selected', function() {
                expect(this.view.$('.icheckbox-hp')).toHaveAttr('aria-checked', 'false');
            });

            describe('then the value is selected', function() {
                beforeEach(function() {
                    this.model.set('selected', true);
                    this.view.updateSelected();
                });

                it('is selected', function() {
                    expect(this.view.$('.icheckbox-hp')).toHaveAttr('aria-checked', 'true');
                });
            });
        });

        describe('with an initially selected value', function() {
            beforeEach(function() {
                this.model = new Backbone.Model({
                    selected: true,
                    value: 'CAT',
                    displayValue: 'Cat',
                    count: 42
                });

                this.view = new SelectModalItemView({model: this.model});
                this.view.render();
            });

            it('is selected', function() {
                expect(this.view.$('.icheckbox-hp')).toHaveAttr('aria-checked', 'true');
            });
        });
    });

});
