define([

], function () {

    return {
        getHref: function(reference, index) {
            if (index) {
                return '../api/public/view/viewDocument?' + $.param({
                    indexes: index.id,
                    reference: reference
                });
            } else {
                return '../api/public/view/viewStaticContentPromotion?' + $.param({
                    reference: reference
                });
            }
        }
    }
});