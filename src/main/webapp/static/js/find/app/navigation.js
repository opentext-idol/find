define([
    'js-utils/js/navigation',
    'i18n!find/nls/bundle',
    'find/app/router',
    'js-utils/js/ensure-array',
    'find/app/model/current-user'
], function(Navigation, i18n, router, ensureArray, currentUser) {

    return Navigation.extend({
        event: 'route:find',
        router: router,

        render: function() {
            Navigation.prototype.render.call(this);

            this.$('li[data-pagename="users"]').after('<li class="divider hide-from-non-useradmin"></li>');
            this.$('.logout-button').before('<li class="divider"></li>');

            currentUser.onLoad(_.bind(function(user) {
                this.$('.nav-username').text(user.get('username'));
                this.$('li.user-dropdown-user b').text(user.get('username'));
                this.$('li.user-dropdown-role b').text(user.get('roles').join(', '));

                if (_.contains(user.get('roles'), 'useradmin')) {
                    this.$('.hide-from-non-useradmin').removeClass('hide-from-non-useradmin');
                    this.$('.hide-from-non-admin').removeClass('hide-from-non-admin');
                }

                if (_.contains(user.get('roles'), 'admin')) {
                    this.$('.hide-from-non-admin').removeClass('hide-from-non-admin');
                }
            }, this));
        },

        getTemplateParameters: function() {
            var pages = this.pages.pages;

            return {
                appName: i18n['app.name'],
                brandRoute: 'find/find-search',
                navLeft: [
                    _.findWhere(pages, {pageName: 'find-search'}),
                    {
                        icon: 'icon-hand-down',
                        label: 'Placeholder Group',
                        children: _.where(pages, {group: 'group'})
                    }
                ],
                navRight: [
                    {
                        classes: 'user-dropdown',
                        icon: 'icon-user',
                        label: '<span class="nav-username"></span>',
                        children: [
                            {
                                classes: 'nav-user-dropdown user-dropdown-user',
                                label: i18n['app.user'] + ': <b></b>'
                            },
                            {
                                classes: 'nav-user-dropdown user-dropdown-role',
                                label: i18n['app.roles'] + ': <b></b>'
                            },
                            {
                                classes: 'logout-button',
                                href: '../login/login.html',
                                label: i18n['app.logout']
                            }
                        ]
                    },
                    {
                        icon: 'icon-cog',
                        label: i18n['app.settings'],
                        children: _.union(
                            _.findWhere(pages, {pageName: 'settings'}),
                            _.findWhere(pages, {pageName: 'users'}),
                            _.where(pages, {group: 'settings'})
                        )
                    }
                ]
            };
        }
    });

});