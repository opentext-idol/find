package com.hp.autonomy.frontend.configuration;

import com.autonomy.frontend.configuration.ValidationResult;
import com.autonomy.frontend.configuration.Validator;
import com.hp.autonomy.frontend.find.search.IndexesService;
import org.springframework.beans.factory.annotation.Autowired;

public class IodConfigValidator implements Validator<IodConfig> {

    @Autowired
    private IndexesService indexesService;

    @Override
    public ValidationResult<?> validate(final IodConfig iodConfig) {
        return iodConfig.validate(indexesService);
    }

    @Override
    public Class<IodConfig> getSupportedClass() {
        return IodConfig.class;
    }
}
