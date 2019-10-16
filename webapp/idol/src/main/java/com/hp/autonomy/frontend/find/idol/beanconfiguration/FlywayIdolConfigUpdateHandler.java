package com.hp.autonomy.frontend.find.idol.beanconfiguration;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.impl.AciServiceImpl;
import com.autonomy.aci.client.transport.AciHttpClient;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.hp.autonomy.frontend.configuration.LoginTypes;
import com.hp.autonomy.frontend.configuration.authentication.CommunityAuthentication;
import com.hp.autonomy.frontend.configuration.validation.ValidationResult;
import com.hp.autonomy.frontend.find.core.beanconfiguration.BiConfiguration;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.types.idol.marshalling.ProcessorFactory;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import static db.migration.AbstractMigrateUsersToIncludeUsernames.COMMUNITY_HOST;
import static db.migration.AbstractMigrateUsersToIncludeUsernames.COMMUNITY_PORT;
import static db.migration.AbstractMigrateUsersToIncludeUsernames.COMMUNITY_PROTOCOL;

@Component
@ConditionalOnExpression(BiConfiguration.BI_PROPERTY_SPEL)
public class FlywayIdolConfigUpdateHandler implements IdolConfigUpdateHandler {

    private final AciService aciService;
    private final ProcessorFactory processorFactory;
    private final Flyway flyway;

    @Autowired
    public FlywayIdolConfigUpdateHandler (
            final ProcessorFactory processorFactory,
            final Flyway flyway,
            final AciHttpClient aciHttpClient
    ) {
        this.processorFactory = processorFactory;
        this.flyway = flyway;

        aciService = new AciServiceImpl(aciHttpClient);
    }


    @Override
    public void update(final IdolFindConfig config) {
        final CommunityAuthentication community = config.getLogin();
        if (LoginTypes.DEFAULT.equalsIgnoreCase(community.getMethod())) {
            return;
        }

        final ValidationResult validation = community.validate(aciService, processorFactory);
        if (!validation.isValid()) {
            throw new RuntimeException(
                "Community server configuration is invalid: " + validation.getData());
        }

        final AciServerDetails serverDetails = config.getCommunityDetails();

        // terrible hack - using system properties to pass data to migration
        System.setProperty(COMMUNITY_PROTOCOL, serverDetails.getProtocol().toString());
        System.setProperty(COMMUNITY_HOST, serverDetails.getHost());
        System.setProperty(COMMUNITY_PORT, String.valueOf(serverDetails.getPort()));

        flyway.migrate();

        System.clearProperty(COMMUNITY_PROTOCOL);
        System.clearProperty(COMMUNITY_HOST);
        System.clearProperty(COMMUNITY_PORT);
    }
}
