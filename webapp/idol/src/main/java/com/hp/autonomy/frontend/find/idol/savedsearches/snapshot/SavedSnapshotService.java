package com.hp.autonomy.frontend.find.idol.savedsearches.snapshot;

import com.hp.autonomy.frontend.find.core.beanconfiguration.BiConfiguration;
import com.hp.autonomy.frontend.find.core.savedsearches.AbstractSavedSearchService;
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchRepository;
import com.hp.autonomy.frontend.find.core.savedsearches.UserEntity;
import com.hp.autonomy.frontend.find.core.savedsearches.snapshot.SavedSnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(BiConfiguration.BI_PROPERTY)
public class SavedSnapshotService extends AbstractSavedSearchService<SavedSnapshot> {
    @Autowired
    public SavedSnapshotService(final SavedSearchRepository<SavedSnapshot> savedSnapshotRepository, final AuditorAware<UserEntity> userEntityAuditorAware) {
        super(savedSnapshotRepository, userEntityAuditorAware);
    }
}
