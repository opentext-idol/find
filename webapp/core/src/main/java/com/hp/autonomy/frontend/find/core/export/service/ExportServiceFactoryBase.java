package com.hp.autonomy.frontend.find.core.export.service;

import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class ExportServiceFactoryBase<R extends QueryRequest<?>, E extends Exception> implements ExportServiceFactory<R, E> {
    private final Map<ExportFormat, PlatformDataExportService<R, E>> platformDataExportServiceMap;
    private final Map<ExportFormat, VisualDataExportService> visualDataExportServiceMap;

    protected ExportServiceFactoryBase(
            final Collection<PlatformDataExportService<R, E>> platformDataExportServices,
            final Collection<VisualDataExportService> visualDataExportServices) {
        platformDataExportServiceMap = platformDataExportServices.stream()
                .flatMap(service -> service.handlesFormats().stream().map(format -> new ImmutablePair<>(format, service)))
                .collect(Collectors.toMap(ImmutablePair::getLeft, ImmutablePair::getRight));
        visualDataExportServiceMap = visualDataExportServices.stream()
                .flatMap(service -> service.handlesFormats().stream().map(format -> new ImmutablePair<>(format, service)))
                .collect(Collectors.toMap(ImmutablePair::getLeft, ImmutablePair::getRight));
    }

    @Override
    public Optional<PlatformDataExportService<R, E>> getPlatformDataExportService(final ExportFormat exportFormat) {
        return Optional.ofNullable(platformDataExportServiceMap.get(exportFormat));
    }

    @Override
    public Optional<VisualDataExportService> getVisualDataExportService(final ExportFormat exportFormat) {
        return Optional.ofNullable(visualDataExportServiceMap.get(exportFormat));
    }
}
