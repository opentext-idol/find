/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'backbone',
    'uuidjs',
    'i18n!find/nls/bundle',
    'find/app/configuration',
    'fieldtext/js/field-text-parser'
], function (_, Backbone, UUID, i18n, configuration, fieldTextParser) {

    /**
     * Convert array references to object with `true` values.  Return a copy if already an object.
     */
    const normaliseRefs = function (references) {
        if (_.isArray(references)) {
            const referencesObj = {};
            _.each(references, function (reference) {
                referencesObj[reference] = true;
            });
            return referencesObj;
        } else {
            // ensure modifications don't affect provided objects
            return _.clone(references);
        }
    }

    /**
     * Select documents to form a whitelist or blacklist.  Attributes should not be changed via
     * `set` - use a specific method instead.
     *
     * @property changedReferences An object whose properties are document references and values are
     *           `true`, for documents which changed since the last change to this model.  Can be
     *           `null`, which should be treated as every document having changed.
     *
     * Attributes:
     *   - isWhitelist: whether this is a whitelist (else a blacklist)
     *   - references: object where properties are document references and values are `true`.
     *                 Initial value provided may be an array of references.
     */
    return Backbone.Model.extend({
        // function to ensure modifications don't affect the default references object
        defaults: function () {
            return { isWhitelist: false, references: {} };
        },

        initialize: function () {
            this.changedReferences = null;
            this.set('references', normaliseRefs(this.get('references')));
        },

        /**
         * @returns {number} Number of documents in whitelist/blacklist
         */
        getReferencesCount: function () {
            return _.size(this.get('references'));
        },

        /**
         * @returns {Array} Document references
         */
        getReferences: function () {
            return _.keys(this.get('references'));
        },

        /**
         * @returns {boolean} Whether all attributes have their default values
         */
        isDefault: function () {
            return _.isEqual(this.toJSON(), this.defaults());
        },

        /**
         * @param reference Document reference
         * @returns Whether the document is selected
         */
        isSelected: function (reference) {
            const isListed = !!this.get('references')[reference];
            return this.get('isWhitelist') ? isListed : !isListed;
        },

        /**
         * @returns {fieldTextParser.ExpressionNode|null} Fieldtext which applies the document
         *          selection
         */
        toFieldText: function () {
            const referenceField = configuration().referenceField;
            const isWhitelist = this.get('isWhitelist');
            const fieldValues = _.map(this.getReferences(), encodeURIComponent);

            if (fieldValues.length === 0) {
                if (isWhitelist) {
                    const uuid = UUID.generate();
                    return new fieldTextParser.ExpressionNode(
                        'MATCH', [referenceField], [uuid]);
                } else {
                    return null;
                }
            } else {
                const matchAny = new fieldTextParser.ExpressionNode(
                    'MATCH', [referenceField], fieldValues);
                return isWhitelist ? matchAny : matchAny.NOT();
            }
        },

        /**
         * @returns {string} Human-readable description of the current state
         */
        describe: function () {
            const isWhitelist = this.get('isWhitelist');
            const numDocs = this.getReferencesCount();

            let description;
            if (numDocs === 0 && isWhitelist) {
                description = i18n['search.documentSelection.none'];
            } else if (numDocs === 0) {
                description = i18n['search.documentSelection.all'];
            } else if (isWhitelist) {
                description = i18n['search.documentSelection.whitelist'](numDocs);
            } else {
                description = i18n['search.documentSelection.blacklist'](numDocs);
            }

            return description;
        },

        /**
         * Include the document with the given reference in the selection.
         */
        select: function (reference) {
            const references = this.get('references');
            if (this.get('isWhitelist')) {
                references[reference] = true;
            } else {
                delete references[reference];
            }

            this.changedReferences = {};
            this.changedReferences[reference] = true;
            this.trigger('change change:references');
        },

        /**
         * Exclude the document with the given reference from the selection.
         */
        exclude: function (reference) {
            const references = this.get('references');
            if (this.get('isWhitelist')) {
                delete references[reference];
            } else {
                references[reference] = true;
            }

            this.changedReferences = {};
            this.changedReferences[reference] = true;
            this.trigger('change change:references');
        },

        /**
         * Reset all attributes to their default values.
         */
        reset: function () {
            this.changedReferences = null;
            this.set(this.defaults());
        },

        /**
         * Include all documents in the selection.
         */
        selectAll: function () {
            this.changedReferences = null;
            this.set({
                isWhitelist: false,
                references: {}
            });
        },

        /**
         * Exclude all documents from the selection.
         */
        excludeAll: function () {
            this.changedReferences = null;
            this.set({
                isWhitelist: true,
                references: {}
            });
        },

        /**
         * Modify attributes to match the document selection stored in the given saved search model.
         */
        setFromSavedSearch: function (savedSearchModel) {
            this.changedReferences = null;
            const attributes = savedSearchModel.toDocumentSelectionModelAttributes();
            this.set(_.defaults({
                references: normaliseRefs(attributes.references)
            }, attributes));
        }

    });

});
