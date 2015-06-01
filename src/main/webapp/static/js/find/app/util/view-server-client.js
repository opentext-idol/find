define([

], function () {

    return {
        getHref: function(reference, index) {
            if (index) {
                return '../api/user/view/viewDocument?' + $.param({
                    indexes: index,
                    reference: reference
                });
            } else {
                return '../api/user/view/viewStaticContentPromotion?' + $.param({
                    reference: reference
                });
            }
        }
    }
});