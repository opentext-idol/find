package com.hp.autonomy.frontend.find.idol.beanconfiguration;

import com.hp.autonomy.frontend.find.core.beanconfiguration.BiConfiguration;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = BiConfiguration.BI_PROPERTY, havingValue = "false")
public class IdolConfigUpdateHandlerImpl implements IdolConfigUpdateHandler {
    @Override
    public void update(final IdolFindConfig config) {

    }
}
