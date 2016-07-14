define([
    'backbone',
    'jquery',
    'underscore',
    'find/app/page/search/filters/parametric/parametric-select-modal-list-view',
    'text!find/templates/app/page/search/filters/parametric/parametric-select-modal-view.html',
    'text!find/templates/app/page/loading-spinner.html',
    'i18n!find/nls/bundle',
    'iCheck'
], function(Backbone, $, _, ParametricSelectModalListView, template, loadingSpinnerTemplate, i18n) {
    'use strict';

    var fieldTemplate = _.template('<div id="<%-field.id.replace(/[/]/g, \'_\')%>" class="tab-pane <%- currentFieldGroup === field.id ? \'active\' : \'\'%>" role="tabpanel"></div>');

    return Backbone.View.extend({
        template: _.template(template),
        loadingTemplate: _.template(loadingSpinnerTemplate)({i18n: i18n, large: false}),
        className: 'full-height',

        initialize: function(options) {
            _.bindAll(this, 'renderFields');
            this.parametricDisplayCollection = options.parametricDisplayCollection;              
            this.selectCollection = options.selectCollection;
            this.currentFieldGroup = options.currentFieldGroup;
            this.parametricCollection = options.parametricCollection;
        },

        renderFields: function () {
            var viewHtml = $(this.template({
                parametricDisplayCollection: this.parametricDisplayCollection,
                currentFieldGroup: this.currentFieldGroup
            }));


            var $fragment = $(document.createDocumentFragment());

            this.parametricDisplayCollection.each(function (field) {
                var $field = $(fieldTemplate({currentFieldGroup: this.currentFieldGroup, field: field}));

                var listView = new ParametricSelectModalListView({
                    field: field,
                    parametricDisplayCollection: this.parametricDisplayCollection,
                    selectCollection: this.selectCollection,
                    allValues: _.map(this.parametricCollection.get(field.id).get('values'), function(attributes) {
                        return {
                            id: attributes.value
                        }
                    })
                });

                $field.append(listView.render().$el);

                $fragment.append($field);
            }, this);

            this.$el.html(viewHtml);

            this.$('.tab-content').html($fragment);
        },

        render: function() {
            this.$el.html(this.loadingTemplate);
        }
    });
});
