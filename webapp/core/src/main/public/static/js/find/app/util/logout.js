/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    'underscore'
], function($, _) {
    'use strict';

    // Logout is a POST request. This cannot be done with an href, so append a magic form to the body and submit it.
    return function(url) {
        var $form = $(_.template('<form action="<%=url%>" method="post"></form>')({
            url: url
        }));

        $('body').append($form);
        $form.submit();
    };
});
