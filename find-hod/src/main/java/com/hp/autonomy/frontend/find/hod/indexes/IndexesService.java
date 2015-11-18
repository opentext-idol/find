/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.indexes;

import com.hp.autonomy.databases.Database;
import com.hp.autonomy.hod.client.error.HodErrorException;

import java.util.List;

public interface IndexesService {
    List<Database> listVisibleIndexes() throws HodErrorException;
}
