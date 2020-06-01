/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * JPA embeddable representation of a documents, to be applied as part of a collection to a search,
 * as a document whitelist or blacklist.
 */
@Embeddable
@Data
@NoArgsConstructor
public class DocumentSelection {
    @Column(name = SavedSearch.DocumentSelectionTable.Column.REFERENCE)
    private String reference;

    @JsonCreator
    public DocumentSelection(@JsonProperty("reference") final String reference) {
        this.reference = reference;
    }

}
