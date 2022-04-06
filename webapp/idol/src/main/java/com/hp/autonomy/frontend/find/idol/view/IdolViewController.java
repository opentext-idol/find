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

package com.hp.autonomy.frontend.find.idol.view;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.configuration.ConfigFileService;
import com.hp.autonomy.frontend.configuration.ConfigResponse;
import com.hp.autonomy.frontend.configuration.authentication.CommunityPrincipal;
import com.hp.autonomy.frontend.find.core.configuration.ProfileOptions;
import com.hp.autonomy.frontend.find.core.configuration.UiCustomization;
import com.hp.autonomy.frontend.find.core.view.ViewController;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.ErrorModelAndViewInfo;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.frontend.logging.Markers;
import com.hp.autonomy.searchcomponents.idol.view.*;
import com.hp.autonomy.user.UserService;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import java.io.IOException;
import java.util.Optional;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(IdolViewController.VIEW_PATH)
@Slf4j
class IdolViewController extends ViewController<IdolViewRequest, String, AciErrorException> {
    private final ControllerUtils controllerUtils;
    private final UserService userService;
    private final AuthenticationInformationRetriever<?, CommunityPrincipal> authenticationInformationRetriever;

    private final boolean updateProfileOnView;

    @SuppressWarnings("TypeMayBeWeakened")
    @Autowired
    public IdolViewController(
            final IdolViewServerService viewServerService,
            final ObjectFactory<IdolViewRequestBuilder> viewRequestBuilder,
            final ControllerUtils controllerUtils,
            final UserService userService,
            final AuthenticationInformationRetriever<?, CommunityPrincipal> authenticationInformationRetriever,
            final ConfigFileService<IdolFindConfig> configService
    ) {
        super(viewServerService, viewRequestBuilder);
        this.controllerUtils = controllerUtils;
        this.userService = userService;
        this.authenticationInformationRetriever = authenticationInformationRetriever;


        this.updateProfileOnView = Optional.ofNullable(configService.getConfigResponse()).map(ConfigResponse::getConfig).map(IdolFindConfig::getUiCustomization).map(UiCustomization::getProfile)
                .map(ProfileOptions::getUpdateProfileOnView).orElse(false);

    }

    @Override
    public void viewDocument(
        @RequestParam(REFERENCE_PARAM) final String reference,
        @RequestParam(DATABASE_PARAM) final String database,
        @RequestParam(value = HIGHLIGHT_PARAM, required = false) final String highlightExpression,
        @RequestParam(ORIGINAL_PARAM) final boolean original,
        final HttpServletResponse response
    ) throws AciErrorException, IOException {
        if (updateProfileOnView) {
            final CommunityPrincipal principal = authenticationInformationRetriever.getPrincipal();
            if (principal != null) {
                userService.profileUser(principal.getName(), reference);
            }
        }

        super.viewDocument(reference, database, highlightExpression, original, response);
    }

    @SuppressWarnings("TypeMayBeWeakened")
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleViewDocumentNotFoundException(
            final ViewDocumentNotFoundException e,
            final HttpServletRequest request,
            final ServletResponse response
    ) {
        response.reset();

        final String reference = e.getReference();

        log.info(Markers.AUDIT, "TRIED TO VIEW NON EXISTENT DOCUMENT WITH REFERENCE {}", reference);

        return controllerUtils.buildErrorModelAndView(new ErrorModelAndViewInfo.Builder()
                .setRequest(request)
                .setMainMessageCode("error.documentNotFound")
                .setSubMessageCode("error.referenceDoesNotExist")
                .setSubMessageArguments(new Object[]{reference})
                .setStatusCode(HttpStatus.NOT_FOUND.value())
                .setContactSupport(true)
                .setException(e)
                .build());
    }

    @SuppressWarnings("TypeMayBeWeakened")
    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleViewServerErrorException(
            final ViewServerErrorException e,
            final HttpServletRequest request,
            final ServletResponse response
    ) {
        response.reset();

        final String reference = e.getReference();

        log.info(Markers.AUDIT, "TRIED TO VIEW DOCUMENT WITH REFERENCE {} BUT VIEW SERVER RETURNED AN ERROR PAGE", reference);

        return controllerUtils.buildErrorModelAndView(new ErrorModelAndViewInfo.Builder()
                .setRequest(request)
                .setMainMessageCode("error.viewServerErrorMain")
                .setSubMessageCode("error.viewServerErrorSub")
                .setSubMessageArguments(new Object[]{reference})
                .setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setContactSupport(true)
                .setException(e)
                .build());
    }
}
