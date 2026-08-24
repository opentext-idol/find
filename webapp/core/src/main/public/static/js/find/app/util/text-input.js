const Backbone = require('backbone');
const _ = require('underscore');
const template = require('find/templates/app/util/text-input.html');

module.exports = Backbone.View.extend({
        template: _.template(template),

        events: {
            'input input': function() {
                this.model.set(this.modelAttribute, this.$input.val());
            },
            'submit': function(e) {
                e.preventDefault();
            }
        },

        initialize: function(options) {
            this.templateOptions = options.templateOptions;
            this.modelAttribute = options.modelAttribute;
        },

        render: function() {
            this.$el.html(this.template(_.extend({
                value: this.model.get(this.modelAttribute)
            }, this.templateOptions)));

            this.$input = this.$('.js-text-input');
        }
    });

