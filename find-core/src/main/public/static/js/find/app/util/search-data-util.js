define([
    'underscore',
    'parametric-refinement/to-field-text-node'
], function (_, toFieldTextNode) {

    function wrapQuotes(concept) {
        return '"' + concept + '"';
    }

    // WARNING: This logic is duplicated in the server-side SavedSearch class
    var makeQueryText = function(inputText, relatedConcepts) {
        if (!inputText){
            return '';
        }

        if (_.isEmpty(relatedConcepts)){
            return inputText;
        }

        return '(' + inputText + ') ' + _.map(_.flatten(_.uniq(relatedConcepts)), wrapQuotes).join(' ');
    };

    var buildIndexes = function(selectedIndexesArray) {
        return _.map(selectedIndexesArray, function(index) {
            return index.domain ? encodeURIComponent(index.domain) + ':' + encodeURIComponent(index.name) : encodeURIComponent(index.name);
        });
    };

    var buildQuery = function(model) {
        return {
            minDate: model.get('minDate'),
            maxDate: model.get('maxDate'),
            queryText: makeQueryText(model.get('queryText'), model.get('relatedConcepts')),
            databases: buildIndexes(model.get('indexes')),
            fieldText: toFieldTextNode(model.get('parametricValues'))
        };
    };

    return {
        makeQueryText: makeQueryText,
        buildIndexes: buildIndexes,
        buildQuery: buildQuery
    };
});