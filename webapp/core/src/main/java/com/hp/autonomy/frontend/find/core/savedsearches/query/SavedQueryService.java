/*
 * Copyright 2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches.query;

import com.hp.autonomy.frontend.find.core.beanconfiguration.BiConfiguration;
import com.hp.autonomy.frontend.find.core.savedsearches.*;
import com.hp.autonomy.searchcomponents.core.fields.TagNameFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnExpression(BiConfiguration.BI_PROPERTY_SPEL)
public class SavedQueryService extends AbstractSavedSearchService<SavedQuery, SavedQuery.Builder> {
    @Autowired
    public SavedQueryService(@SuppressWarnings("TypeMayBeWeakened") final SavedQueryRepository savedQueryRepository,
                             final SharedToUserRepository sharedToUserRepository,
                             final SharedToEveryoneRepository sharedToEveryoneRepository,
                             final AuditorAware<UserEntity> userEntityAuditorAware,
                             final TagNameFactory tagNameFactory) {
        super(savedQueryRepository, sharedToUserRepository, sharedToEveryoneRepository, userEntityAuditorAware, tagNameFactory, SavedQuery.class);
    }

    @Override
    public SavedQuery build(final SavedSearch<?, ?> search) {
        return new SavedQuery.Builder(search).build();
    }

}
