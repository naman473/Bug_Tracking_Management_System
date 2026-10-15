package com.startechies.bugtracker.service;

import com.startechies.bugtracker.model.AppUserDetails;
import com.startechies.bugtracker.model.UserDisplay;
import com.startechies.bugtracker.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Thin facade over user display queries.
 * All display-info comes from a single DB query via
 * {@link UserRepository#getDisplayInfo(String)}.
 * Kept as a service so controllers don't depend directly on the repository.
 */
@Service
public class UserInfoService {

    private final UserRepository userRepository;

    public UserInfoService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Returns all display-level user info in one DB query. */
    public UserDisplay getDisplayInfo(String email) {
        return userRepository.getDisplayInfo(email);
    }

    /** Convenience accessor when only the username is needed (fires a DB query). */
    public String getUserName(String email) {
        return getDisplayInfo(email).userName();
    }

    /**
     * Returns the userName from the {@link AppUserDetails} principal — zero DB queries.
     * Falls back to the email-based lookup if the principal type is unexpected
     * (e.g. in tests or with a non-standard auth provider).
     */
    public String getUserName(Authentication auth) {
        if (auth.getPrincipal() instanceof AppUserDetails details) {
            return details.getUserName();
        }
        return getUserName(auth.getName());
    }
}
