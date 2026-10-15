package com.startechies.bugtracker.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Custom {@link UserDetails} that carries the display {@code userName} and
 * {@link Role} alongside the email so controllers can read both from the
 * {@code Authentication} principal without any DB queries.
 */
public class AppUserDetails implements UserDetails, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String                 email;
    private final String                 password;
    private final String                 userName;
    private final Role                   role;
    private final List<GrantedAuthority> authorities;

    public AppUserDetails(String email, String password, String userName,
                          Role role, List<GrantedAuthority> authorities) {
        this.email       = email;
        this.password    = password;
        this.userName    = userName;
        this.role        = role;
        this.authorities = List.copyOf(authorities);
    }

    /** The display name (e.g. "Alice Smith") — available without a DB query. */
    public String getUserName() { return userName; }

    /** The user's role — available without a DB query. */
    public Role getRole() { return role; }

    // ── UserDetails contract ─────────────────────────────────────────────────

    @Override public String                    getUsername()    { return email; }
    @Override public String                    getPassword()    { return password; }
    @Override public Collection<GrantedAuthority> getAuthorities() { return authorities; }
    @Override public boolean isAccountNonExpired()    { return true; }
    @Override public boolean isAccountNonLocked()     { return true; }
    @Override public boolean isCredentialsNonExpired(){ return true; }
    @Override public boolean isEnabled()              { return true; }
}
