/*
 * (c) Copyright 2015 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.hod.authentication;

import com.hp.autonomy.frontend.find.core.web.ErrorResponse;
import com.hp.autonomy.hod.client.api.authentication.SignedRequest;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.hod.sso.HodAuthenticationRequestService;
import com.hp.autonomy.hod.sso.InvalidOriginException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URL;

/**
 * Controller which signs requests needed by the SSO process frontend code.
 */
@RestController
public class HodCombinedRequestController {
    public static final String COMBINED_PATCH_REQUEST_URL = "/api/authentication/combined-patch-request";

    private final HodAuthenticationRequestService tokenService;

    @Autowired
    public HodCombinedRequestController(final HodAuthenticationRequestService tokenService) {
        this.tokenService = tokenService;
    }

    @RequestMapping(value = COMBINED_PATCH_REQUEST_URL, method = RequestMethod.GET)
    public SignedRequest getCombinedPatchRequest(@RequestParam("redirect-url") final URL redirectUrl) throws InvalidOriginException, HodErrorException {
        return tokenService.getSsoPageCombinedPatchRequest(redirectUrl);
    }

    @ExceptionHandler(InvalidOriginException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidRedirectUrl(final InvalidOriginException exception) {
        return new ErrorResponse("Origin of redirect URL " + exception.getUrl() + " is not allowed");
    }
}
