/*
 * Copyright 2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Primary key for the {@link SharedToUser} table.
 */
@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class SharedToUserPK implements Serializable {
    private static final long serialVersionUID = 1063284526245402056L;

    private Long searchId;
    private Long userId;
}
