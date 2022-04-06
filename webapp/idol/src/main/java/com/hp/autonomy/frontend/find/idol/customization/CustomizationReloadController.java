/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.idol.customization;

import com.hp.autonomy.frontend.find.core.beanconfiguration.AppConfiguration;
import com.hp.autonomy.frontend.find.core.customization.ReloadableCustomizationComponent;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.ErrorModelAndViewInfo;
import com.hp.autonomy.frontend.find.core.web.FindController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.hp.autonomy.frontend.find.idol.customization.CustomizationReloadController.ADMIN_CUSTOMIZATION_PATH;

@Controller
@Slf4j
@RequestMapping(ADMIN_CUSTOMIZATION_PATH)
public class CustomizationReloadController {
    public final static String ADMIN_CUSTOMIZATION_PATH = "/api/admin/customization";
    public final static String CONFIG_RELOAD_PATH = "/config/reload";
    private final ControllerUtils controllerUtils;
    private final Collection<ReloadableCustomizationComponent> reloadableCustomizationComponents;
    private final String contextPath;

    @Autowired
    public CustomizationReloadController(
        @Value(AppConfiguration.SERVER_CONTEXT_PATH) String contextPath,
        final Collection<ReloadableCustomizationComponent> reloadableCustomizationComponents,
        final ControllerUtils controllerUtils)
    {
        this.reloadableCustomizationComponents = reloadableCustomizationComponents;
        this.controllerUtils = controllerUtils;
        this.contextPath = contextPath;
    }

    @RequestMapping(CONFIG_RELOAD_PATH)
    public void reloadConfig(final HttpServletResponse response) throws Exception {
        for(final ReloadableCustomizationComponent component : reloadableCustomizationComponents) {
            log.info("Reloading customization component: " + component.getClass().getSimpleName());
            component.reload();
        }

        // Prevents a situation where we could have two adjacent slashes in a URL contcatenated
        // from the server context path and the APP_PATH
        final List<String> pathElements = Stream.of(
            (contextPath + FindController.APP_PATH).split("/")
        ).filter(s -> !s.isEmpty()).collect(Collectors.toList());

        final String redirect = "/" + String.join("/", pathElements);
        response.sendRedirect(redirect);
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView errorHandler(
        final HttpServletRequest request,
        final Exception e)
    {
        log.error("Failed to reload customization component", e);

        return controllerUtils.buildErrorModelAndView(
            new ErrorModelAndViewInfo.Builder()
                .setRequest(request)
                .setMainMessageCode(null)
                .setSubMessageCode(null)
                .setMainMessage(e.getMessage())
                .setSubMessage(e.getCause().getMessage())
                .setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setButtonHref(URI.create(FindController.APP_PATH))
                .setAuthError(false)
                .build()
        );
    }
}
