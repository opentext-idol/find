import _ from 'underscore';
import Backbone from 'backbone';
import Spectrograph from '../lib/spectrograph'
import imageUrl from '../img/spectrograph.jpg'
import template from './view.html';

const View = Backbone.View.extend({
    template: _.template(template),

    initialize: function (options) {
        this.data = options.data;
    },

    render: function () {
        this.$el.html(this.template({}));
        const $container = this.$('.stories-graph');

        // spectrograph needs to know final text size to render boxes
        document.fonts.load('8pt Metric').then(() => {
            Spectrograph({
                clustersData: this.data.clusters,
                dayCount: this.data.dayCount,
                imageUrl,
                imageWidth: this.data.image.width,
                imageHeight: this.data.image.height
            }, {
                parent: $container,
                spectrograph: $container.find('.spectrograph'),
                headings: $container.find('.headings')
            });
        });
    }
});

export default {
    render: function (data) {
        return new View({ data: data });
    }
};
