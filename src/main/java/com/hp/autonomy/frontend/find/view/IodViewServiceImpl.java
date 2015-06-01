package com.hp.autonomy.frontend.find.view;

/*
 * $Id: $
 *
 * Copyright (c) 2015, Autonomy Systems Ltd.
 *
 * Last modified by $Author: $ on $Date: $
 */

import com.hp.autonomy.frontend.find.ApiKeyService;
import com.hp.autonomy.iod.client.api.formatconversion.ViewDocumentService;
import com.hp.autonomy.iod.client.api.search.Document;
import com.hp.autonomy.iod.client.api.search.Documents;
import com.hp.autonomy.iod.client.api.search.GetContentRequestBuilder;
import com.hp.autonomy.iod.client.api.search.GetContentService;
import com.hp.autonomy.iod.client.api.search.Print;
import com.hp.autonomy.iod.client.error.IodErrorException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import retrofit.client.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

@Service
public class IodViewServiceImpl implements IodViewService {

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private GetContentService getContentService;

    @Autowired
    private ViewDocumentService viewDocumentService;

    @Override
    public void viewDocument(final OutputStream outputStream, final String documentReference, final String indexes) throws IOException, IodErrorException {
        // call get content with document reference
        // if no document will throw IodErrorException
        // if document has a url, view it
        // if document has object store reference, view it
        // else show the text content
        final Map<String, Object> getContentParams = new GetContentRequestBuilder()
                .setPrint(Print.all)
                .build();

        final Documents documents = getContentService.getContent(apiKeyService.getApiKey(), Collections.singletonList(documentReference), indexes, getContentParams);
        final Document document = documents.getDocuments().get(0);

        final Map<String, Object> fields = document.getFields();
        String documentUrl = (String) fields.get("url");

        final Response response;
        InputStream inputStream;

        if (documentUrl == null) {
            documentUrl = document.getReference();
        }

        final UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_2_SLASHES);

        try {
            final URL url = new URL(documentUrl);
            final URI uri = new URI(url.getProtocol(), url.getAuthority(), url.getPath(), url.getQuery(), null);
            final String encodedUrl = uri.toASCIIString();

            if (urlValidator.isValid(encodedUrl)) {
                response = viewDocumentService.viewUrl(apiKeyService.getApiKey(), encodedUrl, null);
                inputStream = response.getBody().in();
            } else {
                throw new URISyntaxException(encodedUrl, "Invalid URL");
            }
        } catch (URISyntaxException | MalformedURLException e) {
            inputStream = IOUtils.toInputStream(document.getContent(), "UTF-8");
        }

        IOUtils.copy(inputStream, outputStream);

        inputStream.close();
    }
}
