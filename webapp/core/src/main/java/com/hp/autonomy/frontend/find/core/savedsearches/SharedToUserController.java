/*
 * Copyright 2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.find.core.beanconfiguration.BiConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Controller
@ConditionalOnProperty(BiConfiguration.BI_PROPERTY)
@RequestMapping(SharedToUserController.SHARED_SEARCHES_PATH)
class SharedToUserController {
    static final String SHARED_SEARCHES_PATH = "/api/public/search/shared-searches";
    private static final String PERMISSIONS_PATH = "permissions";
    private static final String SAVE_PERMISSION_PATH = "save";
    private static final String DELETE_PERMISSION_PATH = "delete";
    private static final String SEARCH_ID_PARAM = "searchId";
    private static final String DATA_PARAM = "data";

    private final SharedToUserRepository sharedToUserRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public SharedToUserController(final SharedToUserRepository sharedToUserRepository,
                                  final ObjectMapper objectMapper) {
        this.sharedToUserRepository = sharedToUserRepository;
        this.objectMapper = objectMapper;
    }

    @RequestMapping(PERMISSIONS_PATH)
    public Set<SharedToUser> getPermissionsForSearch(@RequestParam(SEARCH_ID_PARAM) final String searchId) {
        return sharedToUserRepository.findBySavedSearch_Id(Long.parseLong(searchId));
    }

    @RequestMapping(SAVE_PERMISSION_PATH)
    public void save(@RequestParam(DATA_PARAM) final String data) throws IOException {
        final List<SharedToUser> sharedToUsers = parseData(data);
        sharedToUserRepository.save(sharedToUsers);
    }

    @RequestMapping(DELETE_PERMISSION_PATH)
    public void delete(@RequestParam(DATA_PARAM) final String data) throws IOException {
        final List<SharedToUser> permissionsToDelete = parseData(data);
        sharedToUserRepository.delete(permissionsToDelete);
    }

    private List<SharedToUser> parseData(final String data) throws IOException {
        return objectMapper.readValue(data, new TypeReference<List<SharedToUser>>() {});
    }
}
