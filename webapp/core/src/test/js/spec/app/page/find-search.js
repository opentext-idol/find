define([
    'find/app/page/find-search',
    'find/app/configuration',
    'backbone'
], function (FindSearch, configuration, Backbone) {
    'use strict';

    describe('Find Search', function() {
        beforeEach(function() {
            spyOn(Backbone.history, 'getFragment').and.returnValue('');

            configuration.and.returnValue({
                savedSearchConfig: {
                    pollForUpdates: false
                }
            });

            this.findSearch = new (FindSearch.extend({
                IndexesCollection: Backbone.Collection,
                ServiceView: Backbone.View.extend({
                    getSelectedTab: function() {
                        return '';
                    }
                })
            }))({
                savedQueryCollection: new Backbone.Collection(),
                indexesCollection: new Backbone.Collection()
            });
        });

        it('should show the service view when the inputText is not the empty string', function() {
            this.findSearch.render();

            this.findSearch.searchModel.set('inputText', 'text');

            var $tabbedSearchContainer = this.findSearch.$('.tabbed-search-container');

            expect($tabbedSearchContainer.css('display')).not.toEqual('none');
        });

        it('should animate the input view when the inputText is not the empty string', function() {
            this.findSearch.render();

            this.findSearch.searchModel.set('inputText', 'text');

            var $animatedContainer = this.findSearch.$('.animated-container');

            expect($animatedContainer).not.toEqual([]);
        });
    });

});