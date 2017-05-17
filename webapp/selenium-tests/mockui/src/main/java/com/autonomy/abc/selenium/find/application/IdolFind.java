/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.selenium.find.application;

import com.autonomy.abc.selenium.find.save.SavedSearchService;

public abstract class IdolFind<T extends IdolFindElementFactory> extends FindApplication<T> {
    private T elementFactory;

    public static IdolFind<? extends IdolFindElementFactory> withRole(final UserRole role) {
        if(role == null) {
            return new BIIdolFind();
        }

        switch(role) {
            case BIFHI:
                return new BIIdolFind();
            case FIND:
                return new FindIdolFind();
            default:
                throw new IllegalStateException("Unsupported user role: " + role);
        }
    }

    @Override
    public T elementFactory() {
        return elementFactory;
    }

    @Override
    public boolean isHosted() {
        return false;
    }

    public void setElementFactory(final T elementFactory) {
        this.elementFactory = elementFactory;
    }

    public abstract SavedSearchService savedSearchService();
}
