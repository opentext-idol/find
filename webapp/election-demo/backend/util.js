exports.cbSplit = (errCb, resCb) => (err, res) => {
    if (err) {
        errCb(err)
    } else {
        resCb(res)
    }
}

exports.cbDone = (cb, t) => exports.cbSplit(cb, res => cb(null, t(res)))
