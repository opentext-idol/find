/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.selenium.find.application;

public abstract class HodFind<T extends HodFindElementFactory> extends FindApplication<T> {

    private T elementFactory;

    public static HodFind<? extends HodFindElementFactory> withRole(final UserRole role) {
        if(role == null) {
            return new FindHodFind();
        }

        switch(role) {
            case BIFHI:
                return new BIHodFind();
            case FIND:
                return new FindHodFind();
            default:
                throw new IllegalStateException("Unsupported user role: " + role);
        }
    }

    @Override
    public T elementFactory() {
        return elementFactory;
    }

    public void setElementFactory(final T elementFactory) {
        this.elementFactory = elementFactory;
    }

    @Override
    public boolean isHosted() {
        return true;
    }
}
