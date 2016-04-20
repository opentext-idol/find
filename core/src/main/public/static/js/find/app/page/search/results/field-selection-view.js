define([
    'backbone',
    'underscore',
    'text!find/templates/app/page/search/results/sunburst/field-selection-view.html',
    'i18n!find/nls/bundle',
    'chosen'
], function(Backbone, _, template, i18n) {

    'use strict';

    var optionTemplate = _.template('<option value="<%-field%>" <%= selected ? "selected" : ""%>><%-field%></option>');
    var emptyOptionHtml = '<option value=""></option>';

    return Backbone.View.extend({
        className: 'field-selection-view',
        tagName: 'span',
        template: _.template(template),

        initialize: function(options) {
            this.fields = options.fields;
            this.name = options.name;
            this.allowEmpty = options.allowEmpty;

            this.selectionsStart = this.allowEmpty ? [emptyOptionHtml] : [];
        },

        render: function() {
            this.$el.html(this.template({
                dataPlaceholder: i18n['search.sunburst.fieldPlaceholder.' + this.name]
            }));

            var options = this.selectionsStart.concat(_.map(this.fields, function(field) {
                return optionTemplate({
                    field: field,
                    selected: field === this.model.get('field')
                });
            }, this));

            this.$select = this.$('.parametric-select');

            this.$select.append(options)
                .chosen({
                    width: '20%',
                    allow_single_deselect: this.allowEmpty
                })
                .trigger('chosen:updated');

            this.$select.change(_.bind(function() {
                this.model.set('field', this.$select.val());
            }, this));

            return this;
        }
    });

});
