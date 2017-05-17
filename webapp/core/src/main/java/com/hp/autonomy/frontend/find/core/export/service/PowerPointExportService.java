package com.hp.autonomy.frontend.find.core.export.service;

import com.hp.autonomy.frontend.reports.powerpoint.PowerPointService;
import com.hp.autonomy.frontend.reports.powerpoint.TemplateLoadException;
import com.hp.autonomy.frontend.reports.powerpoint.dto.DategraphData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.ListData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.MapData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.ReportData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.SunburstData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.TableData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.TopicMapData;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;

@SuppressWarnings("resource")
@Component
public class PowerPointExportService implements VisualDataExportService {
    private final PowerPointService powerPointService;

    @Autowired
    public PowerPointExportService(final PowerPointService powerPointService) {
        this.powerPointService = powerPointService;
    }

    @Override
    public void topicMap(final OutputStream outputStream, final TopicMapData data) throws IOException {
        writeToStreamHandlingException(outputStream, () -> powerPointService.topicmap(data));
    }
    
    @Override
    public void sunburst(final OutputStream outputStream, final SunburstData data) throws IOException {
        writeToStreamHandlingException(outputStream, () -> powerPointService.sunburst(data));
    }

    @Override
    public void table(final OutputStream outputStream, final TableData data, final String title) throws IOException {
        writeToStreamHandlingException(outputStream, () -> powerPointService.table(data, title));
    }

    @Override
    public void map(final OutputStream outputStream, final MapData data, final String title) throws IOException {
        writeToStreamHandlingException(outputStream, () -> powerPointService.map(data, title));
    }

    @Override
    public void list(final OutputStream outputStream, final ListData data, final String results, final String sortBy) throws IOException {
        writeToStreamHandlingException(outputStream, () -> powerPointService.list(data, results, sortBy));
    }

    @Override
    public void dateGraph(final OutputStream outputStream, final DategraphData data) throws IOException {
        writeToStreamHandlingException(outputStream, () -> powerPointService.graph(data));
    }

    @Override
    public void report(final OutputStream outputStream, final ReportData data, final boolean multiPage) throws IOException {
        writeToStreamHandlingException(outputStream, () -> powerPointService.report(data, multiPage));
    }

    @Override
    public Collection<ExportFormat> handlesFormats() {
        return Collections.singleton(ExportFormat.PPTX);
    }

    private void writeToStreamHandlingException(final OutputStream outputStream, final PowerPointOperation operation) throws IOException {
        try {
            writeToStream(outputStream, operation);
        } catch (final TemplateLoadException e) {
            throw new PowerPointExportException(e);
        }
    }

    private void writeToStream(final OutputStream outputStream, final PowerPointOperation operation) throws IOException, TemplateLoadException {
        try (final XMLSlideShow slideShow = operation.apply()) {
            slideShow.write(outputStream);
        }
    }
    
    @FunctionalInterface
    private interface PowerPointOperation {
        XMLSlideShow apply() throws TemplateLoadException;
    }
    
    static class PowerPointExportException extends RuntimeException {
        private static final long serialVersionUID = 7068580207138102746L;

        PowerPointExportException(final Throwable cause) {
            super(cause);
        }
    }
}
