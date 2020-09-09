const fs = require('fs')
const Path = require('path')
const _ = require('underscore')
const async = require('async')
const Polls = require('./polls')
const TopicsInterest = require('./topics-interest')
const Stories = require('./stories')

const destPath = Path.join(__dirname, '..', 'frontend', 'src')

async.parallelLimit({
    polls: Polls.getData,
    topicsInterest: TopicsInterest.getData,
    stories: _.partial(Stories.getData, destPath)
}, 1, (err, data) => {
    if (err) {
        console.error(err)
        process.exit(1)
    } else {
        fs.writeFileSync(Path.join(destPath, 'data.json'), JSON.stringify(data))
    }
})

