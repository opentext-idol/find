/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
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
    'find/app/util/modal',
    'find/app/page/search/filters/parametric/parametric-select-modal-view',
    'parametric-refinement/selected-values-collection',
    'text!find/templates/app/page/loading-spinner.html',
    'i18n!find/nls/bundle',
    'underscore'
], function(Backbone, Modal, ParametricSelectView, SelectedValuesCollection, loadingSpinnerTemplate, i18n, _) {
    'use strict';

    // Convert a selected value model to a field value pair
    function toAttributes(model) {
        return model.pick('field', 'displayName', 'value', 'displayValue', 'type', 'range');
    }

    return Modal.extend({
        className: Modal.prototype.className + ' fixed-height-modal wide-modal',

        events: _.defaults({
            'shown.bs.modal': function() {
                // The content view will be visible now, so check if we need to load parametric values
                this.parametricSelectView.checkScroll();
            }
        }, Modal.prototype.events),

        initialize: function(options) {
            this.externalSelectedValues = options.selectedParametricValues;

            // Track values selected in the modal, but only apply them when the user closes it
            this.selectedParametricValues = new SelectedValuesCollection(this.externalSelectedValues.map(toAttributes));

            this.parametricSelectView = new ParametricSelectView({
                initialField: options.initialField,
                indexesCollection: options.indexesCollection,
                queryModel: options.queryModel,
                parametricFieldsCollection: options.parametricFieldsCollection,
                parametricValuesSort: options.parametricValuesSort,
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
            this.parametricSelectView.remove();
            Modal.prototype.remove.call(this);
        }
    });
});
