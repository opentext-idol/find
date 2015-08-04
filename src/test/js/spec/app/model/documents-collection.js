define([
    'find/app/model/documents-collection'
], function(DocumentsCollection) {

    describe('Documents collection', function() {
        describe('model parse method', function() {
            beforeEach(function() {
                this.parse = DocumentsCollection.prototype.model.prototype.parse;
            });

            it('uses the title from the response if present', function() {
                var title = 'My Document';
                expect(this.parse({reference: 'my-document', title: title}).title).toBe(title);
            });

            it('uses the last part of a windows file path as the title if there is no title', function() {
                expect(this.parse({reference: 'C:\\Documents\\file.txt'}).title).toBe('file.txt');
            });

            it('uses the last part of a unix file path as the title if there is no title', function() {
                expect(this.parse({reference: '/home/user/another-file.txt'}).title).toBe('another-file.txt');
            });

            it('uses the whole reference if the reference finishes with a slash', function() {
                var reference = 'http://example.com/main/';
                expect(this.parse({reference: reference}).title).toBe(reference);
            });

            it('uses the whole reference if the reference finishes with a slash followed by whitespace', function() {
                var reference = 'foo/   \n ';
                expect(this.parse({reference: reference}).title).toBe(reference);
            });
        });
    });

});
