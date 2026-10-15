package com.startechies.bugtracker.model;

import java.time.LocalDateTime;

/**
 * Mutable domain model for a registered user.
 * Kept as a JavaBean for JDBC row-mapper compatibility.
 *
 * <p>{@code lastLogin} uses {@link LocalDateTime} — JDBC {@code Timestamp}
 * conversion is the repository's concern, not the domain's.
 */
public class User {

    private String        userId;
    private String        emailId;
    private Role          role;
    private String        userName;
    private String        password;
    private LocalDateTime lastLogin;
    private int           projectCounter;

    public User() {}

    /**
     * Static factory for the registration flow — makes required fields explicit
     * and prevents partial construction.
     */
    public static User forRegistration(String userId, String userName,
                                       String emailId, Role role, String encodedPassword) {
        User u = new User();
        u.userId         = userId;
        u.userName       = userName;
        u.emailId        = emailId;
        u.role           = role;
        u.password       = encodedPassword;
        u.projectCounter = 0;
        return u;
    }

    // ── Accessors ────────────────────────────────────────────────────────────

    public String        getUserId()                      { return userId; }
    public void          setUserId(String v)              { this.userId = v; }

    public String        getEmailId()                     { return emailId; }
    public void          setEmailId(String v)             { this.emailId = v; }

    public Role          getRole()                        { return role; }
    public void          setRole(Role v)                  { this.role = v; }

    /**
     * Returns the DB string of the role (e.g. "ProjectManager") for Thymeleaf
     * comparisons without exposing the enum constant directly to templates.
     */
    public String        getRoleValue()                   { return role != null ? role.dbValue : ""; }

    public String        getUserName()                    { return userName; }
    public void          setUserName(String v)            { this.userName = v; }

    public String        getPassword()                    { return password; }
    public void          setPassword(String v)            { this.password = v; }

    public LocalDateTime getLastLogin()                   { return lastLogin; }
    public void          setLastLogin(LocalDateTime v)    { this.lastLogin = v; }

    public int           getProjectCounter()              { return projectCounter; }
    public void          setProjectCounter(int v)         { this.projectCounter = v; }

    @Override
    public String toString() {
        return "User{id=" + userId + ", email=" + emailId + ", role=" + role + "}";
    }
}
