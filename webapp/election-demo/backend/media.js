const Path = require('path')
const _ = require('underscore')
const async = require('async')
const UUID = require('uuid')
const Util = require('./util')
const MMAP = require('./mmap')

exports.getData = function (destBasePath, callback) {
    const destDir = Path.join(destBasePath, 'dist', 'media')
    // TODO: get stuff using API (maybe works):
    // MMAP.get(MMAP.requests.faceImages('channel'), { filter_text: 'someone' }, callback)
    // TODO: remove destDir and create it again so it's empty

    // TODO: for each media file:
    const fileName = UUID.v4() + '.jpg'
    const destPath = Path.join(destDir, fileName)
    // TODO: save file:
    // Util.pipe(MMAP.getRaw(fileUrl, {}), fs.createWriteStream(destPath), callback)
    const fileData = {
        url: 'media/' + encodeURIComponent(fileName)
    }
    callback(null, { files: [fileData] })
}
