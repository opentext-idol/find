/*
 * Copyright 2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.api.SharedToEveryoneApi;
import com.hp.autonomy.frontend.find.core.beanconfiguration.BiConfiguration;
import com.hp.autonomy.frontend.find.core.configuration.FindConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@ConditionalOnExpression(BiConfiguration.BI_PROPERTY_SPEL)
class SharedToEveryoneController implements SharedToEveryoneApi {
    @Autowired private ConfigService<? extends FindConfig<?, ?>> configService;
    @Autowired private SharedToEveryoneRepository sharedToEveryoneRepository;

    private void checkEnabled() {
        if (!configService.getConfig().getSavedSearchConfig().getSharingEnabled()) {
            throw new IllegalArgumentException("Saved search sharing is disabled");
        }
    }

    @Override
    public ResponseEntity<SharedToEveryone> getSearchEveryonePermissions(final Long searchId) {
        checkEnabled();
        return ResponseEntity.ok(sharedToEveryoneRepository.findOneBySavedSearch_Id(searchId));
    }

    @Override
    public ResponseEntity<SharedToEveryone> shareSearchWithEveryone(
            final Long searchId,
            final SharedToEveryone sharedToEveryone
    ) {
        return updateSearchEveryoneShare(searchId, sharedToEveryone);
    }

    @Override
    public ResponseEntity<SharedToEveryone> updateSearchEveryoneShare(
            final Long searchId,
            final SharedToEveryone sharedToEveryone
    ) {
        checkEnabled();
        sharedToEveryone.setId(new SharedToEveryonePK(searchId));

        final Optional<SharedToEveryone> existing = sharedToEveryoneRepository.findById(sharedToEveryone.getId());
        sharedToEveryone.merge(existing.orElse(null));

        return ResponseEntity.ok(sharedToEveryoneRepository.save(sharedToEveryone));
    }

    @Override
    public ResponseEntity<Void> removeSearchEveryoneShare(final Long searchId) {
        checkEnabled();
        sharedToEveryoneRepository.deleteById(new SharedToEveryonePK(searchId));
        return ResponseEntity.noContent().build();
    }

}
