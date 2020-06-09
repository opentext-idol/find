/*
 * (c) Copyright 2014-2016 Micro Focus or one of its affiliates.
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
    'backbone',
    'jquery',
    'find/app/page/search/spellcheck-view'
], function(Backbone, $, SpellCheckView) {

    describe('Spellcheck View', function() {
        beforeEach(function() {
            this.documentsCollection = new Backbone.Collection();
            this.documentsCollection.autoCorrection = null;

            this.documentsCollection.getAutoCorrection = function() {
                return this.autoCorrection;
            };

            this.queryModel = new Backbone.Model();
            this.spellCheckView = new SpellCheckView({
                queryModel: this.queryModel,
                documentsCollection: this.documentsCollection
            });
            this.spellCheckView.render();
        });

        it('is only visible if the documentsCollection.autoCorrection is true', function() {
            expect(this.spellCheckView.$el).toHaveClass('hidden');
        });
    });
});
