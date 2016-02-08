/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.view;

import com.hp.autonomy.searchcomponents.core.view.ViewContentSecurityPolicy;
import com.hp.autonomy.searchcomponents.core.view.ViewServerService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;

@Controller
@RequestMapping(ViewController.VIEW_PATH)
public abstract class ViewController<S extends Serializable, E extends Exception> {
    public static final String VIEW_PATH = "/api/public/view";
    public static final String VIEW_DOCUMENT_PATH = "/viewDocument";
    public static final String VIEW_STATIC_CONTENT_PROMOTION_PATH = "/viewStaticContentPromotion";
    public static final String REFERENCE_PARAM = "reference";
    public static final String DATABASE_PARAM = "index";

    protected final ViewServerService<S, E> viewServerService;

    protected ViewController(final ViewServerService<S, E> viewServerService) {
        this.viewServerService = viewServerService;
    }

    @RequestMapping(value = VIEW_DOCUMENT_PATH, method = RequestMethod.GET)
    public void viewDocument(
            @RequestParam(REFERENCE_PARAM) final String reference,
            @RequestParam(DATABASE_PARAM) final S database,
            final HttpServletResponse response
    ) throws E, IOException {
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        ViewContentSecurityPolicy.addContentSecurityPolicy(response);
        viewServerService.viewDocument(reference, database, response.getOutputStream());
    }

    @RequestMapping(value = VIEW_STATIC_CONTENT_PROMOTION_PATH, method = RequestMethod.GET)
    public void viewStaticContentPromotion(
            @RequestParam(REFERENCE_PARAM) final String reference,
            final HttpServletResponse response
    ) throws IOException, E {
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        ViewContentSecurityPolicy.addContentSecurityPolicy(response);

        viewServerService.viewStaticContentPromotion(reference, response.getOutputStream());
    }
}
