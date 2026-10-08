const $ = require('jquery');
const _ = require('underscore');
const databaseNameResolver = require('find/app/util/database-name-resolver');

module.exports = {
        /**
         * Get the view document URL for a document in a text index.
         * @param {String} model
         * @param {Boolean} highlightExpressions
         * @param {Boolean} original Whether to retrieve the original file (skip conversion to HTML)
         * @return {String}
         */
        getHref: function(model, highlightExpressions, original) {
            const commonParams = {
                index: databaseNameResolver.resolveDatabaseNameForDocumentModel(model),
                highlightExpressions: highlightExpressions || null
            };

            return 'api/public/view/viewDocument?' + $.param(_.defaults({
                reference: model.get('reference'),
                filepath: this.getFilename(model),
                part: original ? 'ORIGINAL' : 'DOCUMENT',
                // relative to DOCUMENT API call
                urlPrefix: 'viewDocument?' + $.param(_.defaults({
                    part: 'SUBDOCUMENT'
                }, commonParams))
            }, commonParams), true);
        },

        /**
         * Get the view document URL for a search result triggered by a static content promotion
         * @param {String} reference Reference of the search result
         * @return {String}
         */
        getStaticContentPromotionHref: function(reference) {
            return 'api/public/view/viewStaticContentPromotion?' + $.param({
                    reference: reference
                });
        },

          /**
           * Get the filename of a document from its model. If the title isn't already a filename, uses
           * the content-type to extract the file extension to use.
           * @param {String} model
           * @return {String}
           */
          getFilename: function (model) {
            let title = model.get('title');

            // Heuristic: if the title already looks like it has a file extension, return it as-is.
            const fileExtensionRegex = /[a-zA-z0-9]{1,5}/;
            let title_split = title.split('.');
            if (
              title_split.length > 1 &&
              fileExtensionRegex.test(title_split[title_split.length - 1])
            ) {
              return title;
            }

            // Otherwise, guess file extension based on MIME type
            let media_type = model.get('contentType');
            let extension;
            if (media_type == 'text/plain') {
              extension = '.txt';
            } else {
              // Dirty heuristic but works for most common file types,
              // and better than not putting any file extension at all
              extension = '.' + media_type.split('/')[1];
            }

            return title + extension;
        },
};
