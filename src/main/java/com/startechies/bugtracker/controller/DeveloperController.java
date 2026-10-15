package com.startechies.bugtracker.controller;

import com.startechies.bugtracker.model.ProjectHeader;
import com.startechies.bugtracker.service.*;
import com.startechies.bugtracker.util.Routes;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/developer")
public class DeveloperController {

    private final ProjectService    projectService;
    private final BugService        bugService;
    private final UserInfoService   userInfoService;
    private final BugCommentService commentService;

    public DeveloperController(ProjectService projectService, BugService bugService,
                               UserInfoService userInfoService, BugCommentService commentService) {
        this.projectService  = projectService;
        this.bugService      = bugService;
        this.userInfoService = userInfoService;
        this.commentService  = commentService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        String email = auth.getName();
        // Single list fetch — derive assignedCount from it, avoid a second query
        var assignedBugs = bugService.findAssignedBugsForDeveloper(email);
        var stats = bugService.getBugStatsForProjects(
                projectService.findProjectsForUserEmail(email)
                        .stream().map(p -> p.projectName()).toList());

        model.addAttribute("userName",      userInfoService.getUserName(auth));
        model.addAttribute("assignedCount", assignedBugs.size());
        model.addAttribute("stats",         stats);
        return "developer-dashboard";
    }

    @GetMapping("/projects")
    public String projects(Authentication auth, Model model) {
        model.addAttribute("projects", projectService.findProjectsForUserEmail(auth.getName()));
        return "developer-projects";
    }

    @GetMapping("/project")
    public String project(@RequestParam(required = false) String name,
                          Authentication auth, Model model) {
        String email = auth.getName();
        var allProjects = projectService.findProjectsForUserEmail(email);

        String projectName = (name != null && !name.isBlank())
                ? name
                : allProjects.stream().findFirst().map(p -> p.projectName()).orElse(null);

        ProjectHeader header = projectName != null
                ? projectService.findProjectHeader(projectName)
                : ProjectHeader.empty();

        // Scope bugs to selected project — not all bugs across all projects
        List<com.startechies.bugtracker.model.Bug> bugs = projectName != null
                ? bugService.findAssignedBugsForDeveloperByProject(email, projectName)
                : List.of();

        model.addAttribute("projectName", projectName);
        model.addAttribute("managerName", header.managerName());
        model.addAttribute("startDate",   header.startDate());
        model.addAttribute("team",        projectName == null ? List.of()
                : projectService.findTeamMembersByProjectName(projectName));
        model.addAttribute("allProjects", allProjects);
        model.addAttribute("bugs",        bugs);
        model.addAttribute("comments",    commentService.getCommentsGroupedByBug(bugs));
        return "developer-project";
    }

    @PostMapping("/bugs/close")
    public String closeBug(Authentication auth,
                           @RequestParam String bugId,
                           @RequestParam(required = false) String projectName,
                           RedirectAttributes redirectAttributes) {
        String closedBy = userInfoService.getUserName(auth);
        int updated = bugService.closeBug(bugId, closedBy);
        redirectAttributes.addFlashAttribute("message",
                updated == 0
                        ? "Bug could not be closed — it has not been marked for closing by the manager."
                        : "Bug closed successfully.");
        return Routes.redirect(Routes.devProject(projectName));
    }
}
