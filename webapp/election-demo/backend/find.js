const superagent = require('superagent')

const BASE_URL = process.env['FIND_BASE_URL'] || 'http://localhost:8080'

exports.URLs = {
    documents: 'api/public/search/query-text-index/results',
    concepts: 'api/public/search/find-related-concepts',
    parametricValues: 'api/public/parametric/values',
    pollingDataBuckets: 'api/public/pollingdata/buckets',
    themeClusters: 'api/public/themetracker/clusters',
    themeImage: 'api/public/themetracker/image'
}

exports.getRaw = (requestPath, params) => {
    const url = BASE_URL + '/' + requestPath
    console.log(url, params)
    return superagent.get(url).query(params)
}

exports.get = (requestPath, params, callback) => {
    exports.getRaw(requestPath, params).end((err, res) => {
        if (err) {
            callback(err)
        } else if (res.status !== 200) {
            callback('request failed: HTTP ' + res.status)
        } else {
            callback(null, res.body)
        }
    })
}
