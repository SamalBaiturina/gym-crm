package com.epam.gymcrm.application.port.out;

import java.util.List;

public interface UsernameLookup {

    List<String> findUsernamesStartingWith(String prefix);
}
