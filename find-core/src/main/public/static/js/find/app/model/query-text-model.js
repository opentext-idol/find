define([
    'backbone'
], function(Backbone) {

    return Backbone.Model.extend({
        defaults: {
            inputText: '',
            relatedConcepts: []
        },

        makeQueryText: function(){
            if (!this.get('inputText')){
                this.set('relatedConcepts', []);
                return '';
            }

            if (_.isEmpty(this.get('relatedConcepts'))){
                return this.get('inputText');
            }

            return "(" + this.get('inputText') + ") AND " + this.get('relatedConcepts').join(' AND ');
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
