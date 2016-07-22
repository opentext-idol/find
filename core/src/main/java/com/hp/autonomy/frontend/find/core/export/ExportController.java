/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.export;

import com.hp.autonomy.frontend.find.core.web.RequestMapper;
import com.hp.autonomy.searchcomponents.core.search.SearchRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletResponse;
import java.io.IOException;
import java.io.Serializable;

@RequestMapping(ExportController.EXPORT_PATH)
public abstract class ExportController<S extends Serializable, E extends Exception> {
    static final String EXPORT_PATH = "/api/bi/export";
    static final String CSV_PATH = "/csv";
    static final String CSV_MIME_TYPE = "text/csv";

    private final ExportService<S, E> exportService;
    private final RequestMapper<S> requestMapper;

    protected ExportController(final ExportService<S, E> exportService, final RequestMapper<S> requestMapper) {
        this.exportService = exportService;
        this.requestMapper = requestMapper;
    }

    @RequestMapping(CSV_PATH)
    public void exportToCsv(@RequestBody final String json, final ServletResponse response) throws IOException, E {
        final SearchRequest<S> searchRequest = requestMapper.parseSearchRequest(json);

        response.setContentType(CSV_MIME_TYPE);
        exportService.export(response.getOutputStream(), searchRequest, ExportFormat.CSV);
        response.flushBuffer();
    }
}
