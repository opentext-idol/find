/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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
