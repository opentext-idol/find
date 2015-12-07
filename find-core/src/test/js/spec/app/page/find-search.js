define([
    'find/app/configuration',
    'find/app/page/find-search'
], function (configuration, FindSearch) {

    describe('Find Search', function() {
        beforeEach(function() {
            configuration.and.returnValue({hosted: true});
            this.findSearch = new FindSearch();
        });

        it("should show the service view when the queryText is not the empty string", function() {
            this.findSearch.render();

            this.findSearch.queryModel.set('queryText', 'text');

            var $serviceViewContainer = this.findSearch.$el.find('.service-view-container');

            expect($serviceViewContainer.css('display')).not.toEqual('none');
        });

        it("should hide the service view when the queryText is the empty string", function() {
            this.findSearch.render();

            this.findSearch.queryModel.set('queryText', '');

            var $serviceViewContainer = this.findSearch.$el.find('.service-view-container');

            expect($serviceViewContainer.css('display')).toEqual('none');
        });

        it("should animate the input view when the queryText is not the empty string", function() {
            this.findSearch.render();

            this.findSearch.queryModel.set('queryText', 'text');

            var $animatedContainer = this.findSearch.$el.find('.animated-container');

            expect($animatedContainer).not.toEqual([]);
        });

        it("should reverse-animate the input view when the queryText is the empty string", function() {
            this.findSearch.render();

            this.findSearch.queryModel.set('queryText', '');

            var $animatedContainer = this.findSearch.$el.find('.reverse-animated-container');

            expect($animatedContainer).not.toEqual([]);
        });
    });

});