define([
    'find/app/model/find-base-collection'
], function(FindBaseCollection) {

    return FindBaseCollection.Model.extend({
        url: 'api/public/parametric/value-details'
    });
});