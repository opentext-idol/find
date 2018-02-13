/*
 * Copyright 2017 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import com.hp.autonomy.frontend.find.core.beanconfiguration.BiConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static com.hp.autonomy.frontend.find.core.savedsearches.SharedToUserController.SEARCH_ID_PARAM;

@Controller
@ConditionalOnExpression(BiConfiguration.BI_PROPERTY_SPEL)
@RequestMapping(SharedToUserController.SHARED_SEARCHES_PATH)
class SharedToEveryoneController {
    static final String PERMISSIONS_PATH = "/everyone/permissions";

    private final SharedToEveryoneRepository sharedToEveryoneRepository;

    @Autowired
    public SharedToEveryoneController(
            final SharedToEveryoneRepository sharedToEveryoneRepository
    ) {
        this.sharedToEveryoneRepository = sharedToEveryoneRepository;
    }

    @RequestMapping(value = PERMISSIONS_PATH + "/{"+SEARCH_ID_PARAM+"}", method = RequestMethod.GET)
    @ResponseBody
    public SharedToEveryone getPermissionsForSearch(
            @PathVariable(SEARCH_ID_PARAM) final Long searchId
    ) {
        return sharedToEveryoneRepository.findOneBySavedSearch_Id(searchId);
    }

    @RequestMapping(value = PERMISSIONS_PATH + "/{"+SEARCH_ID_PARAM+"}", method = {RequestMethod.PUT, RequestMethod.POST})
    @ResponseBody
    public SharedToEveryone save(
            @RequestBody final SharedToEveryone sharedToEveryone,
            @PathVariable(SEARCH_ID_PARAM) final long searchId
    ) {
        sharedToEveryone.setId(new SharedToEveryonePK(searchId));

        final SharedToEveryone existing = sharedToEveryoneRepository.findOne(sharedToEveryone.getId());
        sharedToEveryone.merge(existing);

        return sharedToEveryoneRepository.save(sharedToEveryone);
    }

    @RequestMapping(value = PERMISSIONS_PATH + "/{"+SEARCH_ID_PARAM+"}", method = RequestMethod.DELETE)
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable(SEARCH_ID_PARAM) final long searchId
    ) {
        sharedToEveryoneRepository.delete(new SharedToEveryonePK(searchId));
    }

}
