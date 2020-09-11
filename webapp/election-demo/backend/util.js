const _ = require('underscore')

exports.cbSplit = (errCb, resCb) => (err, res) => {
    if (err) {
        errCb(err)
    } else {
        resCb(res)
    }
}

exports.cbDone = (cb, t) => exports.cbSplit(cb, res => cb(null, t(res)))

exports.pipe = (inStream, outStream, callback_) => {
    inStream.pipe(outStream)
    const callback = _.once(callback_)
    outStream.on('error', err => callback(err || 'error'))
    outStream.on('finish', () => callback(null))
}
