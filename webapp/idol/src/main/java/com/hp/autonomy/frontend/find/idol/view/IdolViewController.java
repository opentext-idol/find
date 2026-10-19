/*
 * Copyright 2015 Open Text.
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
import com.hp.autonomy.frontend.find.idol.api.ViewApi;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.frontend.logging.Markers;
import com.hp.autonomy.searchcomponents.core.view.ViewingPart;
import com.hp.autonomy.searchcomponents.idol.view.*;
import com.hp.autonomy.user.UserService;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

@Controller
@Slf4j
class IdolViewController extends ViewController<IdolViewRequest, String, AciErrorException> implements ViewApi {
    private final ControllerUtils controllerUtils;
    private final UserService userService;
    private final AuthenticationInformationRetriever<?, CommunityPrincipal> authenticationInformationRetriever;
    private final HttpServletResponse response;

    private final boolean updateProfileOnView;

    @SuppressWarnings("TypeMayBeWeakened")
    @Autowired
    public IdolViewController(
            final IdolViewServerService viewServerService,
            final ObjectFactory<IdolViewRequestBuilder> viewRequestBuilder,
            final ControllerUtils controllerUtils,
            final UserService userService,
            final AuthenticationInformationRetriever<?, CommunityPrincipal> authenticationInformationRetriever,
            final ConfigFileService<IdolFindConfig> configService,
            final HttpServletResponse response
    ) {
        super(viewServerService, viewRequestBuilder);
        this.controllerUtils = controllerUtils;
        this.userService = userService;
        this.authenticationInformationRetriever = authenticationInformationRetriever;
        this.response = response;

        this.updateProfileOnView = Optional.ofNullable(configService.getConfigResponse()).map(ConfigResponse::getConfig).map(IdolFindConfig::getUiCustomization).map(UiCustomization::getProfile)
                .map(ProfileOptions::getUpdateProfileOnView).orElse(false);

    }

    @Override
    public ResponseEntity<Resource> viewDocument(
        final String index,
        final ViewingPart part,
        final String reference,
        final String highlightExpressions,
        final String urlPrefix,
        final String subDocRef
    ) {
        if (updateProfileOnView && part == ViewingPart.DOCUMENT) {
            final CommunityPrincipal principal = authenticationInformationRetriever.getPrincipal();
            if (principal != null) {
                userService.profileUser(principal.getName(), reference);
            }
        }

        try {
            super.viewDocument(reference, index, highlightExpressions, part, urlPrefix, subDocRef,
                    response);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }

        // tell Spring the request has already been handled
        return null;
    }

    @Override
    public ResponseEntity<Resource> viewStaticContentPromotion(final String reference) {
        try {
            super.viewStaticContentPromotion(reference, response);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }

        // tell Spring the request has already been handled
        return null;
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
