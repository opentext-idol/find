/*
 * Copyright 2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.themetracker;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class Cluster {
    public String title;
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public String jobName;
    public long fromDate, toDate;
    public int numDocs, x1, x2, y1, y2, id;

    Cluster() {}

    Cluster(final String title, final String jobName, final long fromDate, final long toDate, final int numDocs, final int x1, final int x2, final int y1, final int y2, final int id) {
        this.title = title;
        this.jobName = jobName;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.numDocs = numDocs;
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.id = id;
    }
}
