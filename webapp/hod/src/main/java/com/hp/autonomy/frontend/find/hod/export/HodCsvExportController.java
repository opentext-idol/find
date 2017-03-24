package com.hp.autonomy.frontend.find.hod.export;

import com.hp.autonomy.frontend.find.core.export.service.ExportFormat;
import com.hp.autonomy.frontend.find.core.export.service.ExportServiceFactory;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.RequestMapper;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.hod.search.HodDocumentsService;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.Collection;

import static com.hp.autonomy.frontend.find.core.export.ExportController.EXPORT_PATH;

@Controller
@RequestMapping(EXPORT_PATH)
public class HodCsvExportController extends HodExportController {
    public HodCsvExportController(final RequestMapper<HodQueryRequest> requestMapper,
                                  final ControllerUtils controllerUtils,
                                  final ExportServiceFactory<HodQueryRequest, HodErrorException> exportServiceFactory,
                                  final HodDocumentsService documentsService) {
        super(requestMapper, controllerUtils, exportServiceFactory, documentsService);
    }

    @Override
    protected ExportFormat getExportFormat() {
        return ExportFormat.CSV;
    }

    @Override
    @RequestMapping(value = CSV_PATH, method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<byte[]> exportQueryResults(
            @RequestParam(QUERY_REQUEST_PARAM) final String queryRequestJSON,
            // required = false to prevent Spring errors if the user asks for a CSV with no fields marked for exportQueryResults.
            // The UI should not allow the User to send a request for a CSV with nothing in it.
            @RequestParam(value = SELECTED_EXPORT_FIELDS_PARAM, required = false) final Collection<String> selectedFieldNames
    ) throws IOException, HodErrorException {
        return super.exportQueryResults(queryRequestJSON, selectedFieldNames);
    }
}
