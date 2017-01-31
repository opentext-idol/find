/*
 * Copyright 2015-2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.export;

import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.ErrorModelAndViewInfo;
import com.hp.autonomy.frontend.find.core.web.RequestMapper;
import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;

@RequestMapping(ExportController.EXPORT_PATH)
public abstract class ExportController<R extends QueryRequest<?>, E extends Exception> {
    static final String EXPORT_PATH = "/api/bi/export";
    static final String CSV_PATH = "/csv";
    static final String SELECTED_EXPORT_FIELDS_PARAM = "selectedFieldIds";
    static final String QUERY_REQUEST_PARAM = "queryRequest";
    private static final String EXPORT_FILE_NAME = "query-results";

    private final ExportService<R, E> exportService;
    private final RequestMapper<R> requestMapper;
    private final ControllerUtils controllerUtils;

    protected ExportController(final ExportService<R, E> exportService, final RequestMapper<R> requestMapper, final ControllerUtils controllerUtils) {
        this.exportService = exportService;
        this.requestMapper = requestMapper;
        this.controllerUtils = controllerUtils;
    }

    @RequestMapping(value = CSV_PATH, method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<byte[]> exportToCsv(
            @RequestParam(QUERY_REQUEST_PARAM) final String queryRequestJSON,
            // required = false to prevent Spring errors if the user asks for a CSV with no fields marked for export.
            // The UI should not allow the User to send a request for a CSV with nothing in it.
            @RequestParam(value = SELECTED_EXPORT_FIELDS_PARAM, required = false) final Collection<String> selectedFieldNames
    ) throws IOException, E {
        return export(queryRequestJSON, ExportFormat.CSV, selectedFieldNames);
    }

    private ResponseEntity<byte[]> export(final String queryRequestJSON, final ExportFormat exportFormat, final Collection<String> selectedFieldNames) throws IOException, E {
        final R queryRequest = requestMapper.parseQueryRequest(queryRequestJSON);

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exportService.export(outputStream, queryRequest, exportFormat, selectedFieldNames);
        final byte[] output = outputStream.toByteArray();

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(exportFormat.getMimeType()));
        final String fileName = EXPORT_FILE_NAME + FilenameUtils.EXTENSION_SEPARATOR + exportFormat.getExtension();
        headers.setContentDispositionFormData(fileName, fileName);

        return new ResponseEntity<>(output, headers, HttpStatus.OK);
    }

    //TODO improve to inform what went wrong with export, rather than generic just error 500.
    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleException(
            final Exception e,
            final HttpServletRequest request,
            final ServletResponse response
    ) {
        response.reset();

        return controllerUtils.buildErrorModelAndView(
                new ErrorModelAndViewInfo.Builder()
                        .setRequest(request)
                        .setMainMessageCode("error.internalServerErrorMain")
                        .setSubMessageCode("error.internalServerErrorSub")
                        .setSubMessageArguments(null)
                        .setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .setContactSupport(true)
                        .setException(e)
                        .build()
        );
    }
}
