/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'js-whatever/js/modal',
    'find/app/page/search/filters/parametric/parametric-select-modal-view',
    'parametric-refinement/selected-values-collection',
    'text!find/templates/app/page/loading-spinner.html',
    'i18n!find/nls/bundle',
    'underscore'
], function(Backbone, Modal, ParametricSelectView, SelectedValuesCollection, loadingSpinnerTemplate, i18n, _) {
    'use strict';

    // Convert a selected value model to a field value pair
    function toAttributes(model) {
        return model.pick('field', 'value');
    }

    return Modal.extend({
        className: Modal.prototype.className + ' fixed-height-modal',

        initialize: function(options) {
            this.parametricFieldsCollection = options.parametricFieldsCollection;
            this.externalSelectedValues = options.selectedParametricValues;
            this.queryModel = options.queryModel;

            // Track values selected in the modal, but only apply them when the user closes it
            this.selectedParametricValues = new SelectedValuesCollection(this.externalSelectedValues.map(toAttributes));

            this.parametricSelectView = new ParametricSelectView({
                currentFieldGroup: options.currentFieldGroup,
                parametricFieldsCollection: options.parametricFieldsCollection,
                selectedParametricValues: this.selectedParametricValues
            });

            Modal.prototype.initialize.call(this, {
                actionButtonClass: 'button-primary',
                actionButtonText: i18n['app.apply'],
                secondaryButtonText: i18n['app.cancel'],
                contentView: this.parametricSelectView,
                title: i18n['search.parametricFilters.modal.title'],
                actionButtonCallback: _.bind(function() {
                    // Update the search with new selected values on close
                    this.externalSelectedValues.set(this.selectedParametricValues.map(toAttributes));

                    this.hide();
                }, this)
            });
        },

        remove: function() {
            this.selectedParametricValues.remove();
            Modal.prototype.remove.call(this);
        }
    });
});
