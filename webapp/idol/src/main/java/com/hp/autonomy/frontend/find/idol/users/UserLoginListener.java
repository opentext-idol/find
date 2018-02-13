package com.hp.autonomy.frontend.find.idol.users;

import com.hp.autonomy.frontend.configuration.authentication.CommunityPrincipal;
import com.hp.autonomy.frontend.find.core.beanconfiguration.BiConfiguration;
import com.hp.autonomy.frontend.find.core.savedsearches.UserEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@ConditionalOnExpression(BiConfiguration.BI_PROPERTY_SPEL)
@Component
public class UserLoginListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private final UserEntityService userEntityService;

    private final Object newUserLock = new Object();

    @Autowired
    public UserLoginListener(final UserEntityService userEntityService) {
        this.userEntityService = userEntityService;
    }

    @Override
    public void onApplicationEvent(final AuthenticationSuccessEvent authenticationSuccessEvent) {
        final Object principal = authenticationSuccessEvent.getAuthentication().getPrincipal();

        if (principal instanceof CommunityPrincipal) {
            final CommunityPrincipal communityPrincipal = (CommunityPrincipal) principal;
            final String principalUsername = communityPrincipal.getUsername();

            userEntityService.getOrCreate(principalUsername);
        }
    }
}
