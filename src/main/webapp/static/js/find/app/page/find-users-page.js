define([
    'users-page/js/users-page',
    'i18n!find/nls/bundle',
    'find/app/model/current-user',
    'find/app/model/users-collection'
], function(UserPage, i18n, currentUserModel, usersCollection) {

    return UserPage.extend({
        currentUser: currentUserModel,
        usersCollection: usersCollection,
        roleSource: [
            {value: 'admin', text: i18n['users.admin'], icon: 'icon-user'},
            {value: 'useradmin', text: i18n['users.useradmin'], icon: 'icon-github-alt'}
        ],
        strings: {
            closeButton: i18n['users.button.cancel'],
            create: i18n['users.create'],
            createButton: i18n['users.button.create'],
            createUserButton: i18n['users.button.createUser'],
            createdMessage: i18n['users.info.createdMessage'],
            creationFailedMessage: i18n['users.info.creationFailedMessage'],
            delete: i18n['users.delete'],
            deleteCancel: i18n["users.delete.cancel"],
            deleteConfirm: i18n["users.delete.confirm"],
            deletedMessage: i18n['users.info.deletedMessage'],
            deleteText: i18n["users.delete.text"],
            infoDone: i18n['users.info.done'],
            infoError: i18n['users.info.error'],
            noUsers: i18n['users.noUsers'],
            password: i18n['users.password'],
            passwordConfirm: i18n['users.password.confirm'],
            passwordError: i18n['users.password.error'],
            passwordMatchError: i18n['users.username.password.match.error'],
            refreshButton: i18n['users.refresh'],
            selectRole: i18n['users.select.level'],
            serverError: i18n['users.serverError'],
            tagline: i18n['users.tagline'],
            title: i18n['users.title'],
            username: i18n['users.username'],
            usernameBlank: i18n['users.username.blank'],
            usernameDuplicate: i18n['users.username.duplicate']
        }
    });

});
