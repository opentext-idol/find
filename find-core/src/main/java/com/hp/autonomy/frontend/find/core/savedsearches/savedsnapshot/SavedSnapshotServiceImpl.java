package com.hp.autonomy.frontend.find.core.savedsearches.savedsnapshot;

import com.hp.autonomy.frontend.find.core.savedsearches.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;

import java.util.Set;


@Service
public class SavedSnapshotServiceImpl implements SavedSnapshotService {

    private SavedSnapshotRepository SavedSnapshotRepository;
    private AuditorAware<UserEntity> userEntityAuditorAware;

    @Autowired
    public SavedSnapshotServiceImpl(final SavedSnapshotRepository SavedSnapshotRepository, final AuditorAware<UserEntity> userEntityAuditorAware) {
        this.SavedSnapshotRepository = SavedSnapshotRepository;
        this.userEntityAuditorAware = userEntityAuditorAware;
    }

    @Override
    public Set<SavedSnapshot> getAll() {
        final Long userId = userEntityAuditorAware.getCurrentAuditor().getUserId();
        return SavedSnapshotRepository.findByActiveTrueAndUser_UserId(userId);
    }

    @Override
    public SavedSnapshot create(final SavedSnapshot query) {
        return SavedSnapshotRepository.save(query);
    }

    @Override
    public SavedSnapshot update(final SavedSnapshot query) {
        return SavedSnapshotRepository.save(query);
    }

    @Override
    public void deleteById(final long id) {
        final SavedSnapshot SavedSnapshot = SavedSnapshotRepository.findOne(id);
        SavedSnapshot.setActive(false);
        SavedSnapshotRepository.save(SavedSnapshot);
    }
}