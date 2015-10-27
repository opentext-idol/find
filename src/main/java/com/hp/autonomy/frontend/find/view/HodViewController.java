package com.hp.autonomy.frontend.find.view;

import com.hp.autonomy.frontend.find.FindQueryProfileService;
import com.hp.autonomy.frontend.view.ViewContentSecurityPolicy;
import com.hp.autonomy.frontend.view.hod.HodViewService;
import com.hp.autonomy.hod.client.api.authentication.HodAuthenticationFailedException;
import com.hp.autonomy.hod.client.api.queryprofile.QueryProfile;
import com.hp.autonomy.hod.client.api.queryprofile.QueryProfileService;
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
@RequestMapping({"/api/public/view"})
@Slf4j
public class HodViewController extends AbstractViewController {

    @Autowired
    private HodViewService hodViewService;

    @Autowired
    private QueryProfileService queryProfileService;

    @Autowired
    private FindQueryProfileService findQueryProfileService;

    @Autowired
    private MessageSource messageSource;

    @RequestMapping(value = "/viewDocument", method = RequestMethod.GET)
    public void viewDocument(
            @RequestParam("reference") final String reference,
            @RequestParam("indexes") final ResourceIdentifier indexes,
            final HttpServletResponse response
    ) throws HodErrorException, IOException {
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        ViewContentSecurityPolicy.addContentSecurityPolicy(response);
        hodViewService.viewDocument(reference, indexes, response.getOutputStream());
    }

    @RequestMapping(value = "/viewStaticContentPromotion", method = RequestMethod.GET)
    public void viewStaticContentPromotion(
        @RequestParam("reference") final String reference,
        final HttpServletResponse response
    ) throws IOException, HodErrorException {
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        ViewContentSecurityPolicy.addContentSecurityPolicy(response);

        final String domain = ((HodAuthentication) SecurityContextHolder.getContext().getAuthentication()).getPrincipal().getApplication().getDomain();

        final String queryProfileName = findQueryProfileService.getQueryProfile().getName();
        final QueryProfile queryProfile = queryProfileService.retrieveQueryProfile(queryProfileName);

        final ResourceIdentifier queryManipulationIndex = new ResourceIdentifier(domain, queryProfile.getQueryManipulationIndex());
        hodViewService.viewStaticContentPromotion(reference, queryManipulationIndex, response.getOutputStream());
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

        final int errorCode;

        if (e.isServerError()) {
            errorCode = 500;
        } else {
            errorCode = 400;
        }

        final String subMessage;

        if (iodErrorMessage != null) {
            subMessage = messageSource.getMessage("error.iodErrorSub", new String[]{iodErrorMessage}, locale);
        } else {
            subMessage = messageSource.getMessage("error.iodErrorSubNull", null, locale);
        }

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
