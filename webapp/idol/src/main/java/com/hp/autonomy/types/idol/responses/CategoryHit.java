package com.hp.autonomy.types.idol.responses;

/*
 * $Id:$
 *
 * Copyright (c) 2017, Autonomy Systems Ltd.
 *
 * Last modified by $Author$ on $Date$ 
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="categoryHit", propOrder = {
        "title",
        "path"
})
public class CategoryHit implements Serializable {
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
