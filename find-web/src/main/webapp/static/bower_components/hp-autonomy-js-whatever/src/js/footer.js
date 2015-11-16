/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module js-whatever/js/footer
 */
define([
    '../../../backbone/backbone',
    'store',
    'text!js-whatever/templates/footer/footer.html',
    'text!js-whatever/templates/footer/footer-tab.html',
    'text!js-whatever/templates/footer/footer-tab-view.html'
], function(Backbone, store, footerTemplate, tabTemplate, viewTemplate) {

    /**
     * @typedef FooterStrings
     * @property {string} [clickToHide='Collapse footer.']
     * @property {string} [clickToShow='Show more...']
     */
    /**
     * @typedef FooterTabData
     * @desc Describes a footer tab. In addition to the properties listed below, it requires a Backbone View for
     * every component in the list of processors
     * @property {string} key Identifier for the tab
     */
    /**
     * @typedef FooterOptions
     * @property {jQuery} $parent Parent element.  The show-footer class to this element when the footer is shown
     * @property {object} vent Instance of vent-constructor
     * @property {FooterStrings} strings Strings to use as tooltip labels
     * @property {Array<FooterTabData>} tabData Defines the tabs in the footer
     */
    /**
     * @name module:js-whatever/js/footer.Footer
     * @desc View representing a page footer which can have multiple tabs and be minimised. The state of the footer is
     * stored in local storage
     * @param {FooterOptions} options
     * @constructor
     * @extends Backbone.View
     */
    return Backbone.View.extend(/** @lends module:js-whatever/js/footer.Footer.prototype */{
        /**
         * @desc Classes added to footer element
         * @type {string}
         */
        className: 'tabbable page-footer',

        /**
         * @desc Backbone events hash
         */
        events: {
            'click .toggle-footer': 'toggle',
            'click .footer-tab': 'handleTabClick'
        },

        /**
         * @desc Local storage key for if the footer is expanded
         * @type {string}
         */
        expanded: 'footer.expanded',

        /**
         * @desc Local storage key for the current tab index
         * @type {string}
         */
        index: 'footer.tab-index',

        /**
         * @typedef FooterProcessor
         * @property {string} component The key in tabData that the processor reads
         * @property {string} selector CSS selector for the element of the footer template that the processor renders to
         * @property {function} template Base template for processor
         * @property {string} target CSS selector for element within the selected element which will be given the class
         * 'active'
         */
        /**
         * @desc Defines the components of the footer
         * @type {Array<FooterProcessor>}
         */
        processors: [
            { selector: '.footer-tabs', component: 'tab', template: _.template(tabTemplate, null, {variable: 'ctx'}), target: 'li'},
            { selector: '.tab-content', component: 'view', template: _.template(viewTemplate, null, {variable: 'ctx'}), target: '.tab-pane' }
        ],

        initialize: function(options) {
            this.$parent = options.$parent;
            this.tabData = options.tabData;
            this.vent = options.vent;

            this.strings = options.strings || {
                clickToHide: 'Collapse footer.',
                clickToShow: 'Show more...'
            };
        },

        /**
         * @desc Renders the footer according to the defined processors and tabData
         */
        render: function() {
            this.$el.html(footerTemplate);

            var selectedIndex = store.get(this.index) || 0;
            this.updateForState(store.get(this.expanded) || false);

            _.each(this.processors, function(processor) {
                var $container = this.$(processor.selector);

                _.each(this.tabData, function(item) {
                    var $wrapper = $($.parseHTML(processor.template({ key: item.key })));
                    var component = item[processor.component];

                    component.render();

                    if ($wrapper.hasClass('context')) {
                        $wrapper.html(component.el);
                    } else {
                        $wrapper.find('.context').html(component.el);
                    }

                    $container.append($wrapper);
                }, this);

                $container.find(processor.target).eq(selectedIndex).addClass('active');
            }, this);
        },

        /**
         * @desc Select a footer tab by index
         * @param {Number} index The index of the tab in tabData
         */
        selectIndex: function(index) {
            store.set(this.index, index);

            _.each(this.processors, function(processor) {
                this.$(processor.target).removeClass('active').eq(index).addClass('active');
            }, this);

            this.updateForState(true);
        },

        /**
         * @desc Event handler called when a tab is clicked
         * @param {object} e jQuery event
         */
        handleTabClick: function(e) {
            var $tab = $(e.currentTarget).parent();

            store.set(this.index, $tab.index());

            if ($tab.hasClass('active') && this.$parent.hasClass('show-footer')) {
                this.hide();
            } else {
                this.show();
            }
        },

        /**
         * @desc Hides the footer
         */
        hide: function() {
            this.updateForState(false);
        },

        /**
         * @desc Shows the footer
         */
        show: function() {
            this.updateForState(true);
        },

        /**
         * @desc Toggles the footer
         */
        toggle: function() {
            this.updateForState(!this.$parent.hasClass('show-footer'));
        },

        /**
         * @desc Shows or hides the footer.
         * @param {boolean} state True to show the footer, false to hide the footer
         */
        updateForState: function(state) {
            store.set(this.expanded, state);
            this.$parent.toggleClass('show-footer', state);

            this.$('.toggle-footer')
                .tooltip('destroy')
                .tooltip({
                    container: 'body',
                    html: true,
                    title: state ? this.strings.clickToHide : this.strings.clickToShow
                });

            this.vent.fireResize();
        }
    });

});