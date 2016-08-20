define([
    'backbone',
    'underscore',
    'text!find/templates/app/page/search/results/sunburst/field-selection-view.html',
    'i18n!find/nls/bundle',
    'parametric-refinement/prettify-field-name',
    'chosen'
], function(Backbone, _, template, i18n, prettifyFieldName) {

    'use strict';

    var optionTemplate = _.template('<option value="<%-field%>" <%= selected ? "selected" : ""%>><%-displayValue%></option>');
    var emptyOptionHtml = '<option value=""></option>';

    return Backbone.View.extend({
        className: 'field-selection-view',
        tagName: 'span',
        template: _.template(template),

        initialize: function(options) {
            this.fields = options.fields;
            this.name = options.name;
            this.allowEmpty = options.allowEmpty;
            this.width  = options.width || '20%';

            this.selectionsStart = this.allowEmpty ? [emptyOptionHtml] : [];
        },

        updateModel: function () {
            this.model.set('field', this.$select.val());
            this.model.set('displayValue', prettifyFieldName(this.$select.val()));
        },

        render: function() {
            this.$el.html(this.template({
                dataPlaceholder: i18n['search.sunburst.fieldPlaceholder.' + this.name]
            }));

            var options = this.selectionsStart.concat(_.map(this.fields, function(field) {
                return optionTemplate({
                    field: field,
                    selected: field === this.model.get('field'),
                    displayValue: prettifyFieldName(field)
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
