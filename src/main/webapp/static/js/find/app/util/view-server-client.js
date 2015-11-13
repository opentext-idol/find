define([
    'jquery'
], function($) {

    return {
        getHref: function(reference, index, domain) {
            if (index && domain) {
                return '../api/public/view/viewDocument?' + $.param({
                        domain: domain,
                        index: index,
                        reference: reference
                    });
            } else {
                return '../api/public/view/viewStaticContentPromotion?' + $.param({
                        reference: reference
                    });
            }
        }
    };

});