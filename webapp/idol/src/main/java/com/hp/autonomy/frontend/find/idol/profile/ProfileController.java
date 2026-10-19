/*
 * Copyright 2018 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.idol.profile;

import com.hp.autonomy.frontend.configuration.authentication.CommunityPrincipal;
import com.hp.autonomy.frontend.find.idol.api.ProfileApi;
import com.hp.autonomy.user.UserService;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import com.opentext.idol.types.responses.Profiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProfileController implements ProfileApi {

    private final UserService userService;
    private final AuthenticationInformationRetriever<?, CommunityPrincipal> authenticationInformationRetriever;

    @Autowired
    public ProfileController(
            final UserService userService,
            final AuthenticationInformationRetriever<?, CommunityPrincipal> authenticationInformationRetriever
    ) {
        this.userService = userService;
        this.authenticationInformationRetriever = authenticationInformationRetriever;
    }

    @Override
    public ResponseEntity<Profiles> getProfileTerms() {
        return ResponseEntity.ok(
                userService.profileRead(authenticationInformationRetriever.getPrincipal().getName()));
    }
}
