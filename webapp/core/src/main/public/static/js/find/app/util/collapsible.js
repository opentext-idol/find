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
    'text!find/templates/app/util/collapsible.html',
    'underscore',
    'bootstrap'
], function (Backbone, collapsibleTemplate, _) {
    'use strict';

    return Backbone.View.extend({
        template: _.template(collapsibleTemplate, {variable: 'data'}),

        events: {
            'click > .collapsible-header': function () {
                this.$collapse.collapse('toggle');

                // other handlers called before this trigger
                this.trigger('toggle', this.collapseModel.get('collapsed'));
            },
            'show.bs.collapse': function () {
                this.collapseModel.set('collapsed', false);
                this.updateHeaderState();

                this.trigger('show');
            },
            'shown.bs.collapse': function () {
                if (this.renderOnOpen) {
                    this.view.render();
                }

                this.trigger('shown');
            },
            'hide.bs.collapse': function () {
                this.collapseModel.set('collapsed', true);
                this.updateHeaderState();

                this.trigger('hide');
            }
        },

        initialize: function (options) {
            this.view = options.view;
            this.collapseModel = options.collapseModel || new Backbone.Model({collapsed: false});
            this.title = options.title;
            this.subtitle = options.subtitle;
            this.renderOnOpen = options.renderOnOpen || false;
        },

        render: function () {
            this.$el.html(this.template({
                title: this.title,
                subtitle: this.subtitle
            }));

            this.$header = this.$('.collapsible-header');
            this.$title = this.$('.collapsible-title');
            this.updateHeaderState();

            // activate plugin manually for greater control of click handlers
            this.$collapse = this.$('.collapse').collapse({
                toggle: !this.collapseModel.get('collapsed')
            });

            // Render after appending to the DOM since graph views must measure element dimensions
            this.$collapse.append(this.view.$el);
            this.view.delegateEvents().render();
        },

        remove: function () {
            this.view.remove();
            Backbone.View.prototype.remove.call(this);
        },

        updateHeaderState: function () {
            // The "collapsed" class controls the icons with class "rotating-chevron"
            this.$header.toggleClass('collapsed', this.collapseModel.get('collapsed'));
        },

        setSubTitle: function (subtitle) {
            this.subtitle = subtitle;
            this.$('.collapsible-subtitle').text(subtitle);
        },

        toggleSubtitle: function (toggle) {
            this.$('.collapsible-subtitle').toggleClass('hide', !toggle)
        },

        show: function () {
            if (this.collapseModel.get('collapsed')) {
                this.$collapse.collapse('show');
            }
        },

        setTitle: function (title) {
            this.title = title;

            if (this.$title) {
                this.$title.text(this.title);
            }
        },

        hide: function () {
            if (!this.collapseModel.get('collapsed')) {
                this.$collapse.collapse('hide');
            }
        },

        toggle: function (state) {
            if (state) {
                this.show();
            } else {
                this.hide();
            }
        }
    });
});
