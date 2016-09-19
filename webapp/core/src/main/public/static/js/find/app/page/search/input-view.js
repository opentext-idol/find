define([
    'backbone',
    'jquery',
    'underscore',
    'find/app/util/database-name-resolver',
    'find/app/util/string-blank',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/input-view.html',
    'typeahead',
    'bootstrap'
], function(Backbone, $, _, databaseNameResolver, stringBlank, i18n, template) {

    return Backbone.View.extend({
        template: _.template(template),

        events: {
            'submit .find-form': function(event) {
                event.preventDefault();
                this.search(this.$input.typeahead('val'));
                this.$input.typeahead('close');
            },
            'typeahead:select': function() {
                this.search(this.$input.typeahead('val'));
            }
        },

        initialize: function(options) {
            this.listenTo(this.model, 'change:inputText', this.updateText);
        },

        render: function() {
            this.$el.html(this.template({i18n: i18n}));
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
                        $.get('api/public/typeahead', {
                            text: query
                        }, function(results) {
                            async(results);
                        });
                    }
                }
            });

            this.updateText();
        },

        focus: function() {
            _.defer(_.bind(function() {
                this.$input.focus();
            }, this));
        },

        unFocus: function() {
            _.defer(_.bind(function() {
                this.$input.blur();
            }, this));
        },

        search: function(query) {
            this.model.set({inputText: $.trim(query)});
        },

        updateText: function() {
            if (this.$input) {
                this.$input.typeahead('val', this.model.get('inputText'));
            }
        }
    });

});
