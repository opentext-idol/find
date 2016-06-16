package com.autonomy.abc.selenium.indexes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Index {
    private final String name;
    private final String displayName;
    private final List<String> parametricFields = new ArrayList<>();
    private final List<String> indexFields = new ArrayList<>();

    public final static Index DEFAULT = new Index("default_index", "Default Index");

    public Index(final String name) {
        this.name = name;
        this.displayName = null;
    }

    public Index(final String name, final String displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    public Index withParametricFields(final Collection<String> fields) {
        parametricFields.addAll(fields);
        return this;
    }

    public Index withIndexFields(final Collection<String> fields) {
        indexFields.addAll(fields);
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
        return "Created a new index: " + getDisplayName();
    }

    public String getDeleteNotification() {
        return "Index " + getDisplayName() + " successfully deleted";
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj instanceof Index){
            if (((Index) obj).getName().equals(getName())){
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "Index<" + getName() + '>';
    }

    public String getDisplayName() {
        if(displayName == null){
            return name;
        }

        return displayName;
    }
}
