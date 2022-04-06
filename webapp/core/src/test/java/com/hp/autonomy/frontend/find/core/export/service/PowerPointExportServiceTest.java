/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@SuppressWarnings({"SpringJavaAutowiredMembersInspection", "resource", "unused"})
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PowerPointExportService.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class PowerPointExportServiceTest {
    @MockBean
    private PowerPointService powerPointService;
    @Autowired
    private PowerPointExportService powerPointExportService;

    @Test
    public void topicMap() throws Exception {
        final TopicMapData data = mock(TopicMapData.class);
        when(powerPointService.topicmap(data)).thenReturn(new XMLSlideShow());
        powerPointExportService.topicMap(new ByteArrayOutputStream(), data);
        verify(powerPointService).topicmap(data);
    }

    @Test(expected = PowerPointExportService.PowerPointExportException.class)
    public void topicMapButError() throws Exception {
        final TopicMapData data = mock(TopicMapData.class);
        when(powerPointService.topicmap(data)).thenThrow(new TemplateLoadException(""));
        powerPointExportService.topicMap(new ByteArrayOutputStream(), data);
    }

    @Test
    public void sunburst() throws Exception {
        final SunburstData data = mock(SunburstData.class);
        when(powerPointService.sunburst(data)).thenReturn(new XMLSlideShow());
        powerPointExportService.sunburst(new ByteArrayOutputStream(), data);
        verify(powerPointService).sunburst(data);
    }

    @Test(expected = PowerPointExportService.PowerPointExportException.class)
    public void sunburstButError() throws Exception {
        final SunburstData data = mock(SunburstData.class);
        when(powerPointService.sunburst(data)).thenThrow(new TemplateLoadException(""));
        powerPointExportService.sunburst(new ByteArrayOutputStream(), data);
    }

    @Test
    public void table() throws Exception {
        final TableData data = mock(TableData.class);
        final String title = "A title";
        when(powerPointService.table(data, title)).thenReturn(new XMLSlideShow());
        powerPointExportService.table(new ByteArrayOutputStream(), data, title);
        verify(powerPointService).table(data, title);
    }

    @Test(expected = PowerPointExportService.PowerPointExportException.class)
    public void tableMapButError() throws Exception {
        final TableData data = mock(TableData.class);
        final String title = "A title";
        when(powerPointService.table(data, title)).thenThrow(new TemplateLoadException(""));
        powerPointExportService.table(new ByteArrayOutputStream(), data, title);
    }

    @Test
    public void map() throws Exception {
        final MapData data = mock(MapData.class);
        final String title = "A title";
        when(powerPointService.map(data, title)).thenReturn(new XMLSlideShow());
        powerPointExportService.map(new ByteArrayOutputStream(), data, title);
        verify(powerPointService).map(data, title);
    }

    @Test(expected = PowerPointExportService.PowerPointExportException.class)
    public void mapButError() throws Exception {
        final MapData data = mock(MapData.class);
        final String title = "A title";
        when(powerPointService.map(data, title)).thenThrow(new TemplateLoadException(""));
        powerPointExportService.map(new ByteArrayOutputStream(), data, title);
    }

    @Test
    public void list() throws Exception {
        final ListData data = mock(ListData.class);
        when(powerPointService.list(data, null, null)).thenReturn(new XMLSlideShow());
        powerPointExportService.list(new ByteArrayOutputStream(), data, null, null);
        verify(powerPointService).list(data, null, null);
    }

    @Test(expected = PowerPointExportService.PowerPointExportException.class)
    public void listButError() throws Exception {
        final ListData data = mock(ListData.class);
        when(powerPointService.list(data, null, null)).thenThrow(new TemplateLoadException(""));
        powerPointExportService.list(new ByteArrayOutputStream(), data, null, null);
    }

    @Test
    public void dateGraph() throws Exception {
        final DategraphData data = mock(DategraphData.class);
        when(powerPointService.graph(data)).thenReturn(new XMLSlideShow());
        powerPointExportService.dateGraph(new ByteArrayOutputStream(), data);
        verify(powerPointService).graph(data);
    }

    @Test(expected = PowerPointExportService.PowerPointExportException.class)
    public void dateGraphButError() throws Exception {
        final DategraphData data = mock(DategraphData.class);
        when(powerPointService.graph(data)).thenThrow(new TemplateLoadException(""));
        powerPointExportService.dateGraph(new ByteArrayOutputStream(), data);
    }

    @Test
    public void report() throws Exception {
        final ReportData data = mock(ReportData.class);
        final boolean multiPage = true;
        when(powerPointService.report(data, multiPage)).thenReturn(new XMLSlideShow());
        powerPointExportService.report(new ByteArrayOutputStream(), data, multiPage);
        verify(powerPointService).report(data, multiPage);
    }

    @Test(expected = PowerPointExportService.PowerPointExportException.class)
    public void reportButError() throws Exception {
        final ReportData data = mock(ReportData.class);
        final boolean multiPage = true;
        when(powerPointService.report(data, multiPage)).thenThrow(new TemplateLoadException(""));
        powerPointExportService.report(new ByteArrayOutputStream(), data, multiPage);
    }

    @Test
    public void handlesFormats() throws Exception {
        assertTrue(powerPointExportService.handlesFormats().contains(ExportFormat.PPTX));
    }
}
