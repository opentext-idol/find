/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/model/entity-collection'
], function(EntityCollection) {

    describe('Entity collection', function() {
        beforeEach(function() {
           this.collection = new EntityCollection([
                {cluster: 1, text: 'gnome'},
                {cluster: 1, text: 'garden'},
                {cluster: 1, text: 'watering can'},
                {cluster: 2, text: 'plant'},
                {cluster: 2, text: 'slugs'}
            ], {
               getSelectedRelatedConcepts: function () {
                   return ['watering can', 'slugs']
               },
                parse: true
            });
        });

        describe('getClusterEntities function', function() {
            it('returns entity text for a cluster', function() {
                var clusterEntities = this.collection.getClusterEntities(1);
                expect(clusterEntities.length).toBe(2);
                expect(clusterEntities).toContain('garden');
                expect(clusterEntities).toContain('gnome');
            });

            it('does not contain any duplicates of query text or selected related concepts', function() {
                var clusterEntities = this.collection.getClusterEntities(1);
                expect(clusterEntities).not.toContain('watering can');
                
                var clusterEntities2 = this.collection.getClusterEntities(2);
                expect(clusterEntities2).not.toContain('slugs');
            })
        });
    });

});
