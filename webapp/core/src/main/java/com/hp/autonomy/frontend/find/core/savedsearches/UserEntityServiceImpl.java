package com.hp.autonomy.frontend.find.core.savedsearches;

import com.hp.autonomy.frontend.find.core.beanconfiguration.BiConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnExpression(BiConfiguration.BI_PROPERTY_SPEL)
public class UserEntityServiceImpl implements UserEntityService {

    private final UserEntityRepository userEntityRepository;

    private final Object lock = new Object();

    @Autowired
    public UserEntityServiceImpl(final UserEntityRepository userEntityRepository) {
        this.userEntityRepository = userEntityRepository;
    }

    @Override
    public UserEntity getOrCreate(final String username) {
        final UserEntity persistedUser = userEntityRepository.findByUsername(username);

        if (persistedUser != null) {
            return persistedUser;
        }

        // Ensure if, say, two applications log in as the same user at same time, one will be made to wait for lock.
        synchronized (lock) {
            // Check that there is still no existing user entity after lock is released.
            final UserEntity persistedUser2 = userEntityRepository.findByUsername(username);

            if (persistedUser2 != null) {
                return persistedUser2;
            }

            final UserEntity currentUser = new UserEntity();
            currentUser.setUsername(username);

            return userEntityRepository.save(currentUser);
        }
    }
}
