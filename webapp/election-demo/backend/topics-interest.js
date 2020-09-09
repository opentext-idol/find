const _ = require('underscore')
const async = require('async')
const moment = require('moment')
const Find = require('./find')
const Util = require('./util')

const SOURCES = [
    { name: 'Democrat', indexes: [
            'twitter-democrat-senate',
            'twitter-democrat-house',
            'twitter-democrat-biden'
        ] },
    { name: 'Citizens', indexes: ['twitter-replies'] },
    { name: 'Republican', indexes: [
            'twitter-republican-senate',
            'twitter-republican-house',
            'twitter-republican-trump'
        ] }
]

const TOPIC_INDEXES = [
    'twitter-democrat-senate',
    'twitter-democrat-house',
    'twitter-democrat-biden',
    'twitter-republican-senate',
    'twitter-republican-house',
    'twitter-republican-trump'
]

const HARDCODED_TOPICS = [
    { name: 'President Trump', terms: ['President Trump'] },
    { name: 'Coronavirus', terms: ['Coronavirus'] },
    { name: 'Covid', terms: ['Covid'] }
]

const TIME_SPAN = moment.duration(1, 'week')
const MAX_TOPICS = 15
const MAX_DOCS_SAMPLED = 5000
const SENTIMENT_FIELD = 'SENTIMENT'
const SENTIMENT_THRESHOLD = 0.15

const sum = ns => _.reduce(ns, (total, n) => total + n, 0)

const normInterest = interest => Math.round(Math.pow(interest, 0.7))

const filterConcepts = response => _.chain(response)
    // A negative cluster indicates that the associated documents did not fall into a cluster
    .reject(concept => concept.cluster < 0)
    .groupBy('cluster')
    // Take a maximum of 10 for each cluster
    .map(cluster => _.first(cluster, 10))
    .flatten()
    .value()

const getDocumentCount = (topic, source, startDate, callback) => {
    const requestData = {
        indexes: source.indexes,
        text: '"' + topic + '"',
        min_date: startDate.toISOString(),
        max_results: 1,
        summary: 'off',
        highlight: false,
        queryType: 'RAW'
    }

    Find.get(Find.URLs.documents, requestData,
        Util.cbDone(callback, response => response.totalResults))
}

const getSources = (startDate, callback) => {
    async.map(SOURCES, (source, done) => {
        getDocumentCount('*', source, startDate, Util.cbDone(done,
            documents => ({ name: source.name, documents })))
    }, callback)
}

const getTopicSentiment = (topic, source, startDate, callback) => {
    const requestData = {
        fieldNames: [SENTIMENT_FIELD],
        databases: source.indexes,
        queryText: '"' + topic + '"',
        maxValues: 3,
        minDate: startDate.toISOString()
    }

    Find.get(Find.URLs.parametricValues, requestData, Util.cbDone(callback, fields => {
        const field = _.findWhere(fields, { id: SENTIMENT_FIELD })
        const values = field ? field.values : []
        const counts = _.object(_.pluck(values, 'value'), _.pluck(values, 'count'))
        const pos = counts.POSITIVE || 0
        const neut = counts.NEUTRAL || 0
        const neg = counts.NEGATIVE || 0
        const sentimentValue = (pos - neg) / (pos + neut + neg)
        return sentimentValue < -SENTIMENT_THRESHOLD ? 'Negative' :
            (sentimentValue > SENTIMENT_THRESHOLD ? 'Positive' : 'Neutral')
    }))
}

const getTopicsInterest = (startDate, callback) => {
    const requestData = {
        databases: TOPIC_INDEXES,
        maxResults: MAX_DOCS_SAMPLED,
        queryText: '*',
        minDate: startDate.toISOString(),
        queryType: 'RAW'
    }

    Find.get(Find.URLs.concepts, requestData, Util.cbSplit(callback, response => {
        const topics = {}
        _.find(filterConcepts(response), concept => {
            if (_.size(topics) >= MAX_TOPICS) {
                return true
            }

            if (topics[concept.cluster] === undefined) {
                topics[concept.cluster] = { name: concept.text, terms: [concept.text] }
            } else {
                topics[concept.cluster].terms.push(concept.text)
            }
            return false
        })

        const allTopics = HARDCODED_TOPICS.concat(_.values(topics))
        async.map(allTopics, (topic, topicDone) => {
            async.map(SOURCES, (source, sourceDone) => {
                async.parallel({
                    interest: _.partial(getDocumentCount, topic.name, source, startDate),
                    sentiment: _.partial(getTopicSentiment, topic.name, source, startDate)
                }, sourceDone)
            }, Util.cbDone(topicDone, sourceResults => {
                return {
                    sources: _.object(_.pluck(SOURCES, 'name'), sourceResults),
                    terms: topic.terms
                }
            }))
        }, Util.cbDone(callback, topicResults => {
            return _.object(_.pluck(allTopics, 'name'), topicResults)
        }))
    }))
}

exports.getData = callback => {
    const startDate = moment().subtract(TIME_SPAN)
    async.parallel({
        sources: _.partial(getSources, startDate),
        topicsInterest: _.partial(getTopicsInterest, startDate)
    }, callback)
}
