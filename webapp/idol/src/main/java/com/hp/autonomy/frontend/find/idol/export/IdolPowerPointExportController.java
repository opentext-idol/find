package com.hp.autonomy.frontend.find.idol.export;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.configuration.ConfigFileService;
import com.hp.autonomy.frontend.find.core.export.service.ExportFormat;
import com.hp.autonomy.frontend.find.core.export.service.ExportServiceFactory;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.RequestMapper;
import com.hp.autonomy.frontend.find.idol.api.PowerpointExportApi;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.searchcomponents.idol.search.IdolDocumentsService;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IdolPowerPointExportController extends IdolExportController implements PowerpointExportApi {
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
    public ResponseEntity<Resource> exportPptxTopicMap(final String data) {
        return super.topicMap(data);
    }

    @Override
    public ResponseEntity<Resource> exportPptxSunburst(final String data) {
        return super.sunburst(data);
    }

    @Override
    public ResponseEntity<Resource> exportPptxTable(final String data, final String title) {
        return super.table(data, title);
    }

    @Override
    public ResponseEntity<Resource> exportPptxMap(final String data, final String title) {
        return super.map(data, title);
    }

    @Override
    public ResponseEntity<Resource> exportPptxList(final String data, final String results, final String sortBy) {
        return super.list(data, results, sortBy);
    }

    @Override
    public ResponseEntity<Resource> exportPptxDateGraph(final String data) {
        return super.dateGraph(data);
    }

    @Override
    public ResponseEntity<Resource> exportPptxReport(final String data, final Boolean multiPage) {
        return super.report(data, multiPage);
    }
}
