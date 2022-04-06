/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

define([
    'find/app/model/entity-collection'
], function(EntityCollection) {
    'use strict';

    describe('Entity collection', function() {
        beforeEach(function() {
            this.collection = new EntityCollection([
                {cluster: 1, text: 'gnome'},
                {cluster: 1, text: 'garden'},
                {cluster: 1, text: 'watering can'},
                {cluster: 2, text: 'plant'},
                {cluster: 2, text: 'slugs'}
            ], {
                getSelectedRelatedConcepts: function() {
                    return ['watering can', 'slugs']
                },
                parse: true
            });
        });

        describe('getClusterEntities function', function() {
            it('returns entity text for a cluster', function() {
                const clusterEntities = this.collection.getClusterEntities(1);
                expect(clusterEntities).toHaveLength(2);
                expect(clusterEntities).toContain('garden');
                expect(clusterEntities).toContain('gnome');
            });

            it('does not contain any duplicates of query text or selected related concepts', function() {
                const clusterEntities = this.collection.getClusterEntities(1);
                expect(clusterEntities).not.toContain('watering can');

                const clusterEntities2 = this.collection.getClusterEntities(2);
                expect(clusterEntities2).not.toContain('slugs');
            })
        });
    });
});
