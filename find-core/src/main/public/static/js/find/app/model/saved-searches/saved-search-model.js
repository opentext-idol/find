define([
    'backbone'
], function(Backbone) {

    return Backbone.Model.extend({
        defaults: {
            queryText: '',
            indexes: [],
            parametricValues: {},
            minDate: undefined,
            maxDate: undefined
        },

        getSearch: function() {
            return {
                queryText: this.queryText,
                indexes: this.indexes,
                fieldText: this.parametricValues.toFieldText(),
                minDate: undefined,
                maxDate: undefined
            }
        }
    });
});