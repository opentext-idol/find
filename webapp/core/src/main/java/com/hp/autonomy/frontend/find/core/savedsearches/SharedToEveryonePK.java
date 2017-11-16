/*
 * Copyright 2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import java.io.Serializable;
import javax.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Primary key for the {@link SharedToEveryone} table.
 */
@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class SharedToEveryonePK implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long searchId;
}
