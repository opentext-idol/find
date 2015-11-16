/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module js-whatever/js/checkbox-modal
 */
define([
    '../../../backbone/backbone',
    'js-whatever/js/ensure-array',
    'text!js-whatever/templates/checkbox-modal/checkbox-modal.html',
    'text!js-whatever/templates/checkbox-modal/checkbox-table.html'
], function(Backbone, ensureArray, checkboxModal, checkboxTable) {
    /**
     * @typedef CheckboxModalTable
     * @property {boolean|string[]} initialState The initial state of the checkboxes. Can be either a boolean, or a list of
     * row names which should start checked
     * @property {string} tableHeader The i18n key for the table header.
     * @property {string} [inputName] The name of the radio input. Only used if checkboxOrRadio is set to radio
     * @property {string[]} rows The names of the rows.  These must be unique
     */
    /**
     * @typedef CheckboxModalOptions
     * @property {String} [templateModal] The template for the modal
     * @property {String} [templateTableModal] The template for the table.
     * @property {Array.<CheckboxModalTable>} tables The configuration for the tables
     * @property {object} [parameters=[]] Additional parameters passed to templateModal
     * @property {function} [okCallback=$.noop] Callback called when the OK button is pressed
     * @property {string} [okIcon='icon-refresh'] Icon for the OK button
     * @property {string} [okClass='btn-success'] Class for the OK button
     * @property {string} [cancelClass=''] Class for the cancel button
     * @property {boolean} [atLeastOneElementSelected=false] Set this to true if a checkbox
     * @property {object} i18n Object containing i18n strings
     * @property {module:js-whatever/js/vent-constructor.Vent} vent Instance of Vent used to track resize events
     * @property {string} [checkboxOrRadio=checkbox] Should be set to 'checkbox' or 'radio' according to the inputs
     * used in your template
     */
    /**
     * @name module:js-whatever/js/checkbox-modal.CheckboxModal
     * @desc Bootstrap modal which displays tables containing checkboxes or radio buttons, which can be toggled individually or
     * through a global checkbox
     * @constructor
     * @param {CheckboxModalOptions} init
     * @extends Backbone.View
     * @example
     * new CheckboxModal({
     *     i18n: i18n,
     *     vent: vent,
     *     parameters: {
     *         modalHeader: 'modalHeader'
     *     },
     *     tables: [{
     *         initialState: true,
     *         tableHeader: 'tableTitle',
     *         rows: [
     *             'one',
     *             'two',
     *             'three'
     *         ]
     *     }]
     * });
     */
    return Backbone.View.extend(/** @lends module:js-whatever/js/checkbox-modal.CheckboxModal.prototype */{

        initialize: function(init) {
            _.bindAll(this, 'render', 'setRows', 'setRow', 'getSelectedRows', 'remove', 'resizeModal',
                'getModalTemplateParams', 'getElementValue', 'restorePrevState', 'setGlobalCheckboxes');

            this.templateModal = _.template(init.templateModal || checkboxModal);
            this.templateTableModal = _.template(init.templateTableModal || checkboxTable);
            this.tables = init.tables;
            this.parameters = ensureArray(init.parameters);
            this.okCallback = init.okCallback || $.noop;
            this.okIcon = init.okIcon || 'icon-refresh';
            this.okClass = init.okClass || 'btn-success';
            this.cancelClass = init.cancelClass || '';
            this.atLeastOneElementSelected = init.atLeastOneElementSelected || false;

            this.i18n = init.i18n;
            this.vent = init.vent;

            this.checkboxOrRadio = init.checkboxOrRadio || 'checkbox';

            if (this.checkboxOrRadio === 'radio') {
                this.atLeastOneElementSelected = true;
            }

            this.setTableInitialState();

            this.storePrevConfig = true;
            this.render();
            this.listenTo(this.vent, 'vent:resize', this.resizeModal);
        },

        /**
         * @desc Called when the vent fires a vent:resize event. Sets the max-height of the modal body to 80% of the window
         * height, less the height of the modal-header and modal-footer, clamped to [1, 400].
         * @protected
         */
        resizeModal: function() {
            var header = this.editModal.find('.modal-header').outerHeight(true);
            var footer = this.editModal.find('.modal-footer').outerHeight(true);

            this.editModal.find('.modal-body').css('max-height',
                Math.min(400, Math.max(1, 0.8 * $(window).height() - header - footer))
            );
        },

        /**
         * @desc Initialises the internal state of the tables from the configuration
         * @protected
         */
        setTableInitialState: function() {
            _.each(this.tables, function(table) {
                table.initialState = table.initialState || false;

                table.columnStates = _.reduce(table.rows, function(memo, row) {
                    if (_.isArray(table.initialState)) {
                        memo[row] = _.contains(table.initialState, row);
                    } else {
                        memo[row] = table.initialState;
                    }

                    return memo;
                }, {});

                table.prevConfig = '';
            });
        },

        /**
         * @desc Sets the internal state of a row
         * @param {number} index The index of the table
         * @param {string} name The name of the row
         * @param {boolean} value The new value of the checkbox
         * @protected
         */
        setRow: function(index, name, value) {
            this.tables[index].columnStates[name] = value;
        },

        /**
         * @desc Sets the internal state of all rows for the given table
         * @param {number} index The index of the table
         * @param {boolean} value The new value of the checkbox
         * @protected
         */
        setRows: function(index, value) {
            _.each(this.tables[index].columnStates, function(oldValue, field) {
                this.setRow(index, field, value);
            }, this);
        },

        /**
         * @desc Clear the internal state of all rows for all tables
         * @protected
         */
        clearAllRows: function() {
            _.each(this.tables, function(table, index) {
                this.setRows(index, false);
            }, this);
        },

        /**
         * @desc Set the checked property of the nth input in the given table
         * @param {number} index The index of the table
         * @param {number} n The index of the checkbox in the table.  This includes the global checkbox and other inputs.
         * @param {boolean} value The new value of the checkbox
         * @deprecated Prefer setCheckbox
         */
        setNthCheckbox: function(index, n, value) {
            this.editModal.find('table:eq(' + index + ') input:eq(' + n + ')').prop('checked', value);
        },

        /**
         * @desc Set the checked property of the named checkbox in the given table
         * @param {number} index The index of the table
         * @param {string} rowName The name of the row
         * @param {boolean} value The new value of the checkbox
         */
        setCheckbox: function(index, rowName, value) {
            this.editModal.find('table:eq(' + index + ') input[data-row-name="' + rowName + '"]').prop('checked', value);
        },

        /**
         * @desc Set the checked property of all checkboxes in the given table
         * @param {number} index The index of the table
         * @param {boolean} value The new value of the checkbox
         */
        setCheckboxes: function(index, value) {
            this.editModal.find('table:eq(' + index + ') input').prop('checked', value);
        },

        /**
         * @desc clear all checkboxes in all tables
         */
        clearAllCheckboxes: function() {
            this.editModal.find('th input[type="' + this.checkboxOrRadio + '"]').prop('indeterminate', false);
            this.editModal.find('input[type="' + this.checkboxOrRadio + '"]').prop('checked', false);
        },

        /**
         * @desc Sets the value of the indeterminate property of the checkbox with the given index
         * @param {number} index The index of the checkbox
         * @param {boolean} value The value of the indeterminate property
         */
        setIndeterminateCheckbox: function(index, value) {
            this.editModal.find('table:eq(' + index + ') input[data-row-name=""]').prop('indeterminate', value);
        },

        /**
         * @desc Sets the internal state and the checkboxes for a given table
         * @param {number} index The index of the table
         * @param {boolean} valueCheckbox The new value of the checkbox
         * @param {boolean} valueGlobalCheckbox The new indeterminate state of the global checkbox
         */
        setCheckboxesAndRows: function(index, valueCheckbox, valueGlobalCheckbox) {
            this.setCheckboxes(index, valueCheckbox);
            this.setRows(index, valueCheckbox);
            if (valueGlobalCheckbox !== undefined) {
                this.setIndeterminateCheckbox(index, valueGlobalCheckbox);
            }
        },

        /**
         * @desc sets the value of the global checkbox in each table in accordance with the values of the checkboxes in
         * the table
         */
        setGlobalCheckboxes: function() {
            _.each(this.editModal.find('table'), function(table, index) {
                var tempCheck = '';

                _.each($(table).find('td input[type="' + this.checkboxOrRadio + '"]'), function(check) {
                    if (tempCheck === '') {
                        tempCheck = $(check).prop('checked');
                    }

                    if (tempCheck !== $(check).prop('checked')) {
                        tempCheck = 'indeterminate';
                    }
                });

                if (tempCheck !== 'indeterminate') {
                    this.setCheckbox(index, '', tempCheck);
                } else {
                    this.setIndeterminateCheckbox(index, true);
                }
            }, this);
        },

        /**
         * Get the selected rows for each table. Updates the internal state of the tables
         * @returns {string[]} The names of the selected rows
         */
        getSelectedRows: function() {
            return _.map(this.editModal.find('table'), function(table, index) {
                var checked = $(table).find('td input[type="' + this.checkboxOrRadio + '"]:checked');
                var notChecked = $(table).find('td input[type="' + this.checkboxOrRadio + '"]:not(:checked)');
                var globalCheckbox = $(table).find('input[data-row-name=""]');
                var innerCheckedRows = [];

                if (checked.length && (!globalCheckbox.length || globalCheckbox.prop('indeterminate'))) {
                    this.setRow(index, '', 'indeterminate');

                    _.each(notChecked, function(elem) {
                        this.setRow(index, $(elem).data('row-name'), false);
                    }, this);

                    innerCheckedRows = _.map(checked, function(elem) {
                        this.setRow(index, $(elem).data('row-name'), true);
                        return $(elem).data('row-name');
                    }, this);
                } else {
                    if (globalCheckbox.prop('checked')) {
                        innerCheckedRows = _.map(checked, function(elem) {
                            return $(elem).data('row-name');
                        }, this);

                        this.setRows(index, true);
                    } else {
                        this.setRows(index, false);
                    }
                }

                return innerCheckedRows;
            }, this);
        },

        /**
         * @typedef TableConfig
         * @property {string} inputName The name of the input if using radio buttons
         * @property {object} i18n strings
         * @property {string} i18nTableHeader A key in i18n which will be used as the table header
         * @property {object} rows A map from row names to booleans representing the checkbox state
         */
        /**
         * @typedef CheckboxModalTemplateParameters
         * @property {Array.<TableConfig>} tableConfigs The configuration for the tables
         * @property {function} tableTemplate The template for the table
         * @property {object} i18n i18n strings
         * @property {string} okIcon The icon for the OK butto
         * @property {string} okClass The class for the OK button
         * @property {string} cancelClass The class for the cancel button
         */
        /**
         * Gets the parameters which are passed to templateModal.  The tables configs are passed to each table.
         * @returns {CheckboxModalTemplateParameters} template parameters augemented with the additional parameters
         * passed to initialize
         * @protected
         */
        getModalTemplateParams: function() {
            var tableConfig = _.map(this.tables, function(table) {
                return {
                    inputName: table.inputName || '',
                    i18n: this.i18n,
                    i18nTableHeader: table.tableHeader,
                    rows: table.columnStates
                };
            }, this);

            var templateParams = {
                tableConfigs: tableConfig,
                tableTemplate: this.templateTableModal,
                i18n: this.i18n,
                okIcon: this.okIcon,
                okClass: this.okClass,
                cancelClass: this.cancelClass
            };

            _.each(this.parameters, function(entry) {
                _.each(entry, function(value, key) {
                    templateParams[key] = value;
                });
            });

            return templateParams;
        },

        /**
         * @desc Get the value of an input. Don't use this to get the value of checkboxes or you will have a bad time
         * @param {string} selector A css selector for locating the input
         * @returns {*} The value of the input
         */
        getElementValue: function(selector) {
            return this.editModal.find(selector).val();
        },

        /**
         * @desc Get the value of an input. Don't use this to get the value of checkboxes or you will have a bad time
         * @param {string} selector A css selector for locating the input
         * @param {*} value The new value of the input
         */
        setElementValue: function(selector, value) {
            this.editModal.find(selector).val(value);
        },

        /**
         * @desc Shows the modal
         */
        showModal: function() {
            this.editModal.modal('show');
            if (!this.storePrevConfig) {
                this.restorePrevState();
            }
        },

        /**
         * @desc Hides the modal
         */
        hideModal: function() {
            this.editModal.modal('hide');
        },

        /**
         * @desc Restores the previous state of the modal
         * @protected
         */
        restorePrevState: function() {
            _.each(this.tables, function(table) {
                _.each(table.prevConfig, function(state, element) {
                    if (state === 'indeterminate') {
                        this.editModal.find('input[data-row-name="' + element + '"]').prop('indeterminate', true);
                    } else {
                        this.editModal.find('input[data-row-name="' + element + '"]').prop('checked', state);
                    }
                }, this);
            }, this);
        },

        /**
         * @desc renders the modal and adds the event handlers
         * @returns this
         */
        render: function() {
            this.editModal = $(this.templateModal(this.getModalTemplateParams()));
            this.editModal.on('shown', this.resizeModal);
            this.setGlobalCheckboxes();

            this.editModal.find('table input[type="' + this.checkboxOrRadio + '"]').change(_.bind(this.handleCheckBoxChange, this));
            this.editModal.find('button.ok').click(_.bind(this.onOkClick, this));

            this.editModal.on('shown', function() {
                document.activeElement.blur();
            });

            return this;
        },

        /**
         * @desc Handler called when a checkbox is clicked. This will set the state of the corresponding global checkbox
         * accordingly
         * @param {object} e jQuery event object
         * @protected
         */
        handleCheckBoxChange: function(e) {
            var $target = $(e.target);
            var rowName = $target.data('row-name');
            var checkboxes = $target.closest('table').find('td input[type="' + this.checkboxOrRadio + '"]');
            var checked = $target.closest('table').find('td input[type="' + this.checkboxOrRadio + '"]:checked');
            var globalCheckbox = $target.closest('table').find('input[data-row-name=""]');

            if (this.storePrevConfig) {
                this.prevConfig = this.columnStates;
                this.storePrevConfig = false;
            }

            if (rowName === '') {
                if ($target.prop('checked') || this.atLeastOneElementSelected) {
                    checkboxes.prop('checked', true);
                    globalCheckbox.prop('indeterminate', false);

                    if (this.atLeastOneElementSelected) {
                        globalCheckbox.prop('checked', true);
                    }
                } else {
                    checkboxes.prop('checked', false);
                    globalCheckbox.prop('indeterminate', false);
                }
            } else {
                if ($target.prop('checked')) {
                    if (checkboxes.length === checked.length) {
                        globalCheckbox.prop('checked', true);
                        globalCheckbox.prop('indeterminate', false);
                    } else {
                        globalCheckbox.prop('indeterminate', true);
                    }
                } else {
                    if (checked.length === 0) {
                        if (this.atLeastOneElementSelected) {
                            $(e.delegateTarget).prop('checked', true);
                        } else {
                            globalCheckbox.prop('indeterminate', false);
                            globalCheckbox.prop('checked', false);
                        }
                    } else {
                        globalCheckbox.prop('indeterminate', true);
                    }
                }
            }
        },

        /**
         * @desc Called when the OK button is pressed.  This will call okCallback and close the modal
         * @protected
         */
        onOkClick: function() {
            this.okCallback(this.getSelectedRows(), this.editModal);
            this.storePrevConfig = true;

            _.each(this.tables, function(table) {
                table.prevConfig = table.columnStates;
            });

            this.hideModal();
        },

        /**
         * @desc removes the modal
         */
        remove: function() {
            this.editModal.remove();
            Backbone.View.prototype.remove.call(this);
        }
    });
});