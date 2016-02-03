package com.hp.autonomy.frontend.find.core.savedsearches.snapshot;

import com.hp.autonomy.frontend.find.core.savedsearches.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;

import java.util.Set;


@Service
public class SavedSnapshotServiceImpl implements SavedSnapshotService {

    private SavedSnapshotRepository savedSnapshotRepository;
    private AuditorAware<UserEntity> userEntityAuditorAware;

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
        return savedSnapshotRepository.save(snapshot);
    }

    @Override
    public void deleteById(final long id) {
        final SavedSnapshot savedSnapshot = savedSnapshotRepository.findOne(id);
        savedSnapshot.setActive(false);
        savedSnapshotRepository.save(savedSnapshot);
    }
}