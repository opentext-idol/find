/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery'
], function(_, $) {
    'use strict';

    function SavedQueryResultPoller(options) {
        this.config = options.config;
        this.savedQueryCollection = options.savedQueryCollection;
        this.queryStates = options.queryStates;
        this.onSuccess = options.onSuccess;

        this.savedQueryIntervalId = setInterval(
            _.bind(this.pollForUpdates, this),
            this.config.pollingInterval * 60 * 1000
        );
        this.pollForUpdates();
    }

    SavedQueryResultPoller.prototype.pollForUpdates = function() {
        this.savedQueryCollection.each(_.bind(this.pollSingleForUpdates, this));
    };

    SavedQueryResultPoller.prototype.pollSingleForUpdates = function(savedQueryModel) {
        if(isPollable(savedQueryModel, this.queryStates)) {
            $.get('api/bi/saved-query/new-results/' + savedQueryModel.id)
                .success(_.partial(this.onSuccess, savedQueryModel.id));
        }
    };

    SavedQueryResultPoller.prototype.destroy = function() {
        clearInterval(this.savedQueryIntervalId);
    };

    function isPollable(savedQueryModel, queryStates) {
        const queryState = queryStates.get(savedQueryModel.cid);

        // To be pollable the model must be saved and either unmodified or have no query state (tab not yet selected)
        return !savedQueryModel.isNew() &&
            (!queryState || savedQueryModel.equalsQueryState(queryState));
    }

    return SavedQueryResultPoller;
});
