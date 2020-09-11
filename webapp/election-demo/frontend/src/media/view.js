import _ from 'underscore';
import Backbone from 'backbone';
import template from './view.html';

const View = Backbone.View.extend({
    template: _.template(template),

    initialize: function (options) {
        this.data = options.data;
    },

    render: function () {
        this.$el.html(this.template({}));
        this.$container = this.$('.media-thing');
        this.showClips();
    },

    showClips: function () {
        // TODO: show the clips using this.data
    }

});

export default {
    render: function (data) {
        return new View({ data: data });
    }
};
