const fs = require('fs')
const Path = require('path')
const _ = require('underscore')
const async = require('async')
const moment = require('moment')
const Util = require('./util')
const Find = require('./find')

const DAY_COUNT = 7
const DAY_SECONDS = 24 * 60 * 60
const SPAN_SECONDS = DAY_COUNT * DAY_SECONDS
const IMAGE_WIDTH = 1200
const IMAGE_HEIGHT = IMAGE_WIDTH

exports.getData = function (destPath, callback) {
    const endDateUnix = Math.floor(moment().unix() / DAY_SECONDS) * DAY_SECONDS + DAY_SECONDS
    const startDate = moment.unix(endDateUnix - SPAN_SECONDS)

    const clustersRequestData = {
        startDate: startDate.unix(),
        interval: SPAN_SECONDS
    }

    const imageRequestData = {
        startDate: startDate.unix(),
        interval: SPAN_SECONDS,
        imageWidth: IMAGE_WIDTH,
        imageHeight: IMAGE_HEIGHT
    }

    async.parallel({
        clusters: _.partial(Find.get, Find.URLs.themeClusters, clustersRequestData),
        image: done => {
            const inStream = Find.getRaw(Find.URLs.themeImage, imageRequestData)
            const outStream = fs.createWriteStream(
                Path.join(destPath, 'src', 'img', 'spectrograph.jpg'))
            Util.pipe(inStream, outStream, done)
        }
    }, Util.cbDone(callback, results => ({
        clusters: results.clusters.clusters,
        dayCount: DAY_COUNT,
        image: { width: IMAGE_WIDTH, height: IMAGE_HEIGHT }
    })))
}
