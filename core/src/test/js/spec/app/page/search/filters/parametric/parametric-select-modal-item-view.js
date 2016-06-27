define([
    'find/app/page/search/filters/parametric/parametric-select-modal-item-view',
    'backbone'
], function(SelectModalItemView, Backbone) {

    describe('Select field modal item view', function() {
        beforeEach(function() {
            this.model = new Backbone.Model({
                selected: false,
                id: 41,
                count: 42
            });

            this.view = new SelectModalItemView({
                model: this.model,
                field: {id: 42}
            });
            this.view.render();
        });

        it('sets a data-field attribute', function() {
            expect(this.view.$el).toHaveAttr('data-id', '41');
        });

        it('displays the count to be 42', function() {
            expect(this.view.$('label')).toContainText(42);
        });

        it('should be unselected', function() {
            expect(this.view.$('.icheckbox-hp')).toHaveAttr('aria-checked', 'false');
        });
    });

});
