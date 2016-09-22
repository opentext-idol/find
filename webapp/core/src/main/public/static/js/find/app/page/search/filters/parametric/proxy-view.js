define([
    'backbone'
], function(Backbone) {
    return Backbone.View.extend({
        initialize: function(options) {
            this.viewTypes = options.viewTypes;
            this.typeAttribute = options.typeAttribute || 'type';

            const type = this.model.get(this.typeAttribute);
            const Constructor = this.viewTypes[type].Constructor;
            
            this.childView = new Constructor(_.extend({
                model: this.model
            }, options[this.viewTypes[type].options]));

            this.childView.setElement(this.$el);
        },
        
        render: function() {
            this.childView.render();
            return this;
        },
        
        remove: function() {
            this.childView.remove();
            Backbone.View.prototype.remove.call(this);
        }
    })
});
