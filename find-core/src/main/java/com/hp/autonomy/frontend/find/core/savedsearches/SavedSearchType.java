/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

/**
 * Enumerate the different subclasses of {@link SavedSearch}
 * so that we have a definition of the  possible values of the
 * {@link javax.persistence.DiscriminatorColumn} in the searches table.
 *
 * Ids are explicitly defined to try and make it more obvious these shouldn't be renamed
 * or reordered without a database migration.
 */
public enum SavedSearchType {

    QUERY(Values.QUERY),
    SNAPSHOT(Values.SNAPSHOT);

    private Integer id;

    SavedSearchType(String id) {
        this.id = Integer.parseInt(id);
    }

    public static SavedSearchType getType(Integer id) {
        if (id == null) {
            return null;
        }

        for (SavedSearchType searchType : SavedSearchType.values()) {
            if (id.equals(searchType.getId())) {
                return searchType;
            }
        }
        throw new IllegalArgumentException("No matching search type for id " + id);
    }

    public Integer getId() {
        return id;
    }

    /**
     * Values used so that we have constant integer values
     * to be used in @DiscriminatorValue annotation values.
     *
     * This hardly seems ideal, but at least we have a single
     * definition point for these values.
     */
    public static class Values {
        public static final String QUERY = "0";
        public static final String SNAPSHOT = "1";
    }
}
