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
