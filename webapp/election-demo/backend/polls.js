const _ = require('underscore')
const async = require('async')
const moment = require('moment')
const Find = require('./find')
const Util = require('./util')

const TIME_SCALES = [
    { id: '6m', duration: moment.duration(6, 'month'), bucketCount: 20 },
    { id: '6w', duration: moment.duration(6, 'week'), bucketCount: 20 },
    { id: '2w', duration: moment.duration(2, 'week'), bucketCount: 10 }
]

exports.getData = callback => {
    const endDate = moment()

    async.map(TIME_SCALES, (timeScale, done) => {
        const startDate = endDate.clone().subtract(timeScale.duration)
        const requestData = {
            bucketCount: timeScale.bucketCount,
            bucketMin: startDate.toISOString(),
            bucketMax: endDate.toISOString()
        }

        Find.get(Find.URLs.pollingDataBuckets, requestData,
            Util.cbDone(done, timeScaleData => ({
                data: timeScaleData,
                startDate: startDate.toISOString(),
                endDate: endDate.toISOString()
            })))
    }, Util.cbDone(callback, data =>_.object(_.pluck(TIME_SCALES, 'id'), data)))
}
