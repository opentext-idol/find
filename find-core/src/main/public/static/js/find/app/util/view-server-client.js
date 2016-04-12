define([
    'jquery',
    'find/app/util/database-name-resolver'
], function($, databaseNameResolver) {

    return {
        /**
         * Get the view document URL for a document in a text index.
         * @param {String} reference
         * @param {String} model
         * @param {Boolean} highlightExpressions
         * @return {String}
         */
        getHref: function(reference, model, highlightExpressions) {
            var database = databaseNameResolver.resolveDatabaseNameForDocumentModel(model);
            
            return '../api/public/view/viewDocument?' + $.param({
                    reference: reference,
                    index: database,
                    highlightExpressions: highlightExpressions || null
                }, true);
        },

        /**
         * Get the view document URL for a search result triggered by a static content promotion
         * @param {String} reference Reference of the search result
         * @return {String}
         */
        getStaticContentPromotionHref: function(reference) {
            return '../api/public/view/viewStaticContentPromotion?' + $.param({
                    reference: reference
                });
        }
    };

});
