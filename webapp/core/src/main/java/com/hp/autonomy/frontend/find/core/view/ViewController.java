/*
 * Copyright 2015-2017 Open Text.
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

package com.hp.autonomy.frontend.find.core.view;

import com.hp.autonomy.frontend.find.core.web.RequestUtils;
import com.hp.autonomy.searchcomponents.core.view.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

public abstract class ViewController<R extends ViewRequest<S>, S extends Serializable, E extends Exception> {
    private final ViewServerService<R, S, E> viewServerService;
    private final ObjectFactory<? extends ViewRequestBuilder<R, S, ?>> viewRequestBuilderFactory;

    protected ViewController(final ViewServerService<R, S, E> viewServerService,
                             final ObjectFactory<? extends ViewRequestBuilder<R, S, ?>> viewRequestBuilderFactory) {
        this.viewServerService = viewServerService;
        this.viewRequestBuilderFactory = viewRequestBuilderFactory;
    }

    /**
     * Determine a filename for a document reference for downloading.
     */
    private static String extractFilename(final String reference) {
        String path;
        try {
            path = new URI(reference).getPath();
        } catch (final URISyntaxException e) {
            path = reference;
        }

        // if there are no path separators, this will just use the whole reference
        final String name = Paths.get(path).getFileName().toString();
        return name.isEmpty() ? reference : name;
    }

    protected void viewDocument(
            final String reference,
            final S database,
            final String highlightExpression,
            final ViewingPart part,
            final String urlPrefix,
            final String subDocRef,
            final HttpServletResponse response
    ) throws E, IOException {
        if (part == ViewingPart.ORIGINAL) {
            // if View fails, this will still get overridden by text/html in the error handler
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            RequestUtils.setFilenameHeader(response, extractFilename(reference));
        } else {
            response.setContentType(MediaType.TEXT_HTML_VALUE);
        }

        ViewContentSecurityPolicy.addContentSecurityPolicy(response);
        final R request = viewRequestBuilderFactory.getObject()
                .documentReference(reference)
                .database(database)
                .highlightExpression(highlightExpression)
                .part(part)
                .urlPrefix(urlPrefix == null ? null : urlPrefix +
                        // viewserver doesn't propagate urlprefix to sub-sub-documents
                        "&urlPrefix=" + URLEncoder.encode(urlPrefix, StandardCharsets.UTF_8) + "&subDocRef=")
                .subDocRef(subDocRef)
                .build();
        viewServerService.viewDocument(request, response.getOutputStream());
    }

    protected void viewStaticContentPromotion(
            final String reference,
            final HttpServletResponse response
    ) throws IOException, E {
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        ViewContentSecurityPolicy.addContentSecurityPolicy(response);

        viewServerService.viewStaticContentPromotion(reference, response.getOutputStream());
    }
}
