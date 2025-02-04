/*
 * Copyright 2018 Open Text.
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

package com.hp.autonomy.frontend.find.idol.themetracker;

import com.fasterxml.jackson.annotation.JsonInclude;

public class Cluster {
    public String title;
    @JsonInclude(JsonInclude.Include.NON_NULL)
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
