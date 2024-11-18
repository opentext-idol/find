define([
    'jquery',
    'underscore',
    'find/app/util/database-name-resolver'
], function($, _, databaseNameResolver) {

    return {
        /**
         * Get the view document URL for a document in a text index.
         * @param {String} model
         * @param {Boolean} highlightExpressions
         * @param {Boolean} original Whether to retrieve the original file (skip conversion to HTML)
         * @return {String}
         */
        getHref: function(model, highlightExpressions, original) {
            const commonParams = {
                index: databaseNameResolver.resolveDatabaseNameForDocumentModel(model),
                highlightExpressions: highlightExpressions || null
            };

            return 'api/public/view/viewDocument?' + $.param(_.defaults({
                reference: model.get('reference'),
                part: original ? 'ORIGINAL' : 'DOCUMENT',
                // relative to DOCUMENT API call
                urlPrefix: 'viewDocument?' + $.param(_.defaults({
                    part: 'SUBDOCUMENT'
                }, commonParams))
            }, commonParams), true);
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
