package com.hp.autonomy.frontend.find.core.export.service;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.beanconfiguration.ExportConfiguration;
import com.hp.autonomy.frontend.find.core.configuration.FindConfig;
import com.hp.autonomy.frontend.reports.powerpoint.dto.DategraphData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.ListData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.MapData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.ReportData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.SunburstData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.TableData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.TopicMapData;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ExportConfiguration.class, PowerPointExportService.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class PowerPointExportServiceIT {
    @MockBean
    private ConfigService<? extends FindConfig<?, ?>> configService;
    @Autowired
    private VisualDataExportService powerPointExportService;

    @SuppressWarnings("rawtypes")
    @Before
    public void setUp() throws Exception {
        when(((ConfigService) configService).getConfig()).thenReturn(mock(FindConfig.class));
    }

    @Ignore
    @Test
    public void topicMap() throws Exception {
        //TODO determine good test data
        simpleDataTest(outputStream -> powerPointExportService.topicMap(outputStream, new TopicMapData()));
    }

    @Ignore
    @Test
    public void sunburst() throws Exception {
        //TODO determine good test data
        simpleDataTest(outputStream -> powerPointExportService.sunburst(outputStream, new SunburstData()));
    }

    @Ignore
    @Test
    public void table() throws Exception {
        //TODO determine good test data
        simpleDataTest(outputStream -> powerPointExportService.table(outputStream, new TableData(), "Test Table"));
    }

    @Ignore
    @Test
    public void map() throws Exception {
        //TODO determine good test data
        simpleDataTest(outputStream -> powerPointExportService.map(outputStream, new MapData(), "Test Map"));
    }

    @Ignore
    @Test
    public void list() throws Exception {
        //TODO determine good test data
        simpleDataTest(outputStream -> powerPointExportService.list(outputStream, new ListData(), "", ""));
    }

    @Ignore
    @Test
    public void dateGraph() throws Exception {
        //TODO determine good test data
        simpleDataTest(outputStream -> powerPointExportService.dateGraph(outputStream, new DategraphData()));
    }

    @Ignore
    @Test
    public void report() throws Exception {
        //TODO determine good test data
        simpleDataTest(outputStream -> powerPointExportService.report(outputStream, new ReportData(), false));
    }

    private void simpleDataTest(final Operation operation) throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        operation.accept(outputStream);
        assertNotNull(outputStream.toByteArray());
    }

    @FunctionalInterface
    private interface Operation {
        void accept(OutputStream outputStream) throws IOException;
    }
}
