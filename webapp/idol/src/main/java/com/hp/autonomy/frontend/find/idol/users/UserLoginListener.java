package com.hp.autonomy.frontend.find.idol.users;

import com.hp.autonomy.frontend.configuration.authentication.CommunityPrincipal;
import com.hp.autonomy.frontend.find.core.beanconfiguration.BiConfiguration;
import com.hp.autonomy.frontend.find.core.savedsearches.UserEntity;
import com.hp.autonomy.frontend.find.core.savedsearches.UserEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(BiConfiguration.BI_PROPERTY)
@Component
public class UserLoginListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private final UserEntityRepository userEntityRepository;

    private final Object newUserLock = new Object();

    @Autowired
    public UserLoginListener(final UserEntityRepository userEntityRepository) {
        this.userEntityRepository = userEntityRepository;
    }

    @Override
    public void onApplicationEvent(final AuthenticationSuccessEvent authenticationSuccessEvent) {
        final Object principal = authenticationSuccessEvent.getAuthentication().getPrincipal();

        if (principal instanceof CommunityPrincipal) {
            final CommunityPrincipal communityPrincipal = (CommunityPrincipal) principal;
            final String principalUsername = communityPrincipal.getUsername();

            final UserEntity persistedUser = userEntityRepository.findByUsername(principalUsername);

            if (persistedUser == null) {
                // Ensure if, say, two applications log in as the same user at same time, one will be made to wait for lock.
                synchronized (newUserLock) {
                    // Check that there is still no existing user entity after lock is released.
                    final UserEntity persistedUser2 = userEntityRepository.findByUsername(principalUsername);

                    if (persistedUser2 == null) {
                        final UserEntity currentUser = new UserEntity();
                        currentUser.setUsername(principalUsername);

                        userEntityRepository.save(currentUser);
                    }
                }
            }
        }
    }
}
