package com.hp.autonomy.frontend.find.hod.export.service;

import com.hp.autonomy.frontend.find.core.export.service.ExportServiceFactoryBase;
import com.hp.autonomy.frontend.find.core.export.service.PlatformDataExportService;
import com.hp.autonomy.frontend.find.core.export.service.VisualDataExportService;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class HodExportServiceFactory extends ExportServiceFactoryBase<HodQueryRequest, HodErrorException> {
    @Autowired
    public HodExportServiceFactory(final Collection<PlatformDataExportService<HodQueryRequest, HodErrorException>> platformDataExportServices,
                                   final Collection<VisualDataExportService> visualDataExportServices) {
        super(platformDataExportServices, visualDataExportServices);
    }
}
