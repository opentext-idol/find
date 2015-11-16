/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module js-whatever/js/icontains
 * @desc jQuery pseudo selector to test if an element contains a particular string, ignoring case
 * @example
 * // <div class="test">cats</div>
 *
 * var $test = $('.test');
 *
 * $test.filter(':icontains("cats")').length; // returns 1
 * $test.filter(':icontains("CATS")').length; // returns 1
 * $test.filter(':icontains("bears")').length; // returns 0
 */
define([
    '../../../jquery/jquery'
], function(jQuery) {
    jQuery.expr[':'].icontains = jQuery.expr.createPseudo(function(arg) {
        return function(elem) {
            return jQuery(elem).text().toUpperCase().indexOf(arg.toUpperCase()) >= 0;
        };
    });
});