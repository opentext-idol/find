/*
 * Copyright (c) 2018, Micro Focus International plc.
 */

package com.hp.autonomy.frontend.find.idol.profile;

import com.hp.autonomy.frontend.configuration.authentication.CommunityPrincipal;
import com.hp.autonomy.types.idol.responses.Profiles;
import com.hp.autonomy.user.UserService;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/public/profile")
public class ProfileController {

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

    @RequestMapping(value = "terms", method = RequestMethod.GET)
    @ResponseBody
    public Profiles getTerms() {
        return userService.profileRead(authenticationInformationRetriever.getPrincipal().getName());
    }
}
