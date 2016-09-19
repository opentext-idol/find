define([
    'underscore',
    'parametric-refinement/to-field-text-node'
], function (_, toFieldTextNode) {

    function wrapQuotes(concept) {
        return '"' + concept + '"';
    }

    // WARNING: This logic is duplicated in the server-side SavedSearch class
    /**
     * Build query text from the text in the search bar and an array of concept groups.
     * @param {string} inputText
     * @param {Array.<Array.<string>>} concepts
     * @return {string}
     */
    function makeQueryText(inputText, concepts) {
        if (!inputText){
            return '';
        }

        if (_.isEmpty(concepts)){
            return inputText;
        }

        var tail = _.map(_.flatten(_.uniq(concepts)), wrapQuotes).join(' ');
        return inputText === '*' ? tail : '(' + inputText + ') ' + tail;
    }

    var buildIndexes = function(selectedIndexesArray) {
        return _.map(selectedIndexesArray, function(index) {
            return index.domain ? encodeURIComponent(index.domain) + ':' + encodeURIComponent(index.name) : encodeURIComponent(index.name);
        });
    };

    var buildFieldText = function(parametricValues) {
        var fieldTextNode = toFieldTextNode(parametricValues);
        return fieldTextNode && fieldTextNode.toString();
    };

    var buildQuery = function(model) {
        return {
            minDate: model.get('minDate'),
            maxDate: model.get('maxDate'),
            queryText: makeQueryText(model.get('queryText'), model.get('relatedConcepts')),
            databases: buildIndexes(model.get('indexes')),
            fieldText: buildFieldText(model.get('parametricValues')),
            anyLanguage: true
        };
    };

    return {
        makeQueryText: makeQueryText,
        buildIndexes: buildIndexes,
        buildQuery: buildQuery,
        buildFieldText: buildFieldText
    };
});