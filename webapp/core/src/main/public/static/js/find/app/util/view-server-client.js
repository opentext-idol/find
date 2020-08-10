define([
    'jquery',
    'find/app/util/database-name-resolver'
], function($, databaseNameResolver) {

    return {
        /**
         * Get the view document URL for a document in a text index.
         * @param {String} model
         * @param {Boolean} highlightExpressions
         * @param {Boolean} original Whether to retrieve the original file (skip conversion to HTML)
         * @return {String}
         */
        getHref: function(model, highlightExpressions, original) {
            var database = databaseNameResolver.resolveDatabaseNameForDocumentModel(model);
            
            return 'api/public/view/viewDocument?' + $.param({
                reference: model.get('reference'),
                index: database,
                highlightExpressions: highlightExpressions || null,
                original: original
            }, true);
        },

        /**
         * Get the view document URL for a search result triggered by a static content promotion
         * @param {String} reference Reference of the search result
         * @return {String}
         */
        getStaticContentPromotionHref: function(reference) {
            return 'api/public/view/viewStaticContentPromotion?' + $.param({
                    reference: reference
                });
        }
    };

});
