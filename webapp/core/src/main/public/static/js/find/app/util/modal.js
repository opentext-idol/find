/*
 * Copyright 2017 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'js-whatever/js/modal',
    'find/app/util/global-key-listener'
], function(Modal, globalKeyListener) {
    'use strict';

    return Modal.extend({
        initialize: function(){
            Modal.prototype.initialize.apply(this, arguments);

            this.listenTo(globalKeyListener, 'escape', function(){
                this.hide();
            })
        }
    });
});
