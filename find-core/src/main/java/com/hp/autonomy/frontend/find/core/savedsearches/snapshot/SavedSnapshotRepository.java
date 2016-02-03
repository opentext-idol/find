package com.hp.autonomy.frontend.find.core.savedsearches.snapshot;

import org.springframework.data.repository.CrudRepository;

import java.util.Set;

/**
 * Repository containing actions which can be performed on the main searches table
 * Spring automatically implements basic operations
 */
public interface SavedSnapshotRepository extends CrudRepository<SavedSnapshot, Long>
{
    Set<SavedSnapshot> findByActiveTrueAndUser_UserId(Long userId);
}


