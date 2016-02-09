/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/snapshots/data-panel-view',
    'backbone'
], function(DataPanelView, Backbone) {

    describe('Snapshot data panel view', function() {
        beforeEach(function() {
            this.model = new Backbone.Model({
                id: 0,
                name: 'Fred',
                age: 23,
                job: 'driver'
            });

            this.view = new DataPanelView({
                model: this.model,
                targetAttributes: ['name', 'age'],
                processAttributes: function(attributes) {
                    return [
                        {title: 'Name', content: attributes.name},
                        {title: 'Age', content: attributes.age}
                    ];
                }
            });

            this.view.render();
        });

        it('displays an item for each of the target attributes', function() {
            var $titles = this.view.$('.text-muted');
            expect($titles).toHaveLength(2);
            expect($titles.eq(0)).toHaveText('Name');
            expect($titles.eq(1)).toHaveText('Age');

            var $content = this.view.$('.font-bold');
            expect($content).toHaveLength(2);
            expect($content.eq(0)).toHaveText('Fred');
            expect($content.eq(1)).toHaveText('23');
        });

        it('updates when the model changes', function() {
            this.model.set('name', 'Bob');

            expect(this.view.$('.font-bold').eq(0)).toHaveText('Bob');
        });
    });

});
