define([
    'backbone'
], function(Backbone) {

    return Backbone.Model.extend({
        defaults: {
            queryText: '',
            indexes: [],
            fieldText: null,
            minDate: null,
            maxDate: null
        },

        getFieldTextString: function() {
            var fieldText = this.get('fieldText');

            if(fieldText) {
                return fieldText.toString();
            } else {
                return null;
            }
        },

        setParametricFieldText: function(fieldText) {
            this.set('fieldText', fieldText);
        }
    });
});