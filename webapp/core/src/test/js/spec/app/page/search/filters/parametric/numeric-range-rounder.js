define([
    'find/app/page/search/filters/parametric/numeric-range-rounder'
], function(rounder) {
    describe('Numeric Range Rounder', function() {
        it('should return large values to the correct number of default significant figures', function () {
            expect(rounder().round(45678, 40000, 50000)).toEqual(45700);
        });
        
        it('should return decimal values to the correct number of default significant figures', function () {
            expect(rounder().round(550.51515, 550.9, 550.3)).toEqual(550.515);
        });
        
        it('should return small decimal values to the correct number of default significant figures', function () {
            expect(rounder().round(0.00051515, 0.0009, 0.0003)).toEqual(0.000515);
        });
        
        it('should handle single value data', function () {
            expect(rounder().round(45678, 45678, 45678)).toEqual(45700);
        });
        
        it('should handle single value decimal data', function () {
            expect(rounder().round(0.00045678, 0.00045678, 0.00045678)).toEqual(0.000457);
        });
        
        it('should return large values to the correct number of specified significant figures', function () {
            expect(rounder(2).round(45678, 40000, 50000)).toEqual(46000);
        });
    });
});