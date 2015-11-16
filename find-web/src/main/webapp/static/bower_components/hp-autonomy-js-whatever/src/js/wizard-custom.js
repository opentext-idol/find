/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module js-whatever/js/wizard-custom
 */
define([
    '../../../backbone/backbone',
    'underscore',
    'text!js-whatever/templates/wizard/wizard.html',
    'jquery-steps'
], function(Backbone, _, template) {
    /**
     * @typedef WizardStrings
     * @property {string} last String displayed on the wizard's finish button
     * @property {string} next String displayed on the wizard's next button
     * @property {string} prev String displayed on the wizard's previous button
     */
    /**
     * @typedef WizardStep
     * @property {string} class The CSS class applied to the step
     * @property {boolean} [active] Denotes the active step. Should be set to true for the first step of the wizard
     * @property {Backbone.View} constructor Constructor function for the step's view
     * @property {object} [options] Options passed to the constructor
     */
    /**
     * @typedef WizardOptions
     * @property {function} [template] Overrides the default template
     * @property {string} [columnClass=span12] Overrides the default columnClass
     * @property {WizardStrings} strings Strings for the wizard
     * @property {Array<WizardStep>} steps Steps for the wizard
     * @property {object} [renderOptions] Additional options passed to the template
     */
    /**
     * @name module:js-whatever/js/wizard-custom.WizardCustom
     * @desc Backbone wrapper around the jQuery steps plugin
     * @constructor
     * @param {WizardOptions} options
     * @extends Backbone.View
     * @see http://www.jquery-steps.com/
     */
    return Backbone.View.extend(/** @lends module:js-whatever/js/wizard-custom.WizardCustom.prototype */{

        /**
         * @desc Template function for the wizard
         */
        template: _.template(template),

        /**
         * @desc Bootstrap grid class
         * @default span12
         */
        columnClass: 'span12',

        initialize: function(options){
            _.bindAll(this, 'handleStepChange');

            if (options.template) {
                this.template = options.template;
            }

            if(options.columnClass) {
                this.columnClass = options.columnClass;
            }

            this.strings = options.strings;
            this.steps = options.steps;
            this.renderOptions = options.renderOptions;

            this.wizardOptions = _.defaults(options.wizardOptions || {}, {
                onStepChanging: this.handleStepChange,
                labels: {
                    finish: this.strings.last,
                    next: this.strings.next,
                    previous: this.strings.prev
                }
            });
        },

        /**
         * @desc Renders the wizard. Rendering of the steps is deferred until they are needed
         */
        render: function(){
            this.$el.html(this.template({
                renderOptions: this.renderOptions,
                steps: this.steps,
                strings: this.strings
            }));

            this.$wizard = this.$('.wizard');

            this.$wizard.steps(this.wizardOptions);

            this.$wizard.find('.actions').insertAfter(this.$wizard.find('.steps'));

            this.$wizard.find('.steps').addClass(this.columnClass);
            this.$wizard.find('.actions').addClass(this.columnClass);
            this.$wizard.find('.content').addClass(this.columnClass);

            _.each(this.steps, function(step){
                var options = step.options || {};

                step.view = new step.constructor(_.defaults({
                    // class is a reserved word
                    el: this.$('.'+ step['class'])
                }, options));
            }, this);
        },

        /**
         * @desc Gets the step at the given index
         * @param {Number} index
         * @returns {WizardStep} The step at the index
         */
        getStep: function(index) {
            return this.steps[index];
        },

        /**
         * @desc Gets the currently selected step
         * @returns {WizardStep} The currently selected step
         */
        getCurrentStep: function() {
            return this.getStep(this.$wizard.steps('getCurrentIndex'));
        },

        /**
         * Renders the view associated with the active step
         */
        renderActiveStep: function(){
            this.getStepByAttribute({active: true}).view.render();
        },

        /**
         * Finds the first step with the given attributes
         * @param {object} attributeHash
         * @returns {WizardStep} The step with the given attributes
         */
        getStepByAttribute: function(attributeHash){
            return _.findWhere(this.steps, attributeHash);
        },

        /**
         * @desc Function called before changing to a step. If you want to override this function you should probably
         * call this as the first line of your function
         * @param {Event} e jQuery event object
         * @param {number} currentIndex The current tab index
         * @param {number} newIndex The newly selected tab index
         * @param {object} renderOptions Parameters passed to the view's render method
         */
        handleStepChange: function(e, currentIndex, newIndex, renderOptions) {
            var newStep = this.getStep(newIndex);

            if(newStep.view.$el.children().length === 0){
                newStep.view.render(renderOptions);
            }
        }
    });
});