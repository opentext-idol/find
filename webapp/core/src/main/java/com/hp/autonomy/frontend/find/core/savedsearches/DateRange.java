/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

public enum DateRange {

    /**
     * N.B. These IDs should not be changed unless a matching migration
     * script for updating stale data is included!
     */
    CUSTOM(0),
    YEAR(1),
    MONTH(2),
    WEEK(3),
    NEW(4);

    private Integer id;

    DateRange(Integer id) {
        this.id = id;
    }

    public static DateRange getType(Integer id) {
        if (id == null) {
            return null;
        }

        for (DateRange dateRange : DateRange.values()) {
            if (id.equals(dateRange.getId())) {
                return dateRange;
            }
        }
        throw new IllegalArgumentException("No matching date range type for id " + id);
    }

    public Integer getId() {
        return id;
    }
}
