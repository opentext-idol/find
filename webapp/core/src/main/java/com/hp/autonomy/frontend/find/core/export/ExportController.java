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

package com.hp.autonomy.frontend.find.core.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.find.core.export.service.ExportFormat;
import com.hp.autonomy.frontend.find.core.export.service.ExportServiceFactory;
import com.hp.autonomy.frontend.find.core.export.service.VisualDataExportService;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.ErrorModelAndViewInfo;
import com.hp.autonomy.frontend.find.core.web.RequestMapper;
import com.hp.autonomy.frontend.reports.powerpoint.TemplateLoadException;
import com.hp.autonomy.frontend.reports.powerpoint.dto.DategraphData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.ListData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.MapData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.ReportData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.SunburstData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.TableData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.TopicMapData;
import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

public abstract class ExportController<R extends QueryRequest<?>, E extends Exception> {
    public static final String EXPORT_PATH = "/api/bi/export";
    public static final String PPTX_PATH = "/pptx";
    protected static final String CSV_PATH = "/csv";
    protected static final String TOPIC_MAP_PATH = "/topic-map";
    protected static final String SUNBURST_PATH = "/sunburst";
    protected static final String TABLE_PATH = "/table";
    protected static final String MAP_PATH = "/map";
    protected static final String LIST_PATH = "/list";
    protected static final String DATE_GRAPH_PATH = "/date-graph";
    protected static final String REPORT_PATH = "/report";

    protected static final String SELECTED_EXPORT_FIELDS_PARAM = "selectedFieldIds";
    protected static final String QUERY_REQUEST_PARAM = "queryRequest";
    protected static final String DATA_PARAM = "data";
    protected static final String TITLE_PARAM = "data";
    protected static final String RESULTS_PARAM = "results";
    protected static final String SORT_BY_PARAM = "sortBy";
    protected static final String MULTI_PAGE_PARAM = "multiPage";
    protected static final String MULTI_PAGE_PARAM_DEFAULT = "false";

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

    @ResponseBody
    public ResponseEntity<byte[]> exportQueryResults(
            @RequestParam(QUERY_REQUEST_PARAM) final String queryRequestJSON,
            // required = false to prevent Spring errors if the user asks for a CSV with no fields marked for exportQueryResults.
            // The UI should not allow the User to send a request for a CSV with nothing in it.
            @RequestParam(value = SELECTED_EXPORT_FIELDS_PARAM, required = false) final Collection<String> selectedFieldNames
    ) throws IOException, E {
        final R queryRequest = requestMapper.parseQueryRequest(queryRequestJSON);
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

    public ResponseEntity<byte[]> topicMap(
            @RequestParam(DATA_PARAM) final String rawData
    ) throws IOException, TemplateLoadException, E {
        final TopicMapData data = objectMapper.readValue(rawData, TopicMapData.class);
        return writeDataToOutputStream(outputStream -> getVisualDataExportService().topicMap(outputStream, data), TOPIC_MAP_EXPORT_FILE_NAME);
    }

    public ResponseEntity<byte[]> sunburst(
            @RequestParam(DATA_PARAM) final String rawData
    ) throws IOException, TemplateLoadException, E {
        final SunburstData data = objectMapper.readValue(rawData, SunburstData.class);
        return writeDataToOutputStream(outputStream -> getVisualDataExportService().sunburst(outputStream, data), SUNBURST_EXPORT_FILE_NAME);
    }

    public ResponseEntity<byte[]> table(
            @RequestParam(DATA_PARAM) final String rawData,
            @RequestParam(TITLE_PARAM) final String title
    ) throws IOException, TemplateLoadException, E {
        final TableData data = objectMapper.readValue(rawData, TableData.class);
        return writeDataToOutputStream(outputStream -> getVisualDataExportService().table(outputStream, data, title), TABLE_EXPORT_FILE_NAME);
    }

    public ResponseEntity<byte[]> map(
            @RequestParam(DATA_PARAM) final String rawData,
            @RequestParam(TITLE_PARAM) final String title
    ) throws IOException, TemplateLoadException, E {
        final MapData data = objectMapper.readValue(rawData, MapData.class);
        return writeDataToOutputStream(outputStream -> getVisualDataExportService().map(outputStream, data, title), MAP_EXPORT_FILE_NAME);
    }

    public ResponseEntity<byte[]> list(
            @RequestParam(DATA_PARAM) final String rawData,
            @RequestParam(RESULTS_PARAM) final String results,
            @RequestParam(SORT_BY_PARAM) final String sortBy
    ) throws IOException, TemplateLoadException, E {
        final ListData data = objectMapper.readValue(rawData, ListData.class);
        return writeDataToOutputStream(outputStream -> getVisualDataExportService().list(outputStream, data, results, sortBy), LIST_EXPORT_FILE_NAME);
    }

    public ResponseEntity<byte[]> dateGraph(
            @RequestParam(DATA_PARAM) final String rawData
    ) throws IOException, TemplateLoadException, E {
        final DategraphData data = objectMapper.readValue(rawData, DategraphData.class);
        return writeDataToOutputStream(outputStream -> getVisualDataExportService().dateGraph(outputStream, data), DATE_GRAPH_EXPORT_FILE_NAME);
    }

    public ResponseEntity<byte[]> report(
            @RequestParam(DATA_PARAM) final String rawData,
            @RequestParam(value = MULTI_PAGE_PARAM, defaultValue = "false") final boolean multiPage
    ) throws IOException, TemplateLoadException, E {
        final ReportData data = objectMapper.readValue(rawData, ReportData.class);
        return writeDataToOutputStream(outputStream -> getVisualDataExportService().report(outputStream, data, multiPage), REPORT_EXPORT_FILE_NAME);
    }

    private VisualDataExportService getVisualDataExportService() {
        return exportServiceFactory.getVisualDataExportService(getExportFormat()).orElseThrow(() -> new UnsupportedOperationException("Visual data export operation not supported for format: " + getExportFormat().name()));
    }

    private ResponseEntity<byte[]> writeDataToOutputStream(final Operation<E> operation,
                                                           final String fileNameWithoutExtension) throws IOException, E {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        operation.accept(outputStream);
        final byte[] output = outputStream.toByteArray();

        final HttpHeaders headers = new HttpHeaders();
        final ExportFormat exportFormat = getExportFormat();
        headers.setContentType(MediaType.parseMediaType(exportFormat.getMimeType()));
        final String fileName = fileNameWithoutExtension + FilenameUtils.EXTENSION_SEPARATOR + exportFormat.getExtension();
        headers.setContentDispositionFormData(fileName, fileName);

        return new ResponseEntity<>(output, headers, HttpStatus.OK);
    }

    @FunctionalInterface
    private interface Operation<E extends Exception> {
        void accept(OutputStream outputStream) throws IOException, E;
    }
}
