package com.hp.autonomy.frontend.find.core.savedsearches.savedsnapshot;

import java.util.Set;

public interface SavedSnapshotService {

    Set<SavedSnapshot> getAll();

    SavedSnapshot create(SavedSnapshot search);

    SavedSnapshot update(SavedSnapshot search);

    void deleteById(long id);

}
