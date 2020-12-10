/*
 * Copyright 2017, 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.users;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.configuration.RelatedUsersConfig;
import com.hp.autonomy.frontend.find.core.configuration.RelatedUsersSourceConfig;
import com.hp.autonomy.frontend.find.core.configuration.UserDetailsFieldConfig;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.types.idol.responses.User;
import com.hp.autonomy.types.idol.responses.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping(IdolUserController.BASE_PATH)
public class IdolUserController {

    static final String BASE_PATH = "/api/public/user";
    static final String SEARCH_PATH = "/search";
    static final String RELATED_PATH = "/related-to-search";
    static final String PARAMETER_SEARCH_TEXT = "searchText";
    static final String PARAMETER_START_USER = "startUser";
    static final String PARAMETER_MAX_USERS = "maxUsers";
    private final ConfigService<IdolFindConfig> configService;
    private final IdolUserSearchService idolUserSearchService;

    @Autowired
    public IdolUserController(
        final ConfigService<IdolFindConfig> configService,
        final IdolUserSearchService idolUserSearchService
    ) {
        this.configService = configService;
        this.idolUserSearchService = idolUserSearchService;
    }

    /**
     * Get a list of usernames matching with names matching the search text.
     */
    @RequestMapping(value = SEARCH_PATH, method= RequestMethod.GET)
    @ResponseBody
    public List<String> searchUsers(@RequestParam(PARAMETER_SEARCH_TEXT) final String searchText, @RequestParam(PARAMETER_START_USER) final int startUser, @RequestParam(PARAMETER_MAX_USERS) final int maxUsers) {
        final UserDetails details =
            idolUserSearchService.searchUser(searchText, startUser, maxUsers);
        return details.getUser().stream()
            .map(user -> user.getUsername())
            .collect(Collectors.toList());
    }

    /**
     * Strip a {@link RelatedUser} down to the minimal information required by consumers of the
     * {@link #getRelatedToSearch} endpoint.
     *
     * @param relatedUser
     * @param config Used to restrict the fields in the returned user
     * @return Restricted user
     */
    private RelatedUser restrictUserRelatedToSearch(
        final RelatedUser relatedUser, final RelatedUsersConfig config
    ) {
        final User user = relatedUser.getUser();
        final User restricted = new User();
        restricted.setUid(user.getUid());
        restricted.setUsername(user.getUsername());
        restricted.setEmailaddress(user.getEmailaddress());

        final Map<String, String> fields = user.getFields();
        final Map<String, String> restrictedFields = new HashMap<>();
        final RelatedUsersSourceConfig sourceConfig =
            relatedUser.isExpert() ? config.getExpertise() : config.getInterests();
        for (final UserDetailsFieldConfig allowedField :
            sourceConfig.getUserDetailsFields()
        ) {
            final String name = allowedField.getName();
            if (fields.containsKey(name)) {
                restrictedFields.put(name, fields.get(name));
            }
        }
        restricted.setFields(restrictedFields);

        return new RelatedUser(restricted, relatedUser.isExpert());
    }

    /**
     * Get users with profiles similar to the search text.
     *
     * @return Users with only the following {@link User} fields filled in: uid, username,
     *         emailaddress, fields (restricted to configured display fields)
     */
    @RequestMapping(value = RELATED_PATH, method= RequestMethod.GET)
    @ResponseBody
    public List<RelatedUser> getRelatedToSearch(
        @RequestParam(PARAMETER_SEARCH_TEXT) final String searchText,
        @RequestParam(PARAMETER_MAX_USERS) final int maxUsers
    ) {
        final RelatedUsersConfig config = configService.getConfig().getUsers().getRelatedUsers();
        // this is an important check for security reasons
        if (!config.getEnabled()) {
            throw new IllegalArgumentException("The related users feature is disabled");
        }
        return idolUserSearchService.getRelatedToSearch(config, searchText, maxUsers).stream()
            .map(relatedUser -> restrictUserRelatedToSearch(relatedUser, config))
            .collect(Collectors.toList());
    }

}
