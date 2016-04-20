package com.hp.autonomy.frontend.find.idol.savedsearches.snapshot;

import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchRepository;
import com.hp.autonomy.frontend.find.core.savedsearches.snapshot.SavedSnapshot;

/**
 * Repository containing actions which can be performed on the main searches table
 * Spring automatically implements basic operations
 */
public interface SavedSnapshotRepository extends SavedSearchRepository<SavedSnapshot> {
}
