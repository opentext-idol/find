/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module js-whatever/js/confirm
 */
define([
    'js-whatever/js/confirm-view'
], function(Confirm){
    //noinspection UnnecessaryLocalVariableJS
    /**
     * @alias module:js-whatever/js/confirm
     * @desc Creates and returns a new instance of {@link module:js-whatever/js/confirm-view.ConfirmView|ConfirmView}
     * @param {ConfirmViewOptions} config Options passed to the confirm view
     */
    var confirm = function(config) {
        return new Confirm(config);
    };

    return confirm;
});