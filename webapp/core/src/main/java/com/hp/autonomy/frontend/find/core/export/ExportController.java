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

package com.hp.autonomy.frontend.find.core.export;

import com.hp.autonomy.frontend.find.core.export.service.ExportFormat;
import com.hp.autonomy.frontend.find.core.export.service.ExportServiceFactory;
import com.hp.autonomy.frontend.find.core.export.service.VisualDataExportService;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.ErrorModelAndViewInfo;
import com.hp.autonomy.frontend.find.core.web.RequestMapper;
import com.hp.autonomy.frontend.reports.powerpoint.TemplateLoadException;
import com.hp.autonomy.frontend.reports.powerpoint.dto.*;
import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Collection;

public abstract class ExportController<R extends QueryRequest<?>, E extends Exception> {
    private static final String QUERY_RESULT_EXPORT_FILE_NAME = "query-results";
    private static final String TOPIC_MAP_EXPORT_FILE_NAME = "topic-map";
    private static final String SUNBURST_EXPORT_FILE_NAME = "sunburst";
    private static final String TABLE_EXPORT_FILE_NAME = "table";
    private static final String MAP_EXPORT_FILE_NAME = "map";
    private static final String LIST_EXPORT_FILE_NAME = "list";
    private static final String DATE_GRAPH_EXPORT_FILE_NAME = "date-graph";
    private static final String REPORT_EXPORT_FILE_NAME = "report";

    protected final ExportServiceFactory<R, E> exportServiceFactory;
    private final RequestMapper<R> requestMapper;
    private final ControllerUtils controllerUtils;
    private final ObjectMapper objectMapper = new ObjectMapper();

    protected ExportController(final RequestMapper<R> requestMapper,
                               final ControllerUtils controllerUtils,
                               final ExportServiceFactory<R, E> exportServiceFactory) {
        this.requestMapper = requestMapper;
        this.controllerUtils = controllerUtils;
        this.exportServiceFactory = exportServiceFactory;
    }

    protected abstract ExportFormat getExportFormat();

    protected ResponseEntity<Resource> exportQueryResults(
            final String queryRequestJSON,
            final Collection<String> selectedFieldNames
    ) throws E {
        final R queryRequest;
        try {
            queryRequest = requestMapper.parseQueryRequest(queryRequestJSON);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
        return writeDataToOutputStream(outputStream -> export(outputStream, queryRequest, selectedFieldNames), QUERY_RESULT_EXPORT_FILE_NAME);
    }

    protected abstract void export(final OutputStream outputStream,
                                   final R queryRequest,
                                   final Collection<String> selectedFieldNames) throws E, IOException;

    //TODO improve to inform what went wrong with exportQueryResults, rather than generic just error 500.
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

    protected ResponseEntity<Resource> topicMap(
            final String rawData
    ) throws E {
        final TopicMapData data = objectMapper.readValue(rawData, TopicMapData.class);
        return writeDataToOutputStream(outputStream -> getVisualDataExportService().topicMap(outputStream, data), TOPIC_MAP_EXPORT_FILE_NAME);
    }

    protected ResponseEntity<Resource> sunburst(
            final String rawData
    ) throws E {
        final SunburstData data = objectMapper.readValue(rawData, SunburstData.class);
        return writeDataToOutputStream(outputStream -> getVisualDataExportService().sunburst(outputStream, data), SUNBURST_EXPORT_FILE_NAME);
    }

    protected ResponseEntity<Resource> table(
            final String rawData,
            final String title
    ) throws E {
        final TableData data = objectMapper.readValue(rawData, TableData.class);
        return writeDataToOutputStream(outputStream -> getVisualDataExportService().table(outputStream, data, title), TABLE_EXPORT_FILE_NAME);
    }

    protected ResponseEntity<Resource> map(
            final String rawData,
            final String title
    ) throws E {
        final MapData data = objectMapper.readValue(rawData, MapData.class);
        return writeDataToOutputStream(outputStream -> getVisualDataExportService().map(outputStream, data, title), MAP_EXPORT_FILE_NAME);
    }

    protected ResponseEntity<Resource> list(
            final String rawData,
            final String results,
            final String sortBy
    ) throws E {
        final ListData data = objectMapper.readValue(rawData, ListData.class);
        return writeDataToOutputStream(outputStream -> getVisualDataExportService().list(outputStream, data, results, sortBy), LIST_EXPORT_FILE_NAME);
    }

    protected ResponseEntity<Resource> dateGraph(
            final String rawData
    ) throws E {
        final DategraphData data = objectMapper.readValue(rawData, DategraphData.class);
        return writeDataToOutputStream(outputStream -> getVisualDataExportService().dateGraph(outputStream, data), DATE_GRAPH_EXPORT_FILE_NAME);
    }

    protected ResponseEntity<Resource> report(
            final String rawData,
            final boolean multiPage
    ) throws E {
        final ReportData data = objectMapper.readValue(rawData, ReportData.class);
        return writeDataToOutputStream(outputStream -> getVisualDataExportService().report(outputStream, data, multiPage), REPORT_EXPORT_FILE_NAME);
    }

    private VisualDataExportService getVisualDataExportService() {
        return exportServiceFactory.getVisualDataExportService(getExportFormat()).orElseThrow(() -> new UnsupportedOperationException("Visual data export operation not supported for format: " + getExportFormat().name()));
    }

    private ResponseEntity<Resource> writeDataToOutputStream(final Operation<E> operation,
                                                             final String fileNameWithoutExtension) throws E {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            operation.accept(outputStream);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        } catch (final TemplateLoadException e) {
            throw new ExportRenderingException(e);
        }
        final byte[] output = outputStream.toByteArray();

        final HttpHeaders headers = new HttpHeaders();
        final ExportFormat exportFormat = getExportFormat();
        headers.setContentType(MediaType.parseMediaType(exportFormat.getMimeType()));
        final String fileName = fileNameWithoutExtension + FilenameUtils.EXTENSION_SEPARATOR + exportFormat.getExtension();
        headers.setContentDispositionFormData(fileName, fileName);

        return new ResponseEntity<>(new ByteArrayResource(output), headers, HttpStatus.OK);
    }

    @FunctionalInterface
    private interface Operation<E extends Exception> {
        void accept(OutputStream outputStream) throws IOException, TemplateLoadException, E;
    }
}
