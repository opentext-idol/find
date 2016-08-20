define([
    'backbone',
    'find/app/model/find-base-collection',
    'find/app/configuration'
], function(Backbone, FindBaseCollection, configuration) {
    "use strict";

    return FindBaseCollection.extend({
        model: Backbone.Model.extend({
            defaults: {
                values: []
            }
        }),
        
        initialize: function (models, options) {
            this.url = options.url;
        },

        parse: function(response) {
            return _.map(response, function(attributes) {
                var fieldMap = _.findWhere(configuration().parametricDisplayValues, {name: attributes.id});
                
                if (!fieldMap) {
                    return attributes;
                }
                
                var values = _.map(attributes.values, function(value) {
                    var param = _.findWhere(fieldMap.values, {name: value.value});

                    return param ? _.extend(value, {displayName: param.displayName}) : value;
                });

                return _.extend(attributes, {
                    displayName: fieldMap.displayName,
                    values: values
                });
            });
        }
    });
});
