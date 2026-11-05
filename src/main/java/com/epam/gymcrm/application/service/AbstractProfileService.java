package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.ProfileDao;
import com.epam.gymcrm.domain.exception.NotFoundException;
import com.epam.gymcrm.domain.model.AuthenticatedUser;
import com.epam.gymcrm.domain.model.Role;
import com.epam.gymcrm.domain.model.User;
import com.epam.gymcrm.domain.model.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Account behaviour shared by trainees and trainers: creation with generated credentials, lookup,
 * password change, update of the names and activation. Subclasses add only what differs.
 * <p>
 * The operations on an existing account take an {@link AuthenticatedUser}: the caller has already been
 * authenticated (by the web layer or the facade), so the services load the account by username and do not
 * check the password again.
 */
public abstract class AbstractProfileService<T extends UserProfile<T>> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Role role;
    private final String roleName;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;

    /** @param role the role served by this service: it is used in validation and log messages */
    protected AbstractProfileService(Role role) {
        this.role = role;
        this.roleName = role.name().toLowerCase(Locale.ROOT);
    }

    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) {
        this.usernameGenerator = usernameGenerator;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    /** The storage of this kind of profile. */
    protected abstract ProfileDao<T> dao();

    /** Takes the fields that differ between roles from changes and keeps the rest from existing. */
    protected abstract T applyChanges(T existing, T changes);

    /** Role-specific checks of a profile; the common ones are done by the base class. */
    protected void validateDetails(T profile) {
    }

    /**
     * Loads the profile of an already authenticated user. The password is not checked here.
     *
     * @throws com.epam.gymcrm.domain.exception.ForbiddenOperationException if the user has another role
     * @throws NotFoundException if the account no longer exists
     */
    protected T load(AuthenticatedUser user) {
        user.requireRole(role);
        return dao().findByUsername(user.username()).orElseThrow(() -> {
            log.warn("Authenticated {} username={} not found", roleName, user.username());
            return new NotFoundException(roleName.substring(0, 1).toUpperCase(Locale.ROOT)
                    + roleName.substring(1) + " not found: " + user.username());
        });
    }

    /** Creates a profile; username and password are generated. */
    @Transactional
    public T create(T profile) {
        validate(profile, "create " + roleName);
        User user = profile.user();
        String username = usernameGenerator.generate(user.firstName(), user.lastName());
        String password = passwordGenerator.generate();
        T saved = dao().save(profile.withUser(user.withCredentials(username, password)));
        log.info("Created {} id={}, username={}", roleName, saved.id(), username);
        return saved;
    }

    @Transactional(readOnly = true)
    public T getByUsername(AuthenticatedUser user) {
        return load(user);
    }

    /** Replaces the password of the authenticated user. */
    @Transactional
    public void changePassword(AuthenticatedUser user, String newPassword) {
        Validation.validated("change password of " + roleName + " username=" + user.username(),
                () -> Validation.requireText(newPassword, "newPassword"));
        T profile = load(user);
        dao().save(profile.withUser(profile.user().withPassword(newPassword)));
        log.info("Password changed for {} username={}", roleName, user.username());
    }

    /** Updates the names and the role-specific fields; username, password and the active flag stay the same. */
    @Transactional
    public T update(AuthenticatedUser user, T changes) {
        return doUpdate(user, changes, null);
    }

    /** Same as update, and also sets the active flag to the given value. */
    protected T updateWithActive(AuthenticatedUser user, T changes, boolean active) {
        return doUpdate(user, changes, active);
    }

    private T doUpdate(AuthenticatedUser user, T changes, Boolean active) {
        T existing = load(user);
        validate(changes, "update " + roleName + " username=" + user.username());
        User updated = existing.user().withNames(changes.user().firstName(), changes.user().lastName());
        if (active != null) {
            updated = updated.withActive(active);
        }
        T saved = dao().save(applyChanges(existing.withUser(updated), changes));
        log.info("Updated {} username={}", roleName, user.username());
        return saved;
    }

    /** Flips the active flag on every call (not idempotent) and returns the new value. */
    @Transactional
    public boolean toggleActive(AuthenticatedUser user) {
        T profile = load(user);
        boolean active = !profile.user().isActive();
        dao().save(profile.withUser(profile.user().withActive(active)));
        log.info("Set active={} for {} username={}", active, roleName, user.username());
        return active;
    }

    private void validate(T profile, String context) {
        Validation.validated(context, () -> {
            Validation.requireNotNull(profile, roleName);
            Validation.requireUser(profile.user());
            validateDetails(profile);
        });
    }
}
