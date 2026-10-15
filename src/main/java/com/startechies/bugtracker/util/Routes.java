package com.startechies.bugtracker.util;

import org.springframework.web.util.UriComponentsBuilder;

/**
 * Central registry of application URL paths.
 * All path-building methods use {@link UriComponentsBuilder} so project names
 * containing spaces or special characters are correctly percent-encoded.
 */
public final class Routes {
    private Routes() {}

    // ── Auth ─────────────────────────────────────────────────────────────────
    public static final String LOGIN           = "/login";
    public static final String REGISTER        = "/register";
    public static final String IMPORT_USERS    = "/import-users";
    public static final String CHANGE_PASSWORD = "/account/change-password";
    public static final String LOGOUT          = "/logout";
    public static final String HOME            = "/home";

    // ── Manager ──────────────────────────────────────────────────────────────
    public static final String MGR_PROJECTS = "/manager/projects";

    /** URL for a specific manager project page — name is percent-encoded. */
    public static String mgrProject(String name) {
        return UriComponentsBuilder
                .fromPath(MGR_PROJECTS + "/{name}")
                .buildAndExpand(name)
                .encode()
                .toUriString();
    }

    // ── Tester ───────────────────────────────────────────────────────────────
    public static final String TST_BUGS = "/tester/bugs";

    // ── Developer ────────────────────────────────────────────────────────────
    /** URL for the developer project page — name query param is percent-encoded. */
    public static String devProject(String name) {
        if (name == null) return "/developer/project";
        return UriComponentsBuilder
                .fromPath("/developer/project")
                .queryParam("name", name)
                .build()
                .encode()
                .toUriString();
    }

    // ── Redirect helper ──────────────────────────────────────────────────────
    /** Prepends "redirect:" for Spring MVC controller return values. */
    public static String redirect(String path) { return "redirect:" + path; }
}
