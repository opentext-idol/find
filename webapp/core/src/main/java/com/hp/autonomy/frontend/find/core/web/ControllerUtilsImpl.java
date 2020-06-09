/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.core.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.beanconfiguration.AppConfiguration;
import com.hp.autonomy.frontend.find.core.configuration.FindConfig;
import com.hp.autonomy.frontend.find.core.configuration.UiCustomization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@Slf4j
class ControllerUtilsImpl implements ControllerUtils {
    private static final String MESSAGE_CODE_CONTACT_SUPPORT_UUID = "error.contactSupportUUID";
    private static final String MESSAGE_CODE_CONTACT_SUPPORT_NO_UUID = "error.contactSupportNoUUID";
    private static final String MESSAGE_CODE_ERROR_BUTTON = "error.button";

    private static final Pattern JSON_ESCAPE_PATTERN = Pattern.compile("</", Pattern.LITERAL);

    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;
    private final String commit;
    private final ConfigService<? extends FindConfig<?, ?>> configService;

    @Autowired
    public ControllerUtilsImpl(
        final ObjectMapper objectMapper,
        final MessageSource messageSource,
        @Value(AppConfiguration.GIT_COMMIT_PROPERTY) final String commit,
        final ConfigService<? extends FindConfig<?, ?>> configService)
    {
        this.objectMapper = objectMapper;
        this.messageSource = messageSource;
        this.commit = commit;
        this.configService = configService;
    }

    @Override
    public String convertToJson(final Object object) throws JsonProcessingException {
        // As we are inserting into a script tag escape </ to prevent injection
        return JSON_ESCAPE_PATTERN.matcher(objectMapper.writeValueAsString(object)).replaceAll("<\\\\/");
    }

    @Override
    public String getMessage(final String code, final Object[] args) throws NoSuchMessageException {
        return messageSource.getMessage(code, args, Locale.ENGLISH);
    }

    @Override
    public ModelAndView buildErrorModelAndView(final ErrorModelAndViewInfo errorInfo) {
        final ModelAndView modelAndView = new ModelAndView(ViewNames.ERROR.viewName());
        modelAndView.addObject(ErrorAttributes.MAIN_MESSAGE.value(),
                               errorInfo.getMainMessageCode() == null
                                   ? errorInfo.getMainMessage()
                                   : getMessage(errorInfo.getMainMessageCode(), null));
        modelAndView.addObject(ErrorAttributes.SUB_MESSAGE.value(),
                               errorInfo.getSubMessageCode() == null
                                   ? errorInfo.getSubMessage()
                                   : getMessage(errorInfo.getSubMessageCode(), errorInfo.getSubMessageArguments()));
        modelAndView.addObject(ErrorAttributes.BASE_URL.value(), RequestUtils.buildBaseUrl(errorInfo.getRequest()));
        modelAndView.addObject(ErrorAttributes.STATUS_CODE.value(), errorInfo.getStatusCode());
        modelAndView.addObject(ErrorAttributes.AUTH_ERROR.value(), errorInfo.isAuthError());

        final String contactSupportMessage = Optional
            .ofNullable(configService.getConfig().getUiCustomization())
            .map(UiCustomization::getErrorCallSupportString)
            .orElse(null);

        if(errorInfo.isContactSupport()) {
            if(errorInfo.getException() != null) {
                final UUID uuid = UUID.randomUUID();
                log.error("Unhandled exception with uuid {}", uuid);
                log.error("Stack trace", errorInfo.getException());
                modelAndView.addObject(ErrorAttributes.CONTACT_SUPPORT.value(),
                                       contactSupportMessage == null
                                           ? getMessage(MESSAGE_CODE_CONTACT_SUPPORT_UUID, new Object[]{uuid})
                                           : contactSupportMessage);
            } else {
                modelAndView.addObject(ErrorAttributes.CONTACT_SUPPORT.value(),
                                       contactSupportMessage == null
                                           ? getMessage(MESSAGE_CODE_CONTACT_SUPPORT_NO_UUID, null)
                                           : contactSupportMessage);
            }
        }

        if(errorInfo.getButtonHref() != null) {
            modelAndView.addObject(ErrorAttributes.BUTTON_HREF.value(), errorInfo.getButtonHref());
            modelAndView.addObject(ErrorAttributes.BUTTON_MESSAGE.value(),
                                   getMessage(MESSAGE_CODE_ERROR_BUTTON, null));
        }
        modelAndView.addObject(MvcConstants.GIT_COMMIT.value(), commit);

        return modelAndView;
    }
}
