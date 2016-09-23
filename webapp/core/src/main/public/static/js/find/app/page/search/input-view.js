define([
    'backbone',
    'jquery',
    'underscore',
    'find/app/util/database-name-resolver',
    'find/app/util/string-blank',
    'text!find/templates/app/page/search/input-view.html',
    'typeahead',
    'bootstrap'
], function(Backbone, $, _, databaseNameResolver, stringBlank, template) {

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
            this.strategy = options.strategy;
            this.strategy.initialize(this);
        },

        render: function() {
            this.$el.html(this.template({placeholder: this.strategy.placeholder}));
            this.$input = this.$('.find-input');

            this.$input.typeahead({
                hint: false,
                highlight: true,
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
            _.defer(function() {
                this.$input.focus();
            }.bind(this));
        },

        unFocus: function() {
            _.defer(function() {
                this.$input.blur();
            }.bind(this));
        },

        search: function(query) {
            this.strategy.onTextUpdate($.trim(query));
        },

        updateText: function() {
            if (this.$input) {
                this.$input.typeahead('val', this.strategy.onExternalUpdate());
            }
        }
    });

});
