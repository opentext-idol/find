/*
 * Copyright 2015-2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.configuration.FindConfig;
import com.hp.autonomy.frontend.find.core.configuration.PowerPointConfig;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.ErrorModelAndViewInfo;
import com.hp.autonomy.frontend.find.core.web.RequestMapper;
import com.hp.autonomy.frontend.reports.powerpoint.PowerPointService;
import com.hp.autonomy.frontend.reports.powerpoint.PowerPointServiceImpl;
import com.hp.autonomy.frontend.reports.powerpoint.TemplateLoadException;
import com.hp.autonomy.frontend.reports.powerpoint.TemplateSettings;
import com.hp.autonomy.frontend.reports.powerpoint.TemplateSettingsSource;
import com.hp.autonomy.frontend.reports.powerpoint.TemplateSource;
import com.hp.autonomy.frontend.reports.powerpoint.dto.DategraphData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.ListData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.MapData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.ReportData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.SunburstData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.TableData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.TopicMapData;
import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import java.io.FileInputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.springframework.http.HttpEntity;
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
import java.io.OutputStream;
import java.util.Collection;

@RequestMapping(ExportController.EXPORT_PATH)
public abstract class ExportController<R extends QueryRequest<?>, E extends Exception> {
    static final String EXPORT_PATH = "/api/bi/export";
    static final String CSV_PATH = "/csv";
    static final String PPT_TOPICMAP_PATH = "/ppt/topicmap";
    static final String PPT_SUNBURST_PATH = "/ppt/sunburst";
    static final String PPT_TABLE_PATH = "/ppt/table";
    static final String PPT_MAP_PATH = "/ppt/map";
    static final String PPT_LIST_PATH = "/ppt/list";
    static final String PPT_DATEGRAPH_PATH = "/ppt/dategraph";
    static final String PPT_REPORT_PATH = "/ppt/report";
    static final String SELECTED_EXPORT_FIELDS_PARAM = "selectedFieldIds";
    static final String QUERY_REQUEST_PARAM = "queryRequest";
    private static final String EXPORT_FILE_NAME = "query-results";
    private final RequestMapper<R> requestMapper;
    private final ControllerUtils controllerUtils;
    private final ObjectMapper objectMapper;

    private final PowerPointService pptService;

    protected ExportController(final RequestMapper<R> requestMapper,
                               final ControllerUtils controllerUtils,
                               final ObjectMapper objectMapper,
                               final ConfigService<? extends FindConfig> configService) {
        this.requestMapper = requestMapper;
        this.controllerUtils = controllerUtils;
        this.objectMapper = objectMapper;

        this.pptService = new PowerPointServiceImpl(() -> {
            final PowerPointConfig powerPoint = configService.getConfig().getPowerPoint();

            if (powerPoint != null) {
                final String template = powerPoint.getTemplateFile();

                if (StringUtils.isNotBlank(template)) {
                    return new FileInputStream(template);
                }
            }

            return TemplateSource.DEFAULT.getInputStream();
        }, () -> {
            final PowerPointConfig powerPoint = configService.getConfig().getPowerPoint();

            return powerPoint == null ? TemplateSettingsSource.DEFAULT.getSettings() : new TemplateSettings(powerPoint.getAnchor());
        });
    }

    @RequestMapping(value = CSV_PATH, method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<byte[]> exportToCsv(
            @RequestParam(QUERY_REQUEST_PARAM) final String queryRequestJSON,
            // required = false to prevent Spring errors if the user asks for a CSV with no fields marked for export.
            // The UI should not allow the User to send a request for a CSV with nothing in it.
            @RequestParam(value = SELECTED_EXPORT_FIELDS_PARAM, required = false) final Collection<String> selectedFieldNames
    ) throws IOException, E {
        final ExportFormat exportFormat = ExportFormat.CSV;
        final R queryRequest = requestMapper.parseQueryRequest(queryRequestJSON);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        export(outputStream, queryRequest, exportFormat, selectedFieldNames);
        return outputStreamToResponseEntity(exportFormat, outputStream);
    }

    protected abstract void export(final OutputStream outputStream,
                                   final R queryRequest,
                                   final ExportFormat exportFormat,
                                   final Collection<String> selectedFieldNames) throws E, IOException;

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

    private ResponseEntity<byte[]> outputStreamToResponseEntity(final ExportFormat exportFormat, final ByteArrayOutputStream outputStream) {
        final byte[] output = outputStream.toByteArray();

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(exportFormat.getMimeType()));
        final String fileName = EXPORT_FILE_NAME + FilenameUtils.EXTENSION_SEPARATOR + exportFormat.getExtension();
        headers.setContentDispositionFormData(fileName, fileName);

        return new ResponseEntity<>(output, headers, HttpStatus.OK);
    }

    @RequestMapping(value = PPT_TOPICMAP_PATH, method = RequestMethod.POST)
    public HttpEntity<byte[]> topicmap(
            @RequestParam("data") final String topicMapStr
    ) throws IOException, TemplateLoadException {
        final TopicMapData data = objectMapper.readValue(topicMapStr, TopicMapData.class);
        return writePPT(pptService.topicmap(data), "topicmap.pptx");
    }

    private HttpEntity<byte[]> writePPT(final XMLSlideShow ppt, final String filename) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ppt.write(baos);
        ppt.close();

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.presentationml.presentation"));
        headers.set("Content-Disposition", "inline; filename=" + filename);
        return new HttpEntity<>(baos.toByteArray(), headers);
    }

    @RequestMapping(value = PPT_SUNBURST_PATH, method = RequestMethod.POST)
    public HttpEntity<byte[]> sunburst(
            @RequestParam("data") final String dataStr
    ) throws IOException, TemplateLoadException {
        final SunburstData data = objectMapper.readValue(dataStr, SunburstData.class);

        final XMLSlideShow ppt = pptService.sunburst(data);

        return writePPT(ppt, "sunburst.pptx");
    }

    @RequestMapping(value = PPT_TABLE_PATH, method = RequestMethod.POST)
    public HttpEntity<byte[]> table(
            @RequestParam("title") final String title,
            @RequestParam("data") final String dataStr

    ) throws IOException, TemplateLoadException {
        final TableData tableData = objectMapper.readValue(dataStr, TableData.class);

        final XMLSlideShow ppt = pptService.table(tableData, title);

        return writePPT(ppt, "table.pptx");
    }

    @RequestMapping(value = PPT_MAP_PATH, method = RequestMethod.POST)
    public HttpEntity<byte[]> map(
            @RequestParam("title") final String title,
            @RequestParam("data") final String markerStr
    ) throws IOException, TemplateLoadException {
        final MapData map = objectMapper.readValue(markerStr, MapData.class);

        final XMLSlideShow ppt = pptService.map(map, title);

        return writePPT(ppt, "map.pptx");
    }

    @RequestMapping(value = PPT_LIST_PATH, method = RequestMethod.POST)
    public HttpEntity<byte[]> list(
            @RequestParam("results") final String results,
            @RequestParam("sortBy") final String sortBy,
            @RequestParam("data") final String docsStr
    ) throws IOException, TemplateLoadException {
        final ListData documentList = objectMapper.readValue(docsStr, ListData.class);

        final XMLSlideShow ppt = pptService.list(documentList, results, sortBy);

        return writePPT(ppt, "list.pptx");
    }

    @RequestMapping(value = PPT_DATEGRAPH_PATH, method = RequestMethod.POST)
    public HttpEntity<byte[]> graph(
            @RequestParam("data") final String dataStr
    ) throws IOException, TemplateLoadException {
        final DategraphData data = objectMapper.readValue(dataStr, DategraphData.class);

        final XMLSlideShow ppt = pptService.graph(data);

        return writePPT(ppt, "dategraph.pptx");
    }

    @RequestMapping(value = PPT_REPORT_PATH, method = RequestMethod.POST)
    public HttpEntity<byte[]> report(
            @RequestParam("data") final String dataStr
    ) throws IOException, TemplateLoadException {
        final ReportData report = objectMapper.readValue(dataStr, ReportData.class);

        final XMLSlideShow ppt = pptService.report(report);

        return writePPT(ppt, "report.pptx");
    }

}