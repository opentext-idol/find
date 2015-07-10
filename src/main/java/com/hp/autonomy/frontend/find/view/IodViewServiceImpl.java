package com.hp.autonomy.frontend.find.view;

/*
 * $Id: $
 *
 * Copyright (c) 2015, Autonomy Systems Ltd.
 *
 * Last modified by $Author: $ on $Date: $
 */

import com.hp.autonomy.hod.client.api.analysis.viewdocument.ViewDocumentRequestBuilder;
import com.hp.autonomy.hod.client.api.analysis.viewdocument.ViewDocumentService;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.api.textindex.query.content.GetContentRequestBuilder;
import com.hp.autonomy.hod.client.api.textindex.query.content.GetContentService;
import com.hp.autonomy.hod.client.api.textindex.query.search.Document;
import com.hp.autonomy.hod.client.api.textindex.query.search.Documents;
import com.hp.autonomy.hod.client.api.textindex.query.search.Print;
import com.hp.autonomy.hod.client.error.HodErrorException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private GetContentService<Documents> getContentService;

    @Autowired
    private ViewDocumentService viewDocumentService;

    private InputStream formatRawContent(final Document document) throws IOException {
        String result = "<h1>" + document.getTitle() + "</h1>";
        result += "<p>" + document.getContent() + "</p>";

        result = result.replace("\n", "<br>");

        return IOUtils.toInputStream(result, "UTF-8");
    }

    /**
     * Uses IOD GetContent and ViewDocument to return the content of a document.
     * @param outputStream  Stream to write the document to
     * @param documentReference  Document reference to load
     * @param indexes  IOD Indexes to search for the document reference in
     * @throws IOException
     * @throws HodErrorException  Thrown if document reference is invalid/not found in the indexes supplied
     */
    @Override
    public void viewDocument(final OutputStream outputStream, final String documentReference, final ResourceIdentifier indexes) throws IOException, HodErrorException {

        // Call GetContent with the document reference as we need details from the full document (e.g. URL)
        final GetContentRequestBuilder getContentParams = new GetContentRequestBuilder()
                .setPrint(Print.all);

        // An IodErrorException can be thrown here if the document isn't found.
        final Documents documents = getContentService.getContent(Collections.singletonList(documentReference), indexes, getContentParams);
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
        InputStream inputStream = null;

        try {
            try {
                final URL url = new URL(documentUrl);
                final URI uri = new URI(url.getProtocol(), url.getAuthority(), url.getPath(), url.getQuery(), null);
                final String encodedUrl = uri.toASCIIString();

                if (urlValidator.isValid(encodedUrl)) {
                    inputStream = viewDocumentService.viewUrl(encodedUrl, new ViewDocumentRequestBuilder());
                } else {
                    throw new URISyntaxException(encodedUrl, "Invalid URL");
                }
            } catch (final URISyntaxException | MalformedURLException | HodErrorException e) {
                // Fallback - URL was not valid or IOD failed, use raw document content from IOD instead
                inputStream = formatRawContent(document);
            }

            IOUtils.copy(inputStream, outputStream);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }
}
