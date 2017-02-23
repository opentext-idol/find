package com.hp.autonomy.frontend.find.idol.savedsearches.snapshot;

import com.hp.autonomy.frontend.find.core.beanconfiguration.BiConfiguration;
import com.hp.autonomy.frontend.find.core.savedsearches.AbstractSavedSearchService;
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchRepository;
import com.hp.autonomy.frontend.find.core.savedsearches.UserEntity;
import com.hp.autonomy.frontend.find.core.savedsearches.snapshot.SavedSnapshot;
import com.hp.autonomy.searchcomponents.core.fields.TagNameFactory;
import com.hp.autonomy.searchcomponents.idol.annotations.IdolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;

@Service
@IdolService
@ConditionalOnProperty(BiConfiguration.BI_PROPERTY)
public class SavedSnapshotService extends AbstractSavedSearchService<SavedSnapshot, SavedSnapshot.Builder> {
    @Autowired
    public SavedSnapshotService(final SavedSearchRepository<SavedSnapshot, SavedSnapshot.Builder> savedSnapshotRepository,
                                final AuditorAware<UserEntity> userEntityAuditorAware,
                                final TagNameFactory tagNameFactory) {
        super(savedSnapshotRepository, userEntityAuditorAware, tagNameFactory);
    }
}
