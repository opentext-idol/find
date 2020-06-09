/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

define([
    'backbone',
    'underscore'
], function(Backbone, _) {
    'use strict';

    return Backbone.View.extend({
        initialize: function(options) {
            this.viewTypes = options.viewTypes;
            this.typeAttribute = options.typeAttribute || 'type';

            const type = this.model.get(this.typeAttribute);
            const Constructor = this.viewTypes[type].Constructor;

            this.childView = new Constructor(_.extend({
                model: this.model
            }, options[this.viewTypes[type].options]));

            this.childView.setElement(this.$el);

            this.listenTo(this.childView, 'all', function() {
                this.trigger.apply(this, arguments);
            })
        },

        render: function() {
            this.childView.render();
            return this;
        },

        remove: function() {
            this.childView.remove();
            Backbone.View.prototype.remove.call(this);
        }
    })
});
