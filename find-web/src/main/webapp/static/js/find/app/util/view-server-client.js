define([
    '../../../../bower_components/jquery/jquery'
], function($) {

    return {
        /**
         * Get the view document URL for a document in a text index.
         * @param {String} reference
         * @param {String} index
         * @param {String} domain
         * @return {String}
         */
        getHref: function(reference, index, domain) {
            return '../api/public/view/viewDocument?' + $.param({
                    domain: domain,
                    index: index,
                    reference: reference
                });
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