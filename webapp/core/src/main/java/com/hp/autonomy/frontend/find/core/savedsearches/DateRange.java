/*
 * Copyright 2016 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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

    private final Integer id;

    DateRange(final Integer id) {
        this.id = id;
    }

    public static DateRange getType(final Integer id) {
        if(id == null) {
            return null;
        }

        for(final DateRange dateRange : DateRange.values()) {
            if(id.equals(dateRange.getId())) {
                return dateRange;
            }
        }
        throw new IllegalArgumentException("No matching date range type for id " + id);
    }

    public Integer getId() {
        return id;
    }
}
