import _ from 'underscore';
import Backbone from 'backbone';
import moment from 'moment';
import Trending from '../lib/trending';
import FieldSelectionView from '../lib/field-selection-view';
import template from './view.html';

const PARTIES = {
    DEM: { name: 'Democrat', colour: '#1668c1' },
    REP: { name: 'Republican', colour: '#ff454f' }
};
const TIME_SCALES = [
    { id: '6m', displayName: 'Last 6 Months' },
    { id: '6w', displayName: 'Last 6 Weeks' },
    { id: '2w', displayName: 'Last 2 Weeks' }
];
const CHART_ASPECT_RATIO = 1.8;

const processData = function (data) {
    return _.mapObject(data, function (timeScaleData, timeScaleId) {
        const processedData = _.chain(timeScaleData.data)
            .filter(function (partyData) {
                return PARTIES[partyData.id];
            })
            .map(function (partyData) {
                return {
                    name: PARTIES[partyData.id].name,
                    color: PARTIES[partyData.id].colour,
                    points: _.map(partyData.values, function (value) {
                        const midTime = moment(value.min)
                            .add(Math.floor(value.bucketSize / 2), 'seconds');
                        return {
                            rate: value.count / 10,
                            mid: midTime.toDate(),
                            min: moment(value.min).toDate(),
                            max: moment(value.max).toDate()
                        };
                    })
                };
            })
            .value();

        const min = _.chain(processedData)
            .pluck('points').flatten().pluck('rate').min().value();
        const max = _.chain(processedData)
            .pluck('points').flatten().pluck('rate').max().value();
        const padding = 0.7 * (max - min);
        return {
            chartData: {
                data: processedData,
                minRate: _.max([0, min - padding]),
                maxRate: max + padding,
                yUnit: '%'
            },
            startDate: moment(timeScaleData.startDate),
            endDate: moment(timeScaleData.endDate)
        };
    });
}

const View = Backbone.View.extend({
    template: _.template(template),

    initialize: function (options) {
        this.data = processData(options.data);
        this.timescaleModel = new Backbone.Model({ field: '6m' });

        this.listenTo(this.timescaleModel, 'change:field', this.showGraph);
    },

    render: function () {
        this.$el.html(this.template({}));

        this.timescaleSelector = new FieldSelectionView({
            model: this.timescaleModel,
            fields: TIME_SCALES,
            allowEmpty: false
        });
        this.$('.polls-timescale-selector').prepend(this.timescaleSelector.$el);
        this.timescaleSelector.render();

        this.$container = this.$('.polls-graph');
        this.$container.height(Math.ceil(this.$container.width() / CHART_ASPECT_RATIO));

        this.showGraph();
    },

    showGraph: function () {
        this.$container.html('');
        const chart = new Trending({
            el: this.$container.get(0),
            tooltipText: function (value, unit, series, t0, t1) {
                 return value + unit + ' ' + series + ' poll average between ' + t0 + ' and ' + t1;
            },
            zoomEnabled: false,
            dragEnabled: false,
            hoverEnabled: true,
            yAxisLabelForUnit: _.constant('Poll Average (%)'),
            yAxisUnitsText: _.constant('%')
        });

        const data = this.data[this.timescaleModel.get('field')];
        chart.draw({
            reloaded: false,
            chartData: data.chartData,
            minDate: data.startDate.toDate(),
            maxDate: data.endDate.toDate()
        });
    }

});

export default {
    render: function (data) {
        return new View({ data: data });
    }
};
