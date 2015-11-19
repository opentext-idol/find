/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module js-whatever/js/list-view
 */
define([
    'backbone',
    'js-whatever/js/list-item-view'
], function(Backbone, ListItemView) {

    // TODO: ListViewOptions.useCollectionChange is deprecated and should be removed in a later version
    /**
     * @typedef module:js-whatever/js/list-view.ListView~ListViewOptions
     * @property {Backbone.Collection} collection The collection containing the items to render
     * @property {boolean} [useCollectionChange=true] (Deprecated, use collectionChangeEvents instead) Whether to re-render
     * the associated ItemView when the collection fires a change event. This is more efficient than creating one change
     * listener for each model.
     * @property {Object.<String, String>|Boolean} [collectionChangeEvents=true] If true, re-renders the associated ItemView
     * when the collection fires a change event. If an object, the keys are model attributes to listen to change events
     * for and the values are the names of functions to call on the associated ItemView when they occur. The callbacks
     * are passed the same arguments triggered by the collection. If false, no change listener is created. This is much
     * more efficient than creating one change listener for each model.
     * @property {function} [ItemView=ListItemView] The Backbone.View constructor to instantiate for each model
     * @property {object} [itemOptions={}] The options to pass to the ItemView constructor in addition to the model
     * @property {String[]} [proxyEvents=[]] Events to proxy from ItemViews, prefixed with 'item:'
     * @property {String} [headerHtml] Optional HTML to render at the top of the list.
     * @property {String} [footerHtml] Optional HTML to render at the bottom of the list.
     */
    /**
     * @name module:js-whatever/js/list-view.ListView
     * @desc View representing a Backbone.Collection. Renders one ItemView for each model, and re-renders in response
     * to collection events
     * @constructor
     * @param {module:js-whatever/js/list-view.ListView~ListViewOptions} options
     * @extends Backbone.View
     */
    return Backbone.View.extend(/** @lends module:js-whatever/js/list-view.ListView.prototype */{
        initialize: function(options) {
            this.itemOptions = options.itemOptions || {};
            this.ItemView = options.ItemView || ListItemView;
            this.proxyEvents = options.proxyEvents || [];
            this.footerHtml = options.footerHtml;
            this.headerHtml = options.headerHtml;

            this.views = {};

            this.listenTo(this.collection, 'add', this.onAdd);
            this.listenTo(this.collection, 'remove', this.onRemove);
            this.listenTo(this.collection, 'sort', this.onSort);
            this.listenTo(this.collection, 'reset', this.render);

            var useCollectionChange = _.isUndefined(options.useCollectionChange) ? true : options.useCollectionChange;

            if ((_.isUndefined(options.collectionChangeEvents) && useCollectionChange) || options.collectionChangeEvents === true) {
                this.listenTo(this.collection, 'change', function(model) {
                    var view = this.views[model.cid];
                    view && view.render();
                });
            } else if (!_.isUndefined(options.collectionChangeEvents)) {
                _.each(options.collectionChangeEvents, function(methodName, attribute) {
                    this.listenTo(this.collection, 'change:' + attribute, function(model) {
                        var view = this.views[model.cid];
                        view && view[methodName].apply(view, arguments);
                    });
                }, this);
            }
        },

        render: function() {
            this.removeViews();
            var $fragment = $(document.createDocumentFragment());

            if (this.headerHtml) {
                $fragment.append(this.headerHtml);
                this.$header = $fragment.children().last();
            }

            this.collection.each(function(model) {
                var view = this.createItemView(model);
                $fragment.append(view.el);
            }, this);

            if (this.footerHtml) {
                var $footer = $(this.footerHtml);
                this.$footer = $footer.first();
                $fragment.append($footer);
            }

            this.$el.html($fragment);
            return this;
        },

        /**
         * @desc Instantiates and renders an ItemView for the given model. Adds it to the map of model cid to ItemView.
         * @param model The model which needs a view
         * @returns {ItemView} The new ItemView
         */
        createItemView: function(model) {
            var view = this.views[model.cid] = new this.ItemView(_.extend({
                model: model
            }, this.itemOptions));

            _.each(this.proxyEvents, function(event) {
                this.listenTo(view, event, function() {
                    this.trigger.apply(this, ['item:' + event].concat(Array.prototype.slice.call(arguments, 0)));
                });
            }, this);

            view.render();
            return view;
        },

        /**
         * @desc Callback called when a model is added to the collection
         * @param {Backbone.Model} model The model added to the collection
         */
        onAdd: function(model) {
            var view = this.createItemView(model);

            if (this.$footer) {
                view.$el.insertBefore(this.$footer);
            } else {
                this.$el.append(view.el);
            }
        },

        /**
         * @desc Callback called when a model is removed from the collection
         * @param {Backbone.Model} model The model that was removed from the collection
         */
        onRemove: function(model) {
            var view = this.views[model.cid];

            if (view) {
                this.removeView(view);
            }
        },

        /**
         * @desc Callback called when the collection is sorted. This will reorder the ItemViews to reflect the new
         * collection order
         */
        onSort: function() {
            var $previous = this.$header;

            this.collection.each(function(model) {
                var view = this.views[model.cid];

                if (view) {
                    var $item = view.$el;

                    if ($previous) {
                        $previous = $item.insertAfter($previous);
                    } else {
                        $previous = $item.prependTo(this.$el);
                    }
                }
            }, this);
        },

        /**
         * @desc Backbone.View remove method. Also calls each ItemView's remove method.
         */
        remove: function() {
            this.removeViews();
            Backbone.View.prototype.remove.call(this);
        },

        /**
         * Remove the view and stopListening to it
         * @param view The view to remove
         */
        removeView: function(view) {
            view.remove();
            this.stopListening(view);
        },

        /**
         * @desc Call each ItemView's remove method and reset the map of views.
         */
        removeViews: function() {
            _.each(this.views, this.removeView, this);
            this.views = {};
        }
    });

});
