/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module js-whatever/js/spinner-utils
 * @desc Utility functions for dealing with spinner classes
 */
define(function() {
    var spinnerClasses = 'icon-spinner icon-spin';

    //noinspection UnnecessaryLocalVariableJS
    /**
     * @alias module:js-whatever/js/spinner-utils
     * @type {{enableSpinner: Function, disableSpinner: Function}}
     */
    var spinnerUtils = {
        /**
         * @desc Removes the given CSS classes from $element and applies the classes icon-spinner and icon-spin
         * @param {jQuery} $element
         * @param {string} classesToRemove The CSS classes to remove
         */
        enableSpinner: function($element, classesToRemove) {
            $element.removeClass(classesToRemove).addClass(spinnerClasses);
        },

        /**
         * @desc Adds the given CSS classes to $element and removes the classes icon-spinner and icon-spin
         * @param {jQuery} $element
         * @param {string} classesToAdd The CSS classes to add
         */
        disableSpinner: function($element, classesToAdd) {
            $element.removeClass(spinnerClasses).addClass(classesToAdd);
        }
    };

    return spinnerUtils;
});