/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.indexes;

import com.hp.autonomy.types.IdolDatabase;

import java.util.List;

public interface IndexesService<D extends IdolDatabase, E extends Exception> {
    List<D> listVisibleIndexes() throws E;
}
