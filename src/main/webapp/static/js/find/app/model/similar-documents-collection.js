define([
    'underscore',
    'find/app/model/find-base-collection'
], function(_, BaseCollection) {

    return BaseCollection.extend({
        url: '../api/public/search/similar-documents',

        initialize: function(models, options) {
            this.indexes = options.indexes;
            this.reference = options.reference;
        },

        fetch: function(options) {
            return BaseCollection.prototype.fetch.call(this, _.extend(options || {}, {
                data: {
                    indexes: this.indexes,
                    reference: this.reference
                }
            }));
        }
    });

});