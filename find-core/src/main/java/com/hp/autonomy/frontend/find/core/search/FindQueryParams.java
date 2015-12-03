/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.search;

import lombok.Data;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.List;

@Data
public class FindQueryParams<S extends Serializable> implements Serializable {
    private final String text;
    private final int maxResults;
    private final String summary;
    private final List<S> index;
    private final String fieldText;
    private final String sort;
    private final DateTime minDate;
    private final DateTime maxDate;
}
