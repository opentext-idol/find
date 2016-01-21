define([
    'backbone'
], function(Backbone) {

    var Sort = {
        date: 'date',
        relevance: 'relevance'
    };

    return Backbone.Model.extend({
        defaults: {
            autoCorrect: true,
            queryText: '',
            indexes: [],
            fieldText: null,
            minDate: undefined,
            maxDate: undefined,
            sort: Sort.relevance
        },

        getIsoDate: function(type) {
            var date = this.get(type);
            if(date) {
                return date.toISOString();
            } else {
                return null;
            }
        },

        hasAnyChangedAttributes: function(attributes) {
            return _.any(attributes, function (attr) {
                return _.has(this.changedAttributes(), attr);
            }, this);
        }
    }, {
        Sort: Sort
    });

});
