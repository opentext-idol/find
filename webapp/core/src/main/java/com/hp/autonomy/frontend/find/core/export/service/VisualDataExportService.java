package com.hp.autonomy.frontend.find.core.export.service;

import com.hp.autonomy.frontend.reports.powerpoint.dto.DategraphData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.ListData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.MapData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.ReportData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.SunburstData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.TableData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.TopicMapData;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

/**
 * Exports visualisers to an external format
 */
public interface VisualDataExportService {
    /**
     * Exports topic map
     *
     * @param outputStream output stream to write to
     * @param data         topic map data
     * @throws IOException any I/O error
     */
    void topicMap(OutputStream outputStream, TopicMapData data) throws IOException;

    /**
     * Exports sunburst
     *
     * @param outputStream output stream to write to
     * @param data         sunburst data
     * @throws IOException any I/O error
     */
    void sunburst(OutputStream outputStream, SunburstData data) throws IOException;

    /**
     * Exports table
     *
     * @param outputStream output stream to write to
     * @param data         table data
     * @param title        table title
     * @throws IOException any I/O error
     */
    void table(OutputStream outputStream, TableData data, String title) throws IOException;

    /**
     * Exports map
     *
     * @param outputStream output stream to write to
     * @param data         map data
     * @param title        map title
     * @throws IOException any I/O error
     */
    void map(OutputStream outputStream, MapData data, String title) throws IOException;

    /**
     * Exports result list
     *
     * @param outputStream output stream to write to
     * @param data         list data
     * @param results      list results
     * @param sortBy       list sort
     * @throws IOException any I/O error
     */
    void list(OutputStream outputStream, ListData data, String results, String sortBy) throws IOException;

    /**
     * Exports date graph
     *
     * @param outputStream output stream to write to
     * @param data         date graph data
     * @throws IOException any I/O error
     */
    void dateGraph(OutputStream outputStream, DategraphData data) throws IOException;

    /**
     * Exports report containing multiple visualisers
     *
     * @param outputStream output stream to write to
     * @param data         date report data
     * @param multiPage    whether to export as a single page or multiple pages
     * @throws IOException any I/O error
     */
    void report(OutputStream outputStream, ReportData data, boolean multiPage) throws IOException;

    /**
     * Export formats supported by this service
     *
     * @return the formats supported by this service
     */
    Collection<ExportFormat> handlesFormats();
}
