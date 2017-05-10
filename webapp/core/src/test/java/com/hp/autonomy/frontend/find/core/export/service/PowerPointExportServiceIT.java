package com.hp.autonomy.frontend.find.core.export.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    private static final String TOPIC_MAP_DATA = "/com/hp/autonomy/frontend/find/core/export/topic-map-data.json";
    private static final String SUNBURST_DATA = "/com/hp/autonomy/frontend/find/core/export/sunburst-data.json";
    private static final String MAP_DATA = "/com/hp/autonomy/frontend/find/core/export/map-data.json";
    private static final String LIST_DATA = "/com/hp/autonomy/frontend/find/core/export/list-data.json";
    private static final String DATE_GRAPH_DATA = "/com/hp/autonomy/frontend/find/core/export/date-graph-data.json";
    private static final String REPORT_DATA = "/com/hp/autonomy/frontend/find/core/export/report-data.json";

    private final ObjectMapper objectMapper = new ObjectMapper();
    @MockBean
    private ConfigService<? extends FindConfig<?, ?>> configService;
    @Autowired
    private VisualDataExportService powerPointExportService;

    @SuppressWarnings("rawtypes")
    @Before
    public void setUp() {
        when(((ConfigService) configService).getConfig()).thenReturn(mock(FindConfig.class));
    }

    @Test
    public void topicMap() throws IOException {
        simpleDataTest(outputStream -> {
            final TopicMapData topicMapData = getData(TOPIC_MAP_DATA, TopicMapData.class);
            powerPointExportService.topicMap(outputStream, topicMapData);
        });
    }

    @Test
    public void sunburst() throws IOException {
        simpleDataTest(outputStream -> {
            final SunburstData sunburstData = getData(SUNBURST_DATA, SunburstData.class);
            powerPointExportService.sunburst(outputStream, sunburstData);
        });
    }

    @Ignore
    @Test
    public void table() throws IOException {
        //TODO determine good test data
        simpleDataTest(outputStream -> {
            final TableData tableData = new TableData();
            powerPointExportService.table(outputStream, tableData, "Test Table");
        });
    }

    @Test
    public void map() throws IOException {
        simpleDataTest(outputStream -> {
            final MapData mapData = getData(MAP_DATA, MapData.class);
            powerPointExportService.map(outputStream, mapData, "Test Map");
        });
    }

    @Test
    public void list() throws IOException {
        simpleDataTest(outputStream -> {
            final ListData listData = getData(LIST_DATA, ListData.class);
            powerPointExportService.list(outputStream, listData, "", "");
        });
    }

    @Test
    public void dateGraph() throws IOException {
        simpleDataTest(outputStream -> {
            final DategraphData dategraphData = getData(DATE_GRAPH_DATA, DategraphData.class);
            powerPointExportService.dateGraph(outputStream, dategraphData);
        });
    }

    @Test
    public void report() throws IOException {
        simpleDataTest(outputStream -> {
            final ReportData reportData = getData(REPORT_DATA, ReportData.class);
            powerPointExportService.report(outputStream, reportData, false);
        });
    }

    @Test
    public void multiPageReport() throws IOException {
        simpleDataTest(outputStream -> {
            final ReportData reportData = getData(REPORT_DATA, ReportData.class);
            powerPointExportService.report(outputStream, reportData, true);
        });
    }

    private void simpleDataTest(final Operation operation) throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        operation.accept(outputStream);
        assertNotNull(outputStream.toByteArray());
    }

    private <T> T getData(final String resource, final Class<T> type) throws IOException {
        return objectMapper.readValue(getClass().getResourceAsStream(resource), type);
    }

    @FunctionalInterface
    private interface Operation {
        void accept(OutputStream outputStream) throws IOException;
    }
}
