/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.hp.autonomy.frontend.configuration.ValidationResult;
import com.hp.autonomy.frontend.configuration.Validator;
import org.springframework.stereotype.Component;

@Component
public class MmapValidator implements Validator<MMAP> {
    @Override
    public ValidationResult<?> validate(final MMAP config) {
        // TODO: real validation
        return new ValidationResult<Object>(true);
    }

    @Override
    public Class<MMAP> getSupportedClass() {
        return MMAP.class;
    }
}
