/*
 * Copyright 2021 Open Text.
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

package com.hp.autonomy.frontend.find.idol.nifi;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;

import java.util.List;

/**
 * Response to the list-actions action.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "nifiListActions_response", propOrder = {"actions"})
@XmlRootElement(namespace = "", name = "actions")
@Getter
public class NifiListActionsResponse {
    @XmlElement(name = "action")
    private List<NifiAction> actions;
}
