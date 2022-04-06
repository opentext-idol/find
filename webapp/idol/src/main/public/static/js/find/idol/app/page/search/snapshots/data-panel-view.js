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
    'underscore',
    'js-whatever/js/model-any-changed-attribute-listener',
    'text!find/templates/app/page/search/snapshots/data-panel-item.html'
], function(Backbone, _, addChangeListener, itemTemplate) {
    'use strict';

    var itemTemplateFunction = _.template(itemTemplate);

    /**
     * Given a hash of model attributes, return a list of title-content pairs to be rendered. There need not be a one to
     * one mapping between the target attributes and the output title-content pairs. Null values will be ignored.
     * @callback DataPanelAttributesProcessor
     * @parameter {Backbone.Model} model
     * @parameter {Object} attributes
     * @returns {?{title: String, content: String}[]}
     */
    /**
     * @typedef {Object} DataPanelViewOptions
     * @property {DataPanelAttributesProcessor} processAttributes
     * @property {String[]} targetAttributes Model attributes to listen to and pass to the processAttributes function
     */
    /**
     * View for rendering a list of title-content pairs derived from model attributes.
     * @name DataPanelView
     * @constructor
     * @param {DataPanelViewOptions} options
     */
    return Backbone.View.extend({
        className: 'p-l-sm p-b-sm',

        initialize: function(options) {
            this.targetAttributes = options.targetAttributes;
            this.processAttributes = options.processAttributes;
            addChangeListener(this, this.model, this.targetAttributes, this.render);
        },

        render: function() {
            var items = this.processAttributes(this.model, this.model.pick(this.targetAttributes));
            var html = _.map(_.compact(items), itemTemplateFunction).join('');
            this.$el.html(html);
        }
    });
});
