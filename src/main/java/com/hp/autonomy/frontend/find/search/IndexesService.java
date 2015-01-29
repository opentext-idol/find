/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.search;

import java.util.List;

public interface IndexesService {

    Indexes listIndexes();

    Indexes listIndexes(String apiKey);

    List<Index> listActiveIndexes();

    List<Index> listVisibleIndexes();
}
