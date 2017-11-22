/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'js-whatever/js/confirm-view',
    'find/app/util/global-key-listener',
    'text!find/templates/app/page/settings/confirm-modal.html',
    'underscore'
], function(Confirm, globalKeyListener, confirmTemplate, _) {
    'use strict';

    return Confirm.extend({
        className: 'modal fade',
        template: _.template(confirmTemplate),

        initialize: function(config){
            Confirm.prototype.initialize.apply(this, arguments);

            if (config.closable) {
                this.listenTo(globalKeyListener, 'escape', function(){
                    this.$el.modal('hide');
                })
            }
        }
    });
});
