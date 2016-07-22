/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.export;

import com.hp.autonomy.frontend.find.core.web.RequestMapper;
import com.hp.autonomy.searchcomponents.core.search.SearchRequest;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

@RequestMapping(ExportController.EXPORT_PATH)
public abstract class ExportController<S extends Serializable, E extends Exception> {
    static final String EXPORT_PATH = "/api/bi/export";
    static final String CSV_PATH = "/csv";
    static final String POST_DATA_PARAM = "postData";
    private static final String EXPORT_FILE_NAME = "query-results";

    private final ExportService<S, E> exportService;
    private final RequestMapper<S> requestMapper;

    protected ExportController(final ExportService<S, E> exportService, final RequestMapper<S> requestMapper) {
        this.exportService = exportService;
        this.requestMapper = requestMapper;
    }

    @RequestMapping(value = CSV_PATH, method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<byte[]> exportToCsv(@RequestParam(POST_DATA_PARAM) final String json) throws IOException, E {
        return export(json, ExportFormat.CSV);
    }

    private ResponseEntity<byte[]> export(final String json, final ExportFormat exportFormat) throws IOException, E {
        final SearchRequest<S> searchRequest = requestMapper.parseSearchRequest(json);

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exportService.export(outputStream, searchRequest, ExportFormat.CSV);
        final byte[] output = outputStream.toByteArray();

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(exportFormat.getMimeType()));
        final String fileName = EXPORT_FILE_NAME + FilenameUtils.EXTENSION_SEPARATOR + exportFormat.getExtension();
        headers.setContentDispositionFormData(fileName, fileName);

        return new ResponseEntity<>(output, headers, HttpStatus.OK);
    }
}
