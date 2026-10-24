package com.epam.gymcrm.application.port.out;

import java.util.List;

/** Finds taken usernames, so that a new one gets a free serial number. */
public interface UsernameLookup {

    List<String> findUsernamesStartingWith(String prefix);
}
