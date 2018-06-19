package com.hp.autonomy.frontend.find.idol.export;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.configuration.ConfigFileService;
import com.hp.autonomy.frontend.find.core.export.service.ExportFormat;
import com.hp.autonomy.frontend.find.core.export.service.ExportServiceFactory;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.RequestMapper;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.frontend.reports.powerpoint.TemplateLoadException;
import com.hp.autonomy.searchcomponents.idol.search.IdolDocumentsService;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

import static com.hp.autonomy.frontend.find.core.export.ExportController.EXPORT_PATH;
import static com.hp.autonomy.frontend.find.idol.export.IdolPowerPointExportController.PPTX_PATH;

@Controller
@RequestMapping(EXPORT_PATH + PPTX_PATH)
public class IdolPowerPointExportController extends IdolExportController {
    public IdolPowerPointExportController(final RequestMapper<IdolQueryRequest> requestMapper,
                                          final ControllerUtils controllerUtils,
                                          final ExportServiceFactory<IdolQueryRequest, AciErrorException> exportServiceFactory,
                                          final IdolDocumentsService documentsService,
                                          final ConfigFileService<IdolFindConfig> configService) {
        super(requestMapper, controllerUtils, exportServiceFactory, documentsService, configService);
    }

    @Override
    protected ExportFormat getExportFormat() {
        return ExportFormat.PPTX;
    }

    @Override
    @RequestMapping(value = TOPIC_MAP_PATH, method = RequestMethod.POST)
    public ResponseEntity<byte[]> topicMap(@RequestParam(DATA_PARAM) final String rawData) throws IOException, TemplateLoadException, AciErrorException {
        return super.topicMap(rawData);
    }

    @Override
    @RequestMapping(value = SUNBURST_PATH, method = RequestMethod.POST)
    public ResponseEntity<byte[]> sunburst(@RequestParam(DATA_PARAM) final String rawData) throws IOException, TemplateLoadException, AciErrorException {
        return super.sunburst(rawData);
    }

    @Override
    @RequestMapping(value = TABLE_PATH, method = RequestMethod.POST)
    public ResponseEntity<byte[]> table(@RequestParam(DATA_PARAM) final String rawData,
                                        @RequestParam(TITLE_PARAM) final String title) throws IOException, TemplateLoadException, AciErrorException {
        return super.table(rawData, title);
    }

    @Override
    @RequestMapping(value = MAP_PATH, method = RequestMethod.POST)
    public ResponseEntity<byte[]> map(@RequestParam(DATA_PARAM) final String rawData,
                                      @RequestParam(TITLE_PARAM) final String title) throws IOException, TemplateLoadException, AciErrorException {
        return super.map(rawData, title);
    }

    @Override
    @RequestMapping(value = LIST_PATH, method = RequestMethod.POST)
    public ResponseEntity<byte[]> list(@RequestParam(DATA_PARAM) final String rawData,
                                       @RequestParam(RESULTS_PARAM) final String results,
                                       @RequestParam(SORT_BY_PARAM) final String sortBy) throws IOException, TemplateLoadException, AciErrorException {
        return super.list(rawData, results, sortBy);
    }

    @Override
    @RequestMapping(value = DATE_GRAPH_PATH, method = RequestMethod.POST)
    public ResponseEntity<byte[]> dateGraph(@RequestParam(DATA_PARAM) final String rawData) throws IOException, TemplateLoadException, AciErrorException {
        return super.dateGraph(rawData);
    }

    @Override
    @RequestMapping(value = REPORT_PATH, method = RequestMethod.POST)
    public ResponseEntity<byte[]> report(@RequestParam(DATA_PARAM) final String rawData,
                                         @RequestParam(value = MULTI_PAGE_PARAM, defaultValue = MULTI_PAGE_PARAM_DEFAULT) final boolean multiPage) throws IOException, TemplateLoadException, AciErrorException {
        return super.report(rawData, multiPage);
    }
}
