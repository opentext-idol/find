/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.types.idol.responses;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name="suggestOnTextWithPathResponseData",
        propOrder = {
        "numhits",
        "hits"
})
@XmlRootElement(namespace = "", name = "responsedata")
public class SuggestOnTextWithPathResponseData implements Serializable {

    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter1.class)
    @XmlSchemaType(name = "integer")
    protected Long numhits;

    @XmlElement(name = "hit")
    protected List<CategoryHit> hits;

    public List<CategoryHit> getHits() {
        if (hits == null) {
            hits = new ArrayList<>();
        }
        return hits;
    }

}

