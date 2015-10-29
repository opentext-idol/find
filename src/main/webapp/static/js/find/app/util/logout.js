define([
    'jquery'
], function($) {

    // Logout is a POST request. This cannot be done with an href, so append a magic form to the body and submit it.
    return function(url) {
        var $form = $(_.template('<form action="<%=url%>" method="post"></form>')({
            url: url
        }));

        $('body').append($form);
        $form.submit();
    };
});