define([
    'backbone',
    'jquery',
    'underscore',
    'find/app/util/string-blank',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/input-view.html',
    'typeahead'
], function(Backbone, $, _, stringBlank, i18n, template) {

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
            this.queryModel = options.queryModel;

            // For example, when clicking one of the suggested search links
            this.listenTo(this.queryModel, 'change:queryText', this.updateText);

            this.search = _.debounce(function(query, refresh) {
                if (refresh) {
                    options.queryModel.refresh(query);
                } else {
                    options.queryModel.set('queryText', query);
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
                    // Don't look for suggestions if the query is blank
                    if (stringBlank(query)) {
                        sync([]);
                    } else {
                        $.get('../api/public/typeahead', {
                            text: query
                        }, function(results) {
                            async(results);
                        });
                    }
                }
            });

            this.updateText();
        },

        updateText: function() {
            if (this.$input) {
                this.$input.typeahead('val', this.queryModel.get('queryText'));
            }
        }
    });

});
