const fs = require('fs')
const Path = require('path')
const _ = require('underscore')
const async = require('async')
const Polls = require('./polls')
const Media = require('./media')
const TopicsInterest = require('./topics-interest')
const Stories = require('./stories')

const destPath = Path.join(__dirname, '..', 'frontend')

async.parallelLimit({
    polls: Polls.getData,
    media: _.partial(Media.getData, destPath),
    topicsInterest: TopicsInterest.getData,
    stories: _.partial(Stories.getData, destPath)
}, 1, (err, data) => {
    if (err) {
        console.error(err)
        process.exit(1)
    } else {
        fs.writeFileSync(Path.join(destPath, 'src', 'data.json'), JSON.stringify(data))
    }
})

