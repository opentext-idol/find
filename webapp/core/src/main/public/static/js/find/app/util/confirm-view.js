/*
 * Copyright 2016 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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
