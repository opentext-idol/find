/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/customisations/valued-list-view'
], function(ValuedListView) {
    'use strict';

    var itemTemplate = _.template('<%-data[textAttribute]%>');

    var update = function(value) {
        this.$el.val(value);
    };

    var updateAndTrigger = ValuedListView.monitorChange(update);

    return ValuedListView.extend({
        tagName: 'select',

        events: {
            'change': function() {
                this.fireChange(this.getValue());
            }
        },

        initialize: function(options) {
            ValuedListView.prototype.initialize.call(this, _.defaults(options, {
                itemOptions: {
                    tagName: 'option',
                    template: itemTemplate,
                    attributes: function() {
                        return {
                            value: this.model.get(options.valueAttribute)
                        }
                    },
                    templateOptions: {
                        textAttribute: options.textAttribute
                    }
                }
            }));
        },

        getValue: function() {
            return this.$el.val();
        },

        setValue: function(value, silent) {
            if (silent) {
                update.call(this, value);
            } else {
                updateAndTrigger.call(this, value);
            }
        }
    });

});
