package com.hp.autonomy.frontend.find.idol.beanconfiguration;

import com.hp.autonomy.frontend.find.core.beanconfiguration.BiConfiguration;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnExpression(BiConfiguration.BI_PROPERTY_SPEL)
public class FlywayIdolConfigUpdateHandler implements IdolConfigUpdateHandler {
    private final Flyway flyway;

    @Autowired
    public FlywayIdolConfigUpdateHandler (final Flyway flyway) {
        this.flyway = flyway;
    }


    @Override
    public void update(final IdolFindConfig config) {
        flyway.repair();
        flyway.migrate();
    }
}
