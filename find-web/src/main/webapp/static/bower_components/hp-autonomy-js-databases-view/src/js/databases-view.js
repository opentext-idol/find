/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module databases-view/js/databases-view
 */
define([
    'backbone',
    'jquery',
    'underscore',
    'js-whatever/js/list-view',
    'js-whatever/js/filtering-collection',
    'js-whatever/js/escape-hod-identifier'
], function(Backbone, $, _, ListView, FilteringCollection, escapeHodIdentifier) //noinspection JSClosureCompilerSyntax
{

    var filteredIndexesCollection = function(filter, databasesCollection) {
        return new FilteringCollection([], {
            collection: databasesCollection,
            modelFilter: filter
        });
    };

    var setParent = function(category) {
        _.each(category.children, function(child) {
            child.parent = category;
            setParent(child);
        });
    };

    var processCategories = function(categories, collection, currentSelection) {
        return _.chain(categories)
            // find all the categories who have a child in the databases collection
            .filter(function(child) {
                return collection.filter(child.filter).length > 0;
            })
            // add the correct collapse property to each child
            .map(function(child) {
                child = _.clone(child);

                var childHasSelection = _.chain(currentSelection)
                    // for every item in the current selection find the corresponding database in the database collection
                    .map(function(selection) {
                        return collection.findWhere(selection);
                    }, this)
                    // throw out any that don't have a database
                    .compact()
                    // find a selected database that is in the current category
                    .find(child.filter)
                    .value();

                // if the category has a selected database, don't collapse it
                // if we're not forcing selection and the selection is empty (i.e. everything is implicitly selected), don't collapse it
                child.collapse = !(childHasSelection || (!this.forceSelection && _.isEmpty(currentSelection)));

                return child;
            })
            .value();
    };

    /**
     * @typedef module:databases-view/js/databases-view.DatabasesListItemViewOptions
     * @desc As ListItemViewOptions but specifies some default values
     * @extends module:js-whatever/js/list-item-view.ListItemView~ListItemViewOptions
     * @property {string} [tagName=li] The tag name for each database/category
     * @property {callback} [template=this.databaseTemplate] The template to use for a database
     */
    /**
     * @typedef module:databases-view/js/databases-view.DatabasesListViewOptions
     * @desc As ListViewOptions but specifies some default values
     * @extends module:js-whatever/js/list-view.ListView~ListViewOptions
     * @property {string} [classname=list-unstyled] The classname to apply to the list items
     * @property {string} [tagName=ul] The tag name for the top level of the view
     * @property {module:databases-view/js/databases-view.DatabasesListItemViewOptions} itemOptions The options to use for the list items
     */
    /**
     * @typedef ResourceIdentifier
     * @property {string} name The name of the resource
     * @property {string} domain The domain of the resource
     */
    /**
     * Function that describes the databases that a category contains
     * @callback module:databases-view/js/databases-view.DatabasesView~CategoryFilter
     * @param {Backbone.Model} model The database model
     * @return {boolean} True if the database is in the category; false otherwise
     */
    /**
     * @typedef module:databases-view/js/databases-view.DatabasesView~Category
     * @property {module:databases-view/js/databases-view.DatabasesView~CategoryFilter} filter Filter describing the databases contained in the category
     * @property {string} name The name of the category
     * @property {string} displayName A different name which may be more suitable for display
     * @property {string} className CSS classes to apply to the category when it is rendered
     */
    /**
     * @typedef module:databases-view/js/databases-view.DatabasesView~DatabasesViewOptions
     * @property {module:databases-view/js/databases-collection.DatabasesCollection} databasesCollection The resources that the view will display
     * @property {module:databases-view/js/databases-collection.DatabasesCollection} selectedDatabasesCollection The currently selected resources
     * @property {string} topLevelDisplayName The display name of the top level category
     * @property {boolean} [forceSelection=false] True if at least one item must always be selected; false otherwise
     * @property {string} [emptyMessage=''] Message to display if there are no databases
     * @property {Array<module:databases-view/js/databases-view.DatabasesView~Category>} [childCategories] The categories the databases will be placed in. If undefined all the databases will be in a single category
     * @property {module:databases-view/js/databases-view.DatabasesListViewOptions} [listViewOptions] Options used to create the list views
     */
    /**
     * @name module:databases-view/js/databases-view.DatabasesView
     * @desc View for showing and selecting HP Haven OnDemand resources.  This is an abstract class and must be supplied
     * with templates for databases and categories. In addition, six primitive operations must be implemented:
     * <ul>
     *     <li> check
     *     <li> uncheck
     *     <li> enable
     *     <li> disable
     *     <li> determinate
     *     <li> indeterminate
     * </ul>
     * The selected databases collection will be kept up to date with the state of the UI, and changes made to the collection
     * will be reflected in the view.
     * @param {module:databases-view/js/databases-view.DatabasesView~DatabasesViewOptions} options The options for the view
     * @constructor
     * @abstract
     * @extends Backbone.View
     */
    //noinspection JSClosureCompilerSyntax
    return Backbone.View.extend(/**@lends module:databases-view/js/databases-view.DatabasesView.prototype */{
        /**
         * @desc Template for the view. If overriding it should have an element with class databases-list. For empty
         * message support add an element with class no-active databases
         * @method
         */
        template: _.template('<div class="no-active-databases"></div><div class="databases-list"></div>'),

        /**
         * @desc Template for an individual database. The element which will respond to user interaction must have
         * class database-input. The name of the database must be in a data-name attribute. The name of the domain must
         * be in a data-domain attribute
         * @abstract
         * @method
         */
        databaseTemplate: $.noop,

        /**
         * @desc Template for a category. The element which will respond to user interaction must have
         * class database-input. The name of the category must be in a data-category-id attribute. There must be an
         * element with class child-categories if child categories are used.
         * @abstract
         * @method
         */
        categoryTemplate: $.noop,


        /**
         * @desc Perform any initialization required for the database and category inputs to become functional
         * @abstract
         * @method
         */
        initializeInputs: $.noop,

        /**
         * @desc Marks the given input as selected
         * @param {jQuery} input The input to mark
         * @abstract
         * @method
         */
        check: $.noop,

        /**
         * @desc Marks the given input as deselected
         * @param {jQuery} input The input to mark
         * @abstract
         * @method
         */
        uncheck: $.noop,

        /**
         * @desc Marks the given input as enabled
         * @param {jQuery} input The input to mark
         * @abstract
         * @method
         */
        enable: $.noop,

        /**
         * @desc Marks the given input as disabled
         * @param {jQuery} input The input to mark
         * @abstract
         * @method
         */
        disable: $.noop,

        /**
         * @desc Marks the given input as determinate
         * @param {jQuery} input The input to mark
         * @abstract
         * @method
         */
        determinate: $.noop,

        /**
         * @desc Marks the given input as indeterminate
         * @param {jQuery} input The input to mark
         * @abstract
         * @method
         */
        indeterminate: $.noop,

        initialize: function(options) {
            this.collection = options.databasesCollection;
            this.selectedDatabasesCollection = options.selectedDatabasesCollection;
            this.forceSelection = options.forceSelection || false;
            this.emptyMessage = options.emptyMessage || '';

            this.listViewOptions = options.listViewOptions || {};
            this.listViewOptions.itemOptions = this.listViewOptions.itemOptions || {};

            _.defaults(this.listViewOptions.itemOptions, {
                tagName: 'li',
                template: this.databaseTemplate
            });

            _.defaults(this.listViewOptions, {
                className: 'list-unstyled',
                tagName: 'ul'
            });

            if (!this.forceSelection && this.selectedDatabasesCollection.length === this.collection.length) {
                this.currentSelection = [];
            } else {
                this.currentSelection = this.selectedDatabasesCollection.toResourceIdentifiers();
            }

            if (options.childCategories) {
                var children = processCategories(options.childCategories, this.collection, this.currentSelection);

                this.hierarchy = {
                    name: 'all',
                    displayName: options.topLevelDisplayName,
                    className: 'list-unstyled',
                    collapse: false,
                    children: children
                };
            } else {
                this.hierarchy = {
                    name: 'all',
                    displayName: options.topLevelDisplayName,
                    className: 'list-unstyled'
                };
            }

            setParent(this.hierarchy);

            // if node.children, call for each child
            // else if node has a filter, set up filtering collection and list view
            // else set up list view
            var buildHierarchy = _.bind(function(node, collection) {
                if (node.children) {
                    _.each(node.children, function(child) {
                        buildHierarchy(child, collection);
                    });
                } else {
                    if (node.filter) {
                        node.children = filteredIndexesCollection(node.filter, collection);
                    } else {
                        node.children = collection;
                    }

                    node.listView = new ListView(_.extend({
                        collection: node.children
                    }, this.listViewOptions));
                }
            }, this);

            // start at this hierarchy
            buildHierarchy(this.hierarchy, this.collection);

            this.listenTo(this.collection, 'add remove reset', this.updateEmptyMessage);

            this.listenTo(this.collection, 'remove', function(model) {
                var selectedIndex = _.findWhere(this.currentSelection, model.pick('domain', 'name'));

                if (selectedIndex) {
                    this.currentSelection = _.without(this.currentSelection, selectedIndex);
                    this.updateCheckedOptions();
                    this.updateSelectedDatabases();
                }
            });

            this.listenTo(this.collection, 'reset', function(collection) {
                if (!_.isEmpty(this.currentSelection)) {
                    var newItems = collection.toResourceIdentifiers();

                    var newSelection = _.filter(this.currentSelection, function(selectedItem) {
                        return _.findWhere(newItems, selectedItem);
                    });

                    if (!_.isEqual(newSelection, this.currentSelection)) {
                        this.currentSelection = newSelection;
                        this.updateSelectedDatabases();
                    }

                    this.updateCheckedOptions();
                }
                else {
                    // empty selection is everything, which may now be different
                    this.updateSelectedDatabases();
                }
            });

            this.listenTo(this.selectedDatabasesCollection, 'update reset', function() {
                // Empty current selection means all selected; if we still have everything selected then there is no work to do
                if (!(_.isEmpty(this.currentSelection) && this.selectedDatabasesCollection.length === this.collection.length)) {
                    this.currentSelection = this.selectedDatabasesCollection.toResourceIdentifiers();
                    this.updateCheckedOptions();
                }
            });

            if(options.childCategories) {
                // if the databases change, we need to recalculate category collapsing and visibility
                this.listenTo(this.collection, 'reset update', function() {
                    var removeListViews = function(node) {
                        if (node.listView) {
                            node.listView.remove();
                        }

                        if (_.isArray(node.children)) {
                            _.each(node.children, function(child) {
                                removeListViews(child);
                            });
                        }
                    };

                    removeListViews(this.hierarchy);

                    this.hierarchy.children = processCategories(options.childCategories, this.collection, this.currentSelection);

                    buildHierarchy(this.hierarchy, this.collection);

                    this.render();

                    this.updateSelectedDatabases();
                });
            }
        },

        /**
         * @desc Renders the view
         * @returns {module:databases-view/js/databases-view.DatabasesView} this
         */
        render: function() {
            this.$el.html(this.template(this.getTemplateOptions()));

            var renderNode = _.bind(function(node, $parentDomElement) {
                var $nodeEl = $(this.categoryTemplate({
                    data: {node: node}
                }));

                var $child = $nodeEl.find('.child-categories');

                $child.addClass('collapse');

                if (node.collapse) {
                    $nodeEl.find('[data-target]').addClass('collapsed');
                } else {
                    $child.addClass('in');
                }

                if (node.listView) {
                    node.listView.render();

                    $child.append(node.listView.$el);
                }
                else {
                    _.each(node.children, function(child) {
                        renderNode(child, $child);
                    });
                }

                $parentDomElement.append($nodeEl);
            }, this);

            this.$databasesList = this.$('.databases-list');

            renderNode(this.hierarchy, this.$databasesList);

            this.$databaseCheckboxes = this.$('.database-input');
            this.$categoryCheckboxes = this.$('.category-input');

            this.$emptyMessage = this.$('.no-active-databases');
            this.$emptyMessage.text(this.emptyMessage);

            this.initializeInputs();
            this.updateCheckedOptions();
            this.updateEmptyMessage();
            return this;
        },

        /**
         * Returns any parameters required for use in the template. This allows custom templates to take custom parameters
         * @returns {object} The parameters
         */
        getTemplateOptions: function() {
            return {};
        },

        /**
         * @desc Selects the database with the given name and domain
         * @param {string} database The name of the database
         * @param {string} domain The domain of the database
         * @param {boolean} checked The new state of the database
         */
        selectDatabase: function(database, domain, checked) {
            if (checked) {
                this.currentSelection.push({
                    domain: domain,
                    name: database
                });

                this.currentSelection = _.uniq(this.currentSelection, function (item) {
                    // uniq uses reference equality on the transform
                    return escapeHodIdentifier(item.domain) + ':' + escapeHodIdentifier(item.name);
                });
            } else {
                this.currentSelection = _.reject(this.currentSelection, function (selectedItem) {
                    return selectedItem.name === database && selectedItem.domain === domain;
                });
            }

            this.updateCheckedOptions();
            this.updateSelectedDatabases();
        },

        /**
         * @desc Selects the category with the given name
         * @param {string} category
         * @param {boolean} checked The new state of the category
         */
        selectCategory: function(category, checked) {
            var findNode = function(node, name) {
                if (node.name === name) {
                    return node;
                }
                else if (_.isArray(node.children)) {
                    return _.find(node.children, function(child) {
                        return findNode(child, name);
                    });
                }
            };

            var findDatabases = function(node) {
                if (_.isArray(node.children)) {
                    return _.chain(node.children)
                        .map(function(child) {
                            return findDatabases(child);
                        })
                        .flatten()
                        .value();
                }
                else {
                    return node.children.map(function(child) {
                        return child.pick('domain', 'name');
                    });
                }
            };

            var databases = findDatabases(findNode(this.hierarchy, category));

            if (checked) {
                this.currentSelection = _.chain([this.currentSelection, databases]).flatten().uniq(function (item) {
                    return escapeHodIdentifier(item.domain) + ':' + escapeHodIdentifier(item.name);
                }).value();
            } else {
                this.currentSelection = _.reject(this.currentSelection, function (selectedItem) {
                    return _.findWhere(databases, selectedItem);
                });
            }

            this.updateCheckedOptions();
            this.updateSelectedDatabases();
        },

        /**
         * @desc Updates the selected databases collection with the state of the UI
         */
        updateSelectedDatabases: function() {
            this.selectedDatabasesCollection.set(_.isEmpty(this.currentSelection) ? this.collection.toResourceIdentifiers() : this.currentSelection);
        },

        /**
         * @desc Updates the view to match the current internal state. There should be no need to call this method
         * @private
         */
        updateCheckedOptions: function() {
            _.each(this.$databaseCheckboxes.add(this.$categoryCheckboxes), function(checkbox) {
                var $checkbox = $(checkbox);
                this.uncheck($checkbox);
                this.enable($checkbox);
                this.determinate($checkbox);
            }, this);

            _.each(this.$databaseCheckboxes, function (checkbox) {
                var $checkbox = $(checkbox);

                if (_.findWhere(this.currentSelection, {name: $checkbox.attr('data-name'), domain: $checkbox.attr('data-domain')})) {
                    this.check($checkbox);

                    if (this.forceSelection && this.currentSelection.length === 1) {
                        this.disable($checkbox);
                    }
                }
            }, this);

            this.updateCategoryCheckbox(this.hierarchy);
        },

        /**
         * Updates a category checkbox to match the current internal state. There should be no need to call this method
         * @param {module:databases-view/js/databases-view.DatabasesView~Category} node The category to update
         * @returns {Array<ResourceIdentifier>} The resource identifier in the category and its descendants
         * @private
         */
        updateCategoryCheckbox: function(node) {
            var $categoryCheckbox = this.$('[data-category-id="' + node.name + '"]');
            var childIndexes;

            if (_.isArray(node.children)) {
                // traverse each of the child categories recursively
                // the flatMap gives a list of all the indexes in this category and its descendants
                childIndexes = _.chain(node.children)
                    .map(function(child) {
                        return this.updateCategoryCheckbox(child);
                    }, this)
                    .flatten()
                    .value();
            }
            else {
                // the indexes are the ones in the collection for this category
                childIndexes = node.children.map(function(child) {
                    return child.pick('domain', 'name');
                });
            }

            // checkedState is an array containing true if there are checked boxes, and false if there are unchecked boxes
            var checkedState = _.chain(childIndexes)
                .map(function(childIndex) {
                    return Boolean(_.findWhere(this.currentSelection, childIndex));
                }, this)
                .uniq()
                .value();

            var checkedBoxes = _.contains(checkedState, true);
            var unCheckedBoxes = _.contains(checkedState, false);

            if (checkedBoxes && unCheckedBoxes) {
                this.indeterminate($categoryCheckbox);
            }
            else if (checkedBoxes) {
                // all the category's children are checked
                this.check($categoryCheckbox);

                // if this category's children comprise the entire selection, it should be disabled
                // otherwise clicking it would leave an empty database selection
                if (this.forceSelection && this.currentSelection.length === childIndexes.length) {
                    this.disable($categoryCheckbox);
                }
            }
            else {
                this.uncheck($categoryCheckbox);
            }

            // return the list of child categories for use in the flatMap of the ancestor category
            return childIndexes;
        },

        /**
         * @desc Updates the no databases message to match the current internal state. There should be no need to call this method
         * @private
         */
        updateEmptyMessage: function() {
            if (this.$emptyMessage && this.$databasesList) {
                this.$emptyMessage.toggleClass('hide', !this.collection.isEmpty());
                this.$databasesList.toggleClass('hide', this.collection.isEmpty());
            }
        }
    });

});
