/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
define([
    'backbone',
    'find/app/page/search/spellcheck-view'
], function(Backbone, SpellCheckView) {

    return Backbone.View.extend({
        initialize: function(options) {
            if (!options.configuration.hasBiRole) {
                this.spellCheckView = new SpellCheckView({
                    documentsCollection: options.documentsCollection,
                    queryModel: options.queryModel
                });
            }
        },

        render: function() {
            if(this.spellCheckView) {
                this.$el.append(this.spellCheckView.$el);
                this.spellCheckView.render();
            }

            return this;
        },

        remove: function() {
            if(this.spellCheckView) {
                this.spellCheckView.remove();
            }

            Backbone.View.prototype.remove.call(this);
        }
    });
});
