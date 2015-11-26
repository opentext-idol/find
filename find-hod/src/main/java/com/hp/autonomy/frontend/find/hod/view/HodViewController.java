package com.hp.autonomy.frontend.find.hod.view;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.view.AbstractViewController;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.frontend.view.ViewContentSecurityPolicy;
import com.hp.autonomy.frontend.view.hod.HodViewService;
import com.hp.autonomy.hod.client.api.authentication.HodAuthenticationFailedException;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.hod.sso.HodAuthentication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;

@Controller
@RequestMapping("/api/public/view")
@Slf4j
public class HodViewController extends AbstractViewController {
    @Autowired
    private ConfigService<HodFindConfig> configService;

    @Autowired
    private HodViewService hodViewService;

    @Autowired
    private MessageSource messageSource;

    @RequestMapping(value = "/viewDocument", method = RequestMethod.GET)
    public void viewDocument(
            @RequestParam("reference") final String reference,
            @RequestParam("domain") final String domain,
            @RequestParam("index") final String index,
            final HttpServletResponse response
    ) throws HodErrorException, IOException {
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        ViewContentSecurityPolicy.addContentSecurityPolicy(response);
        hodViewService.viewDocument(reference, new ResourceIdentifier(domain, index), response.getOutputStream());
    }

    @RequestMapping(value = "/viewStaticContentPromotion", method = RequestMethod.GET)
    public void viewStaticContentPromotion(
            @RequestParam("reference") final String reference,
            final HttpServletResponse response
    ) throws IOException, HodErrorException {
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        ViewContentSecurityPolicy.addContentSecurityPolicy(response);

        final String domain = ((HodAuthentication) SecurityContextHolder.getContext().getAuthentication()).getPrincipal().getApplication().getDomain();
        final String queryManipulationIndex = configService.getConfig().getQueryManipulation().getIndex();
        hodViewService.viewStaticContentPromotion(reference, new ResourceIdentifier(domain, queryManipulationIndex), response.getOutputStream());
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
}
