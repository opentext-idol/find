/*
 * Copyright 2021 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.nifi;

import lombok.Getter;

import javax.xml.bind.annotation.*;
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
