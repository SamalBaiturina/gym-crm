package com.epam.gymcrm.application.port.out;

import com.epam.gymcrm.domain.model.Role;

/**
 * Business events that the application counts. The services report an event here and do not know
 * where the numbers go: the infrastructure layer decides that.
 */
public interface UsageMetrics {

    /** An account of the given role was created. */
    void registrationCompleted(Role role);

    /** A training was saved. */
    void trainingAdded();

    /** A request came with an unknown username or a wrong password. */
    void authenticationFailed();
}
