/*
 * Copyright 2020 Open Text.
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

package com.hp.autonomy.frontend.find.core.savedsearches;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;

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
