define([
    'find/app/model/document-model'
], function(DocumentModel) {

    describe('Document model', function() {
        describe('parse method', function() {
            beforeEach(function() {
                this.parse = DocumentModel.prototype.parse;
            });

            it('uses the title from the response if present', function() {
                var title = 'My Document';
                expect(this.parse({reference: 'my-document', title: title}).title).toBe(title);
            });

        });
    });

});
