/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import java.util.Set;

public interface SavedQueryService {

    Set<SavedQuery> getAll();

    SavedQuery create(SavedQuery search);

    SavedQuery update(SavedQuery search);

    void deleteById(long id);

}
