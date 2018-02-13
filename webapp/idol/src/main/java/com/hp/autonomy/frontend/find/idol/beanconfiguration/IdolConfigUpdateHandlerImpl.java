package com.hp.autonomy.frontend.find.idol.beanconfiguration;

import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import static com.hp.autonomy.frontend.find.core.beanconfiguration.BiConfiguration.BI_PROPERTY_SPEL;

@Component
@ConditionalOnExpression("not " + BI_PROPERTY_SPEL)
public class IdolConfigUpdateHandlerImpl implements IdolConfigUpdateHandler {
    @Override
    public void update(final IdolFindConfig config) {

    }
}
