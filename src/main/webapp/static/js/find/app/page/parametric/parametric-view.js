define([
    'backbone',
    'underscore',
    'js-whatever/js/list-view',
    'js-whatever/js/filtering-collection',
    'find/app/page/parametric/parametric-list-item-view',
    'fieldtext/js/field-text-parser',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/parametric/parametric-view.html',
    'text!find/templates/app/page/parametric/field-name.html'
], function(Backbone, _, ListView, FilteringCollection, ParametricListItemView, parser, i18n, template, fieldTemplate) {

    var DEBOUNCE_WAIT_MILLISECONDS = 500;

    return Backbone.View.extend({
        template: _.template(template),

        initialize: function(options) {
            this.collection = options.parametricCollection; //new ParametricCollection([], {singleRequest: true});

            this.fieldNamesListView = new ListView({
                className: 'unstyled',
                collection: this.collection,
                tagName: 'ul',
                ItemView: ParametricListItemView,
                proxyEvents: [
                    'changeFieldText'
                ],
                itemOptions: {
                    className: 'animated fadeIn',
                    template: _.template(fieldTemplate)
                }
            });

            this.collection.on('request', _.bind(function() {
                this.setProcessing();
            }, this));

            this.collection.on('sync', _.bind(function() {
                this.setDone();
            }, this));

            this.listenTo(this.fieldNamesListView, 'item:changeFieldText', _.debounce(this.changeFieldText, DEBOUNCE_WAIT_MILLISECONDS));
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n
            }));

            this.$fieldNamesListView = this.$('.search-parametric-wrapper').append(this.fieldNamesListView.render().$el);
            this.$emptyMessage = this.$('.no-field-names');
            this.$processing = this.$('.processing');

            return this;
        },

        changeFieldText: function() {
            this.setProcessing();
            var fieldTextArray = _.chain(this.fieldNamesListView.views)
                .map(function(view) {
                    return {
                        field: view.model.get('name'),
                        values: view.getChecked()
                    }
                })
                .reject(function(data) {
                    return _.isEmpty(data.values)
                })
                .map(function(data) {
                    return new parser.ExpressionNode("MATCH", [data.field], data.values);
                }, this)
                .value();

            if(!_.isEmpty(fieldTextArray)) {
                this.fieldText = _.reduce(fieldTextArray, function(memo, expression) {
                    return memo.AND(expression);
                })
            }
            else {
                this.fieldText = null;
            }

            this.trigger('change', this.fieldText);
        },

        uncheckField: function(field) {
            var view = _.find(this.fieldNamesListView.views, function(view) {
                return view.model.id === field
            });

            view.clear();
        },

        setDatabases: function(databases) {
            this.databases = databases;
            this.fetch();
        },

        setQueryText: function(queryText) {
            this.queryText = queryText;
            this.fetch();
        },

        setRequestFieldText: function(fieldText) {
            this.requestFieldText = fieldText;
            this.fetch();
        },

        clearFieldText: function() {
            this.fieldText = null;
            this.requestFieldText = null;
            this.trigger('change', null);

            _.each(this.fieldNamesListView.views, function(view) {
                view.clear();
            });
        },

        getFieldText: function() {
            return this.fieldText;
        },

        fetch: function() {
            this.setProcessing();

            if(!_.isEmpty(this.databases) && this.queryText) {
                var fieldTextString;

                if(this.requestFieldText) {
                    fieldTextString = this.requestFieldText.toString();
                }

                this.collection.fetch({
                    data: {
                        databases: this.databases,
                        queryText: this.queryText,
                        fieldText: fieldTextString || null
                    },
                    error: _.bind(function(model, err) {
                        this.setDone();
                    },this)
                })
            }
        },

        setProcessing: function() {
            if(this.$processing) {
                this.$processing.removeClass('hide');
                this.$fieldNamesListView.addClass('hide');
                this.$emptyMessage.addClass('hide');
            }
        },

        setDone: function() {
            if(this.$emptyMessage && this.$fieldNamesListView) {
                this.$emptyMessage.toggleClass('hide', !this.collection.isEmpty());
                this.$fieldNamesListView.toggleClass('hide', this.collection.isEmpty());
            }
            if(this.$processing) {
                this.$processing.addClass('hide');
            }
        }
    })
});
