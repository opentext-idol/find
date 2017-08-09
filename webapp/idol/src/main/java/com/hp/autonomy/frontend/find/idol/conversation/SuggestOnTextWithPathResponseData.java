/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.conversation;

import com.hp.autonomy.types.idol.responses.Hit;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "suggestOnTextWithPathResponseData", propOrder = {
        "hits"
})
@XmlRootElement(namespace = "", name = "responsedata")
public class SuggestOnTextWithPathResponseData implements Serializable {

    @XmlElement(name = "hit")
    protected List<CategoryHit> hits;

    public List<CategoryHit> getHits() {
        if (hits == null) {
            hits = new ArrayList<>();
        }
        return hits;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "hit", propOrder = {
            "title",
            "path"
    })
    public static class CategoryHit {
        protected String title;
        protected List<String> path;

        public String getTitle() {
            return title;
        }

        public void setTitle(final String title) {
            this.title = title;
        }

        public List<String> getPath() {
            if (path == null) {
                path = new ArrayList<>();
            }
            return this.path;
        }

    }
}

