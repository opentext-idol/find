define([
    'backbone',
    'jquery',
    'underscore',
    'js-whatever/js/list-view',
    'find/app/vent',
    'i18n!find/nls/bundle',
    'find/app/page/search/filters/parametric/parametric-select-modal-item-view',
    'find/app/util/generate-error-support-message',
    'text!find/templates/app/page/loading-spinner.html',
    'text!find/templates/app/page/search/filters/parametric/parametric-select-modal-list-view.html',
    'iCheck'
], function (Backbone, $, _, ListView, vent, i18n, ItemView, generateErrorHtml, loadingTemplate, template) {
    'use strict';

    // Height above the bottom of the element at which infinite scroll occurs in pixels
    const INFINITE_SCROLL_OFFSET = 30;

    return Backbone.View.extend({
        className: 'full-height tab-pane',

        html: _.template(template)({
            i18n: i18n,
            loadingHtml: _.template(loadingTemplate)({i18n: i18n, large: false})
        }),

        attributes: {
            role: 'tabpanel'
        },

        events: {
            'scroll': 'checkScroll',

            'ifClicked .parametric-value-all-label': function (e) {
                // if everything is selected, select nothing; otherwise, select everything
                e.preventDefault();
                const paginator = this.paginator;
                const allSelected = paginator.valuesCollection.every(function (model) {
                    return model.get('selected');
                });
                paginator.valuesCollection.forEach(function (model) {
                    paginator.setSelected(model.get('value'), !allSelected);
                });
            },

            'ifClicked .parametric-value-label': function (e) {
                this.paginator.toggleSelection($(e.currentTarget).attr('data-value'));
            }
        },

        initialize: function (options) {
            this.paginator = options.paginator;

            this.listView = new ListView({
                collection: this.paginator.valuesCollection,
                ItemView: ItemView,
                collectionChangeEvents: {
                    selected: 'updateSelected'
                }
            });

            // Check scroll when values are added in case the returned values are not enough to fill the modal
            this.listenTo(this.paginator.valuesCollection, 'update', this.checkScroll);
            this.listenTo(this.paginator.valuesCollection, 'update change', this.updateAll);

            // Check scroll on resize because the height may have changed such that we need more values to fill the modal
            this.listenTo(vent, 'vent:resize', this.checkScroll);

            this.listenTo(this.paginator.stateModel, 'change:loading', this.updateLoading);
            this.listenTo(this.paginator.stateModel, 'change:empty', this.updateEmpty);
            this.listenTo(this.paginator.stateModel, 'change:error', this.updateError);
        },

        render: function () {
            this.$el.html(this.html);

            this.$loading = this.$('.loading-spinner');
            this.$error = this.$('.parametric-select-error');
            this.$empty = this.$('.parametric-select-empty');
            this.$all = this.$('.parametric-value-all-label');

            // don't show 'all' checkbox until we've loaded some values
            this.$all.toggleClass('hide', true);
            this.$all.find('input').iCheck({ checkboxClass: 'icheckbox-hp' });
            this.listView.setElement(this.$('ul.values-list')).render();

            this.updateAll();
            this.updateLoading();
            this.updateError();
            this.updateEmpty();
            return this;
        },

        /**
         * If the view is visible and scrolled within the trigger offset, fetch more values. Can be called from outside
         * if an external change may have affected this condition.
         */
        checkScroll: function () {
            _.defer(function () {
                if (this.$el.is(':visible') && this.el.scrollHeight - this.el.scrollTop - this.el.offsetHeight < INFINITE_SCROLL_OFFSET) {
                    // The Paginator will not fetch if a request is already in flight so we don't need to check that here
                    this.paginator.fetchNext();
                }
            }.bind(this));
        },

        updateAll: function () {
            // throttle: when 'all' is toggled, it toggles lots of checkboxes, each of which
            // triggers this handler
            _.throttle(function () {
                if (this.$all) {
                    const numValues = this.paginator.valuesCollection.length;
                    if (numValues > 0) {
                        // will never be hidden again - values shouldn't disappear
                        this.$all.toggleClass('hide', false);
                    }

                    const numSelected = this.paginator.valuesCollection.countBy(function (model) {
                        return model.get('selected');
                    }).true || 0;
                    const determinate = numSelected === 0 || numSelected === numValues;
                    this.$all.find('input').iCheck(numSelected === 0 ? 'uncheck' : 'check');
                    this.$all.find('input').iCheck(determinate ? 'determinate' : 'indeterminate');
                }
            }.bind(this), 100)();
        },

        updateLoading: function () {
            if (this.$loading) {
                const loading = this.paginator.stateModel.get('loading');
                this.$loading.toggleClass('hide', !loading);
            }
        },

        updateEmpty: function () {
            if (this.$empty) {
                const empty = this.paginator.stateModel.get('empty');
                this.$empty.toggleClass('hide', !empty);
            }
        },

        updateError: function () {
            if (this.$error) {
                const error = this.paginator.stateModel.get('error');
                this.$error.toggleClass('hide', !error);

                if (error) {
                    this.$error.html(generateErrorHtml({
                        errorDetails: error.message,
                        errorUUID: error.uuid,
                        errorLookup: error.backendErrorCode,
                        isUserError: error.isUserError
                    }));
                }
            }
        }
    });
});
