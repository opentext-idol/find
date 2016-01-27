define([
    'underscore'
], function(_) {
    function containsAll(a, b, predicate) {
        // for every item in a...
        return _.all(a, function(aItem) {
            // there exists an item in b...
            return _.any(b, function(bItem) {
                // that matches the predicate
                return predicate(aItem, bItem);
            });
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

        return containsAll(a, b, predicate) && containsAll(b, a, predicate);
    };
});