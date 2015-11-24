package com.autonomy.abc.selenium.indexes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Index {
    private final String name;
    private final List<String> parametricFields = new ArrayList<>();
    private final List<String> indexFields = new ArrayList<>();

    public final static Index DEFAULT = new Index("default_index");

    public Index(String name) {
        this.name = name;
    }

    public Index withParametricFields(Collection<String> fields) {
        parametricFields.addAll(fields);
        return this;
    }

    public Index withIndexFields(Collection<String> fields) {
        getIndexFields().addAll(fields);
        return this;
    }

    public String getName() {
        return name;
    }

    public List<String> getParametricFields() {
        return Collections.unmodifiableList(parametricFields);
    }

    public List<String> getIndexFields() {
        return Collections.unmodifiableList(indexFields);
    }

    public String getCreateNotification() {
        return "Created a new index: " + name;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Index){
            if (((Index) obj).getName().equals(getName())){
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return "Index<" + getName() + ">";
    }
}
