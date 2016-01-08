define([
    'backbone',
    'jquery',
    'underscore',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/input-view.html',
    'typeahead'
], function(Backbone, $, _, i18n, template) {

    var html = _.template(template)({i18n: i18n});

    return Backbone.View.extend({
        events: {
            'input .find-input': function() {
                this.search(this.$input.typeahead('val'), false);
            },
            'submit .find-form': function(event) {
                event.preventDefault();
                this.search(this.$input.typeahead('val'), true);
                this.$input.typeahead('close');
            },
            'typeahead:select': function() {
                this.search(this.$input.typeahead('val'), false);
            }
        },

        initialize: function(options) {
            var queryModel = options.queryModel;

            this.search = _.debounce(function(query, refresh) {
                if (refresh) {
                    queryModel.refresh(query);
                } else {
                    queryModel.set('queryText', query);
                }
            }, 500);
        },

        render: function() {
            this.$el.html(html);
            this.$input = this.$('.find-input');

            this.$input.typeahead({
                hint: false,
                hightlight: true,
                minLength: 1
            }, {
                async: true,
                limit: 7,
                source: function(query, sync, async) {
                    $.get('../api/public/typeahead', {
                        text: query
                    }, function(results) {
                        async(results);
                    });
                }
            });
        }
    });

});
