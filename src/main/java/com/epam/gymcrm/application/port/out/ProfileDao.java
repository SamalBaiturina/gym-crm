package com.epam.gymcrm.application.port.out;

import com.epam.gymcrm.domain.model.UserProfile;

import java.util.Optional;

/**
 * What every profile (trainee or trainer) storage can do.
 */
public interface ProfileDao<T extends UserProfile<T>> extends BaseDao<T>, UsernameLookup {

    Optional<T> findByUsername(String username);
}
