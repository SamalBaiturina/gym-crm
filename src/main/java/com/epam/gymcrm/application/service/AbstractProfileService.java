package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.ProfileDao;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.PasswordChange;
import com.epam.gymcrm.domain.model.User;
import com.epam.gymcrm.domain.model.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Account behaviour shared by trainees and trainers: creation with generated credentials, lookup,
 * password change, update of the names and activation. Subclasses add only what differs.
 */
public abstract class AbstractProfileService<T extends UserProfile<T>> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final String role;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;
    private AuthenticationService authenticationService;

    /** @param role "trainee" or "trainer": used in validation and log messages */
    protected AbstractProfileService(String role) {
        this.role = role;
    }

    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) {
        this.usernameGenerator = usernameGenerator;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    @Autowired
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /** The storage of this kind of profile. */
    protected abstract ProfileDao<T> dao();

    /** Returns the profile if the credentials are valid, otherwise throws AuthenticationException. */
    protected abstract T authenticate(Credentials credentials);

    /** Takes the fields that differ between roles from changes and keeps the rest from existing. */
    protected abstract T applyChanges(T existing, T changes);

    /** Role-specific checks of a profile; the common ones are done by the base class. */
    protected void validateDetails(T profile) {
    }

    protected AuthenticationService authentication() {
        return authenticationService;
    }

    /** Creates a profile; username and password are generated. */
    @Transactional
    public T create(T profile) {
        validate(profile);
        User user = profile.user();
        String username = usernameGenerator.generate(user.firstName(), user.lastName());
        String password = passwordGenerator.generate();
        T saved = dao().save(profile.withUser(user.withCredentials(username, password)));
        log.info("Created {} id={}, username={}", role, saved.id(), username);
        return saved;
    }

    @Transactional(readOnly = true)
    public T getByUsername(Credentials credentials) {
        return authenticate(credentials);
    }

    @Transactional
    public void changePassword(PasswordChange change) {
        T profile = authenticate(change.current());
        dao().save(profile.withUser(profile.user().withPassword(change.newPassword())));
        log.info("Password changed for {} username={}", role, change.current().username());
    }

    /** Updates the names and the role-specific fields; username and password stay the same. */
    @Transactional
    public T update(Credentials credentials, T changes) {
        T existing = authenticate(credentials);
        validate(changes);
        User user = existing.user().withNames(changes.user().firstName(), changes.user().lastName());
        T saved = dao().save(applyChanges(existing.withUser(user), changes));
        log.info("Updated {} username={}", role, credentials.username());
        return saved;
    }

    /** Flips the active flag on every call (not idempotent) and returns the new value. */
    @Transactional
    public boolean toggleActive(Credentials credentials) {
        T profile = authenticate(credentials);
        boolean active = !profile.user().isActive();
        dao().save(profile.withUser(profile.user().withActive(active)));
        log.info("{} username={} is now active={}", role, credentials.username(), active);
        return active;
    }

    private void validate(T profile) {
        Validation.requireNotNull(profile, role);
        Validation.requireUser(profile.user());
        validateDetails(profile);
    }
}
