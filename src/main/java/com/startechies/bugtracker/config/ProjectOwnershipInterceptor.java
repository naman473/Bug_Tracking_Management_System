package com.startechies.bugtracker.config;

import com.startechies.bugtracker.service.ProjectService;
import com.startechies.bugtracker.util.Routes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.UriTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Enforces project ownership for all /manager/projects/{projectName}/** routes.
 * The project name is URL-decoded before the ownership EXISTS check so names
 * containing spaces or special characters (e.g. "Auth0 Integration") work correctly.
 */
@Component
public class ProjectOwnershipInterceptor implements HandlerInterceptor {

    private static final UriTemplate[] PATH_TEMPLATES = {
        new UriTemplate("/manager/projects/{projectName}/{seg1}/{seg2}/{seg3}"),
        new UriTemplate("/manager/projects/{projectName}/{seg1}/{seg2}"),
        new UriTemplate("/manager/projects/{projectName}/{seg1}"),
        new UriTemplate("/manager/projects/{projectName}")
    };

    private final ProjectService projectService;

    public ProjectOwnershipInterceptor(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String rawPath = request.getRequestURI();

        // /manager/projects/new is the create-project form — not a project name, skip ownership check
        if (rawPath.equals("/manager/projects/new") || rawPath.startsWith("/manager/projects/new/")) {
            return true;
        }

        String projectName = extractProjectName(rawPath);
        if (projectName == null) return true;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            response.sendRedirect(Routes.LOGIN);
            return false;
        }

        if (!projectService.ownsProject(projectName, auth.getName())) {
            response.sendRedirect(Routes.MGR_PROJECTS + "?denied=true");
            return false;
        }
        return true;
    }

    private String extractProjectName(String rawPath) {
        for (UriTemplate t : PATH_TEMPLATES) {
            Map<String, String> vars = t.match(rawPath);
            if (vars != null && vars.containsKey("projectName")) {
                // Decode percent-encoding so "Auth0%20Integration" → "Auth0 Integration"
                return UriUtils.decode(vars.get("projectName"), StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}
