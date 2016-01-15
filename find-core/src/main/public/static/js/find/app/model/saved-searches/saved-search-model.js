define([
    'backbone',
    'moment',
    'underscore'
], function(Backbone, moment, _) {

    var DATE_FIELDS = [
        'minDate',
        'maxDate',
        'dateCreated',
        'dateModified'
    ];

    return Backbone.Model.extend({
        defaults: {
            queryText: '',
            title: '',
            indexes: [],
            parametricValues: []
        },

        parse: function(response) {
            var dateAttributes = _.mapObject(_.pick(response, DATE_FIELDS), function(value) {
                return value && moment(value);
            });

            return _.defaults(dateAttributes, response);
        }
    });

});