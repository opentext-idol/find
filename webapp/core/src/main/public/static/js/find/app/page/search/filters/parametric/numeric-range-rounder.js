define([], function () {
    const SIGNIFICANT_FIGURES = 3;

    return function (numberOfSignificantFigures) {
        const significantFigures = numberOfSignificantFigures || SIGNIFICANT_FIGURES;
        return {
            round: function (value, min, max) {
                const diff = max - min;
                const scientificDiff = diff.toExponential();
                const exponent = +scientificDiff.substring(scientificDiff.indexOf('e') + 1);
                return diff === 0 ? +value.toPrecision(significantFigures) : Math.round(value * Math.pow(10, significantFigures - exponent - 1)) / Math.pow(10, significantFigures - exponent - 1);
            }
        }
    }
});