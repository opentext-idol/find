/**
 * @module js-whatever/js/list-item-view
 */
define([
    'backbone'
], function(Backbone) {

    /**
     * @typedef module:js-whatever/js/list-item-view.ListItemView~ListItemViewOptions
     * @property {Backbone.Model} model The model this view represents
     * @property {function} template Template function used to fill the View's element during render. Passed the model's
     * attributes as the data property, as well as any templateOptions.
     * @property {object} [templateOptions={}] Options passed to the template in addition to the model's attributes
     */
    /**
     * @name module:js-whatever/js/list-item-view.ListItemView
     * @desc A Backbone.View representing a single Backbone.Model. On render, sets the content of it's element to the
     * template function called with the model attributes. Also sets a data-id attribute to the id of the model.
     * @constructor
     * @param {module:js-whatever/js/list-item-view.ListItemView~ListItemViewOptions} options
     * @extends Backbone.View
     */
    return Backbone.View.extend(/** @lends module:js-whatever/js/list-item-view.ListItemView.prototype */{
        initialize: function(options) {
            this.template = options.template;
            this.templateOptions = options.templateOptions || {};
            this.setDataIdAttribute();
        },

        render: function() {
            this.$el.html(this.template(_.extend({
                data: this.model.attributes
            }, this.templateOptions)));

            this.setDataIdAttribute();
        },

        /**
         * @desc Sets a data-id attribute on the View's element with the model id as the value
         * @protected
         */
        setDataIdAttribute: function() {
            this.$el.attr('data-id', this.model.id);
        }
    });

});