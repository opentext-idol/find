package com.hp.autonomy.frontend.find.idol.beanconfiguration;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.stereotype.Service;

// suppress default migrations - we will run them in IdolFindConfigService#postUpdate
@Service
public class IdolFlywayMigrationStrategy implements FlywayMigrationStrategy {

    // Override the migrate method to stop SpringBoot triggering the migrations on startup.
    @Override
    public void migrate(final Flyway flyway) {}

}
