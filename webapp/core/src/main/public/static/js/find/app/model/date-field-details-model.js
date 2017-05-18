define([
    'find/app/model/find-base-collection'
], function (FindBaseCollection) {
    "use strict";

    return FindBaseCollection.Model.extend({
        url: 'api/public/parametric/date/value-details'
    });
});