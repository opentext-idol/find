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
import com.hp.autonomy.iod.client.error.IodErrorCode;
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
import java.util.List;
import java.util.Map;

@Service
public class IodViewServiceImpl implements IodViewService {

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private GetContentService getContentService;

    @Autowired
    private ViewDocumentService viewDocumentService;

    /**
     * Uses IOD GetContent and ViewDocument to return the content of a document.
     * @param outputStream  Stream to write the document to
     * @param documentReference  Document reference to load
     * @param indexes  IOD Indexes to search for the document reference in
     * @throws IOException
     * @throws IodErrorException  Thrown if document reference is invalid/not found in the indexes supplied
     */
    @Override
    public void viewDocument(final OutputStream outputStream, final String documentReference, final String indexes) throws IOException, IodErrorException {

        // Call GetContent with the document reference as we need details from the full document (e.g. URL)
        final Map<String, Object> getContentParams = new GetContentRequestBuilder()
                .setPrint(Print.all)
                .build();

        // An IodErrorException can be thrown here if the document isn't found.
        final Documents documents = getContentService.getContent(apiKeyService.getApiKey(), Collections.singletonList(documentReference), indexes, getContentParams);
        final Document document = documents.getDocuments().get(0);

        // Check for a URL field on the document
        final Map<String, Object> fields = document.getFields();
        final Object urlField = fields.get("url");
        final String documentUrl;

        if(urlField instanceof List) {
            documentUrl = ((List<?>) urlField).get(0).toString();
        }
        else {
            // If there isn't a URL field, use the document reference - this is often actually a URL
            documentUrl = document.getReference();
        }

        final UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_2_SLASHES);

        // Attempt to load the URL
        InputStream inputStream;
        try {
            final URL url = new URL(documentUrl);
            final URI uri = new URI(url.getProtocol(), url.getAuthority(), url.getPath(), url.getQuery(), null);
            final String encodedUrl = uri.toASCIIString();

            if (urlValidator.isValid(encodedUrl)) {
                final Response response = viewDocumentService.viewUrl(encodedUrl, null);
                inputStream = response.getBody().in();
            } else {
                throw new URISyntaxException(encodedUrl, "Invalid URL");
            }
        } catch (final URISyntaxException | MalformedURLException | IodErrorException e) {
            // Fallback - URL was not valid or IOD failed, use raw document content from IOD instead
            inputStream = IOUtils.toInputStream(document.getContent(), "UTF-8");
        }

        IOUtils.copy(inputStream, outputStream);
        inputStream.close();
    }
}
