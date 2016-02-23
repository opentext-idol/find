define([
    'underscore',
    'find/app/model/comparisons/comparison-model',
    'find/app/util/search-data-util'
], function (_, ComparisonsModel, searchDataUtil) {

    return function(primaryModel, secondaryModel) {
        var primaryStateMatchId = primaryModel.get('stateMatchId');
        var secondaryStateMatchId = secondaryModel.get('stateMatchId');

        var comparisonModelArguments = {};

        primaryStateMatchId ? comparisonModelArguments.firstQueryStateToken = primaryStateMatchId : comparisonModelArguments.firstRestrictions = searchDataUtil.buildQuery(primaryModel);
        secondaryStateMatchId ? comparisonModelArguments.secondQueryStateToken = secondaryStateMatchId : comparisonModelArguments.secondRestrictions = searchDataUtil.buildQuery(secondaryModel);

        return new ComparisonsModel(comparisonModelArguments);
    }
});