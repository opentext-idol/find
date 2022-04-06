/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
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
    'text!find/templates/app/page/search/results/sunburst/field-selection-view.html',
    'i18n!find/nls/bundle',
    'chosen'
], function(Backbone, _, template, i18n) {
    'use strict';

    const optionTemplate = _.template('<option value="<%-field%>" <%- selected ? "selected" : ""%>><%-displayValue%></option>');
    const emptyOptionHtml = '<option value=""></option>';

    return Backbone.View.extend({
        className: 'field-selection-view',
        tagName: 'span',
        template: _.template(template),

        initialize: function(options) {
            this.fields = options.fields;
            this.name = options.name;
            this.allowEmpty = options.allowEmpty;
            this.width = options.width || '20%';

            this.selectionsStart = this.allowEmpty ? [emptyOptionHtml] : [];
        },

        updateModel: function () {
            const fieldId = this.$select.val();
            this.model.set('field', fieldId);
            this.model.set('displayName', fieldId ? _.findWhere(this.fields, {id: fieldId}).displayName : '');
        },

        render: function() {
            this.$el.html(this.template({
                dataPlaceholder: i18n['search.sunburst.fieldPlaceholder.' + this.name]
            }));

            const options = this.selectionsStart.concat(_.map(this.fields, function (field) {
                return optionTemplate({
                    field: field.id,
                    selected: field.id === this.model.get('field'),
                    displayValue: field.displayName
                });
            }, this));

            this.$select = this.$('.parametric-select');

            this.$select.append(options)
                .chosen({
                    width: this.width,
                    allow_single_deselect: this.allowEmpty
                })
                .trigger('chosen:updated');

            this.$select.change(_.bind(this.updateModel, this));

            if (!this.allowEmpty && !_.isEmpty(this.fields)) {
                this.updateModel();
            }

            return this;
        }
    });
});
