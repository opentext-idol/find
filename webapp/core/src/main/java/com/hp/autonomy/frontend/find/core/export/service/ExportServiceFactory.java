package com.hp.autonomy.frontend.find.core.export.service;

import com.hp.autonomy.searchcomponents.core.search.QueryRequest;

import java.util.Optional;

/**
 * Retrieves export services available per export format
 */
public interface ExportServiceFactory<R extends QueryRequest<?>, E extends Exception> {
    /**
     * Retrieves the {@link PlatformDataExportService} (if any) for the supplied export format
     *
     * @param exportFormat an export format
     * @return the service corresponding to the export format (if present)
     */
    Optional<PlatformDataExportService<R, E>> getPlatformDataExportService(ExportFormat exportFormat);

    /**
     * Retrieves the {@link VisualDataExportService} (if any) for the supplied export format
     *
     * @param exportFormat an export format
     * @return the service corresponding to the export format (if present)
     */
    Optional<VisualDataExportService> getVisualDataExportService(ExportFormat exportFormat);
}
