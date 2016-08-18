define([
    'js-whatever/js/list-item-view',
    'underscore',
    'parametric-refinement/prettify-field-name',
    'text!find/templates/app/util/csv-field-selection-list-item.html',
    'i18n!find/nls/bundle',
    'iCheck'
], function(ListItemView, _, prettifyFieldName, template, i18n) {

    'use strict';

    return ListItemView.extend({
        template: _.template(template),

        initialize: function(options) {
            ListItemView.prototype.initialize.call(this, _.defaults({
                template: this.template,
                templateOptions: {
                    fieldDataId: options.model.id,
                    fieldPrintedLabel: i18n['search.document.' + options.model.id] || prettifyFieldName(options.model.id)
                }
            }, options));
        },

        render: function() {
            ListItemView.prototype.render.apply(this, arguments);

            this.$el.iCheck({checkboxClass: 'icheckbox-hp'});
            this.updateSelected();
        },

        updateSelected: function() {
            this.$el.iCheck(this.model.get('selected') ? 'check' : 'uncheck');
        }
    });
});
