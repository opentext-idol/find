define([
    'underscore'
], function(_) {
    function bagEquality(a, b, predicate) {
        // for every item in a...
        return _.all(a, function(ai) {
            // create a version of predicate which checks equality to ai
            var boundPredicate = _.partial(predicate, ai);

            // find everything in a which is the same as ai
            var as = _.filter(a, boundPredicate);

            // find everything in b which is the same as ai
            var bs = _.filter(b, boundPredicate);

            // if ai occurs the same number of times in a and b, they are equal
            return as.length === bs.length;
        });
    }

    function referenceEquality(aItem, bItem) {
        return aItem === bItem
    }

    return function(a, b, predicate) {
        if (!a && !b) return true;
        if (!a || !b) return false;

        if (a.length !== b.length) return false;

        predicate = predicate || referenceEquality;

        return bagEquality(a, b, predicate)
    };
});