/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQuery;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class UserEntityTest {
    @Test
    public void equalsIgnoresSearches() {
        final UserEntity userEntity1 = new UserEntity();
        userEntity1.setUid(3L);

        final UserEntity userEntity2 = new UserEntity();
        userEntity2.setUid(3L);
        final Set<SavedSearch<?>> searches = new HashSet<>();
        final SavedSearch<?> search = new SavedQuery();
        searches.add(search);
        userEntity2.setSearches(searches);

        assertEquals(userEntity1, userEntity2);
    }
}