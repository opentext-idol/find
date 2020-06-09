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

package com.hp.autonomy.frontend.find.core.view;

import com.hp.autonomy.searchcomponents.core.view.ViewContentSecurityPolicy;
import com.hp.autonomy.searchcomponents.core.view.ViewRequest;
import com.hp.autonomy.searchcomponents.core.view.ViewRequestBuilder;
import com.hp.autonomy.searchcomponents.core.view.ViewServerService;
import org.springframework.beans.factory.ObjectFactory;
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
public abstract class ViewController<R extends ViewRequest<S>, S extends Serializable, E extends Exception> {
    public static final String VIEW_PATH = "/api/public/view";
    public static final String VIEW_DOCUMENT_PATH = "/viewDocument";
    public static final String REFERENCE_PARAM = "reference";
    public static final String DATABASE_PARAM = "index";
    private static final String VIEW_STATIC_CONTENT_PROMOTION_PATH = "/viewStaticContentPromotion";
    protected static final String HIGHLIGHT_PARAM = "highlightExpressions";

    private final ViewServerService<R, S, E> viewServerService;
    private final ObjectFactory<? extends ViewRequestBuilder<R, S, ?>> viewRequestBuilderFactory;

    protected ViewController(final ViewServerService<R, S, E> viewServerService,
                             final ObjectFactory<? extends ViewRequestBuilder<R, S, ?>> viewRequestBuilderFactory) {
        this.viewServerService = viewServerService;
        this.viewRequestBuilderFactory = viewRequestBuilderFactory;
    }

    @RequestMapping(value = VIEW_DOCUMENT_PATH, method = RequestMethod.GET)
    public void viewDocument(
            @RequestParam(REFERENCE_PARAM) final String reference,
            @RequestParam(DATABASE_PARAM) final S database,
            @RequestParam(value = HIGHLIGHT_PARAM, required = false) final String highlightExpression,
            final HttpServletResponse response
    ) throws E, IOException {
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        ViewContentSecurityPolicy.addContentSecurityPolicy(response);
        final R request = viewRequestBuilderFactory.getObject()
                .documentReference(reference)
                .database(database)
                .highlightExpression(highlightExpression)
                .build();
        viewServerService.viewDocument(request, response.getOutputStream());
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
