package com.startechies.bugtracker.model;

import java.util.Locale;

/**
 * User roles.
 * {@link #dbValue}   – stored in the database  (e.g. "ProjectManager")
 * {@link #authority} – Spring Security authority (e.g. "ROLE_PROJECTMANAGER")
 * {@link #cssClass}  – CSS modifier on comment cards (e.g. "manager")
 */
public enum Role {
    PROJECT_MANAGER("ProjectManager", "ROLE_PROJECTMANAGER", "manager",  "/manager/dashboard"),
    DEVELOPER      ("Developer",      "ROLE_DEVELOPER",      "developer","/developer/dashboard"),
    TESTER         ("Tester",         "ROLE_TESTER",         "tester",   "/tester/dashboard");

    public final String dbValue;
    public final String authority;
    public final String cssClass;
    public final String dashboardPath;

    Role(String dbValue, String authority, String cssClass, String dashboardPath) {
        this.dbValue       = dbValue;
        this.authority     = authority;
        this.cssClass      = cssClass;
        this.dashboardPath = dashboardPath;
    }

    /** Parse from DB value or common aliases (case-insensitive). */
    public static Role fromDb(String value) {
        if (value == null) return null;
        String v = value.trim().toLowerCase(Locale.ROOT);
        return switch (v) {
            case "projectmanager", "project manager", "pm" -> PROJECT_MANAGER;
            case "developer"                               -> DEVELOPER;
            case "tester"                                  -> TESTER;
            default -> {
                for (Role r : values()) {
                    if (r.dbValue.equalsIgnoreCase(v)) yield r;
                }
                yield null;
            }
        };
    }

    /** Parse from Spring Security authority string ("ROLE_PROJECTMANAGER" → PROJECT_MANAGER). */
    public static Role fromAuthority(String authority) {
        if (authority == null) return null;
        for (Role r : values()) {
            if (r.authority.equals(authority)) return r;
        }
        return null;
    }
}
