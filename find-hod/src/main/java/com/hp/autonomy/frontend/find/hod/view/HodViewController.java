/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.view;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.view.ViewController;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.hod.client.api.authentication.HodAuthenticationFailedException;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.hod.sso.HodAuthentication;
import com.hp.autonomy.searchcomponents.core.view.ViewContentSecurityPolicy;
import com.hp.autonomy.searchcomponents.hod.view.HodViewService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

@Controller
@RequestMapping("/api/public/view")
@Slf4j
public class HodViewController extends ViewController<HodViewService, ResourceIdentifier, HodErrorException> {
    private static final String ERROR_PAGE = "error";

    private final ConfigService<HodFindConfig> configService;
    private final MessageSource messageSource;

    @Autowired
    public HodViewController(final HodViewService viewServerService, final ConfigService<HodFindConfig> configService, final MessageSource messageSource) {
        super(viewServerService);
        this.configService = configService;
        this.messageSource = messageSource;
    }


    @RequestMapping(value = "/viewStaticContentPromotion", method = RequestMethod.GET)
    public void viewStaticContentPromotion(
            @RequestParam(REFERENCE_PARAM) final String reference,
            final HttpServletResponse response
    ) throws IOException, HodErrorException {
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        ViewContentSecurityPolicy.addContentSecurityPolicy(response);

        final String domain = ((HodAuthentication) SecurityContextHolder.getContext().getAuthentication()).getPrincipal().getApplication().getDomain();
        final String queryManipulationIndex = configService.getConfig().getQueryManipulation().getIndex();
        viewServerService.viewStaticContentPromotion(reference, new ResourceIdentifier(domain, queryManipulationIndex), response.getOutputStream());
    }

    @ExceptionHandler
    public ModelAndView handleIodErrorException(
            final HodErrorException e,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) {
        response.reset();

        log.error("IodErrorException thrown while viewing document", e);

        final Locale locale = Locale.ENGLISH;

        final String errorKey = "error.iodErrorCode." + e.getErrorCode();
        String iodErrorMessage;

        try {
            iodErrorMessage = messageSource.getMessage(errorKey, null, locale);
        } catch (final NoSuchMessageException e1) {
            // we don't have a key in the bundle for this error code
            iodErrorMessage = messageSource.getMessage("error.unknownError", null, locale);
        }

        final int errorCode = e.isServerError() ? 500 : 400;

        final String subMessage = iodErrorMessage != null ? messageSource.getMessage("error.iodErrorSub", new String[]{iodErrorMessage}, locale) : messageSource.getMessage("error.iodErrorSubNull", null, locale);

        response.setStatus(errorCode);

        return buildErrorModelAndView(
                request,
                messageSource.getMessage("error.iodErrorMain", null, locale),
                subMessage
        );
    }

    @ExceptionHandler
    public ModelAndView hodAuthenticationFailedException(
            final HodAuthenticationFailedException e,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws IOException {
        response.reset();
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        log.error("HodAuthenticationFailedException thrown while viewing document", e);

        return buildErrorModelAndView(
                request,
                messageSource.getMessage("error.iodErrorMain", null, Locale.ENGLISH),
                messageSource.getMessage("error.iodTokenExpired", null, Locale.ENGLISH),
                false
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleGeneralException(
            final Exception e,
            final HttpServletRequest request,
            final ServletResponse response
    ) {
        response.reset();

        final UUID uuid = UUID.randomUUID();
        log.error("Unhandled exception with uuid {}", uuid);
        log.error("Stack trace", e);

        final Locale locale = Locale.ENGLISH;

        return buildErrorModelAndView(
                request,
                messageSource.getMessage("error.internalServerErrorMain", null, locale),
                messageSource.getMessage("error.internalServerErrorSub", new Object[]{uuid}, locale)
        );
    }

    protected ModelAndView buildErrorModelAndView(
            final HttpServletRequest request,
            final String mainMessage,
            final String subMessage
    ) {
        return buildErrorModelAndView(request, mainMessage, subMessage, true);
    }

    protected ModelAndView buildErrorModelAndView(
            final HttpServletRequest request,
            final String mainMessage,
            final String subMessage,
            final boolean contactSupport
    ) {
        final ModelAndView modelAndView = new ModelAndView(ERROR_PAGE);
        modelAndView.addObject("mainMessage", mainMessage);
        modelAndView.addObject("subMessage", subMessage);
        modelAndView.addObject("baseUrl", getBaseUrl(request));
        modelAndView.addObject("contactSupport", contactSupport);

        return modelAndView;
    }

    private String getBaseUrl(final HttpServletRequest request) {
        final String path = request.getRequestURI().replaceFirst(request.getContextPath(), "");

        final int depth = StringUtils.countMatches(path, "/") - 1;

        return depth == 0 ? "." : StringUtils.repeat("../", depth);
    }
}
