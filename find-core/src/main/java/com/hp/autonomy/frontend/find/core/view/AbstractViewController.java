package com.hp.autonomy.frontend.find.core.view;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.UUID;

@Slf4j
public class AbstractViewController {

    protected static final String ERROR_PAGE = "error";

    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleGeneralException(
            final Exception e,
            final HttpServletRequest request,
            final HttpServletResponse response
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

        final String baseUrl;

        if (depth == 0) {
            baseUrl = ".";
        } else {
            baseUrl = StringUtils.repeat("../", depth);
        }

        return baseUrl;
    }

}
