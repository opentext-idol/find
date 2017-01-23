/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
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
