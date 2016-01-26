define([
    'backbone'
], function(Backbone) {

    return Backbone.Model.extend({
        defaults: {
            selectedSearchCid: null,
            inputText: '',
            relatedConcepts: []
        },

        setInputText: function(attributes) {
            if (_.has(attributes, 'inputText')){
                attributes.inputText = attributes.inputText.trim();
            }

            return Backbone.Model.prototype.set.call(this, attributes);
        },

        refresh: function() {
            this.trigger('refresh');
        }
    });

});
