/*
 * Copyright 2021 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.nifi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * An action that can be executed in NiFi.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "nifiListActions_actions", propOrder = {"id", "name"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NifiAction {
    private String id;
    @XmlElement(name = "displayName")
    private String name;
}
