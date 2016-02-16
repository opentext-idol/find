package com.hp.autonomy.frontend.find.core.savedsearches.snapshot;

import com.hp.autonomy.frontend.find.core.savedsearches.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class SavedSnapshotServiceImpl implements SavedSnapshotService {
    private final SavedSnapshotRepository savedSnapshotRepository;
    private final AuditorAware<UserEntity> userEntityAuditorAware;

    @Autowired
    public SavedSnapshotServiceImpl(final SavedSnapshotRepository savedSnapshotRepository, final AuditorAware<UserEntity> userEntityAuditorAware) {
        this.savedSnapshotRepository = savedSnapshotRepository;
        this.userEntityAuditorAware = userEntityAuditorAware;
    }

    @Override
    public Set<SavedSnapshot> getAll() {
        final Long userId = userEntityAuditorAware.getCurrentAuditor().getUserId();
        return savedSnapshotRepository.findByActiveTrueAndUser_UserId(userId);
    }

    @Override
    public SavedSnapshot create(final SavedSnapshot snapshot) {
        return savedSnapshotRepository.save(snapshot);
    }

    @Override
    public SavedSnapshot update(final SavedSnapshot snapshot) {
        final SavedSnapshot existing = savedSnapshotRepository.findOne(snapshot.getId());

        if (existing == null) {
            throw new IllegalArgumentException("Saved snapshot not found");
        } else {
            existing.setIndexes(snapshot.getIndexes());
            existing.setMaxDate(snapshot.getMaxDate());
            existing.setMinDate(snapshot.getMinDate());
            existing.setParametricValues(snapshot.getParametricValues());
            existing.setRelatedConcepts(snapshot.getRelatedConcepts());
            existing.setQueryText(snapshot.getQueryText());
            existing.setTitle(snapshot.getTitle());
            existing.setResultCount(snapshot.getResultCount());
            existing.setStateTokens(snapshot.getStateTokens());
            return savedSnapshotRepository.save(existing);
        }
    }

    @Override
    public void deleteById(final long id) {
        final SavedSnapshot savedSnapshot = savedSnapshotRepository.findOne(id);
        savedSnapshot.setActive(false);
        savedSnapshotRepository.save(savedSnapshot);
    }
}