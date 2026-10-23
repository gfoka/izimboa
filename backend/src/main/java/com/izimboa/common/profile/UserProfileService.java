package com.izimboa.common.profile;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

    private final UserProfileRepository repository;
    private final Clock clock;

    public UserProfileService(UserProfileRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    /** Enregistre le passage de l'utilisateur (création au premier appel) et renvoie son profil. */
    @Transactional
    public UserProfile touch(UUID userId) {
        Instant now = clock.instant();
        return repository.findById(userId)
                .map(p -> { p.seen(now); return p; })
                .orElseGet(() -> repository.save(new UserProfile(userId, now)));
    }
}
