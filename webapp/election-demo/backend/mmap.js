const _ = require('underscore')
const superagent = require('superagent')
require('superagent-proxy')(superagent)

const BASE_URL = process.env['MMAP_BASE_URL'] || 'http://localhost:8081'
const PROXY = process.env['MMAP_PROXY']

exports.requests = {
    faceImages: channel => ({
        path: 'vms/api/v1/channels/' + encodeURIComponent(channel) + '/events',
        params: { type: 'face' }
    })
}

// get request object using full URL
exports.getRaw = (url, params) => {
    console.log(url, allParams)
    return superagent.get(url).query(allParams).proxy(PROXY)
}

// get JSON body using relative URL
exports.get = (request, params, callback) => {
    const url = BASE_URL + '/' + request.path
    const allParams = _.extend({}, request.params, params)
    exports.getRaw(url, allParams)
        .set('Accept', 'application/json')
        .end((err, res) => {
            if (err) {
                callback(err)
            } else if (res.status !== 200) {
                callback('request failed: HTTP ' + res.status)
            } else {
                callback(null, res.body)
            }
        })
}
