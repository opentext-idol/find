package com.hp.autonomy.frontend.find.core.export.service;

import com.hp.autonomy.frontend.reports.powerpoint.PowerPointService;
import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings({"SpringJavaAutowiredMembersInspection", "unused"})
@RunWith(SpringRunner.class)
public abstract class ExportServiceFactoryTest<R extends QueryRequest<?>, E extends Exception> {
    @MockBean
    private PowerPointService powerPointService;
    @Autowired
    private ExportServiceFactory<R, E> exportServiceFactory;

    @Test
    public void csvExportWiring() {
        assertTrue(exportServiceFactory.getPlatformDataExportService(ExportFormat.CSV).isPresent());
        assertFalse(exportServiceFactory.getVisualDataExportService(ExportFormat.CSV).isPresent());
    }

    @Test
    public void pptxExportWiring() {
        assertFalse(exportServiceFactory.getPlatformDataExportService(ExportFormat.PPTX).isPresent());
        assertTrue(exportServiceFactory.getVisualDataExportService(ExportFormat.PPTX).isPresent());
    }
}
