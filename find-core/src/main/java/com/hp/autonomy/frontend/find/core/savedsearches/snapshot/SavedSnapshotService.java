package com.hp.autonomy.frontend.find.core.savedsearches.snapshot;

import com.hp.autonomy.frontend.find.core.savedsearches.AbstractSavedSearchService;
import com.hp.autonomy.frontend.find.core.savedsearches.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class SavedSnapshotService extends AbstractSavedSearchService<SavedSnapshot> {
    private final SavedSnapshotRepository savedSnapshotRepository;

    @Autowired
    public SavedSnapshotService(final SavedSnapshotRepository savedSnapshotRepository, final AuditorAware<UserEntity> userEntityAuditorAware) {
        super(savedSnapshotRepository, userEntityAuditorAware);
        this.savedSnapshotRepository = savedSnapshotRepository;
    }

    @Override
    protected Set<SavedSnapshot> getAllForUserId(final Long userId) {
        return savedSnapshotRepository.findByActiveTrueAndUser_UserId(userId);
    }
}