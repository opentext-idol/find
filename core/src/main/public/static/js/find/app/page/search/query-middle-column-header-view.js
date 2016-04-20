/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'find/app/page/search/spellcheck-view',
    'find/app/model/search-filters-collection',
    'find/app/page/search/filter-display/filter-display-view'
], function(Backbone, SpellCheckView, SearchFiltersCollection, FilterDisplayView) {

    return Backbone.View.extend({
        // May be overridden
        SearchFiltersCollection: SearchFiltersCollection,

        initialize: function(options) {
            this.spellCheckView = new SpellCheckView({
                documentsCollection: options.documentsCollection,
                queryModel: options.queryModel
            });

            this.filtersCollection = new this.SearchFiltersCollection([], {
                queryState: options.queryState,
                indexesCollection: options.indexesCollection
            });

            this.filterDisplayView = new FilterDisplayView({collection: this.filtersCollection});
        },

        render: function() {
            this.$el.empty()
                .append(this.spellCheckView.$el)
                .append(this.filterDisplayView.$el);

            this.spellCheckView.render();
            this.filterDisplayView.render();

            return this;
        },

        remove: function() {
            this.filtersCollection.stopListening();
            this.filterDisplayView.remove();
            this.spellCheckView.remove();

            Backbone.View.prototype.remove.call(this);
        }
    });

});
