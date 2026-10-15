package com.startechies.bugtracker.controller;

import com.startechies.bugtracker.model.*;
import com.startechies.bugtracker.service.*;
import com.startechies.bugtracker.util.Routes;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/manager")
public class ManagerController {

    private final ProjectService    projectService;
    private final BugService        bugService;
    private final UserInfoService   userInfoService;
    private final BugCommentService commentService;

    public ManagerController(ProjectService projectService, BugService bugService,
                             UserInfoService userInfoService, BugCommentService commentService) {
        this.projectService  = projectService;
        this.bugService      = bugService;
        this.userInfoService = userInfoService;
        this.commentService  = commentService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        String email = auth.getName();
        List<Project> projects = projectService.findProjectsForUserEmail(email);

        UserDisplay display = userInfoService.getDisplayInfo(email);
        List<String> names  = projects.stream().map(Project::projectName).toList();
        BugStats stats = bugService.getBugStatsForProjects(names);

        model.addAttribute("userName",       display.userName());
        model.addAttribute("email",          email);
        model.addAttribute("lastLogin",      display.lastLoginText());
        model.addAttribute("projectCounter", display.projectCounter());
        model.addAttribute("totalOpen",      stats.open());
        model.addAttribute("totalCritical",  stats.critical());
        model.addAttribute("totalAssigned",  stats.assigned());
        return "manager-dashboard";
    }

    @GetMapping("/projects")
    public String projects(Authentication auth, Model model) {
        model.addAttribute("projects", projectService.findProjectsForUserEmail(auth.getName()));
        return "manager-project-list";
    }

    @GetMapping("/projects/new")
    public String newProjectPage(Authentication auth, Model model) {
        String email = auth.getName();
        model.addAttribute("developers", projectService.findEligibleDevelopers());
        model.addAttribute("testers",    projectService.findEligibleTesters(email));
        return "manager-new-project";
    }

    @PostMapping("/projects")
    public String createProject(Authentication auth,
                                @RequestParam String pname,
                                @RequestParam String sdate,
                                @RequestParam String desc,
                                @RequestParam(name = "teamMembers", required = false) String[] teamMembers,
                                RedirectAttributes redirectAttributes) {
        projectService.createProject(auth.getName(), pname, desc,
                LocalDate.parse(sdate), teamMembers);
        redirectAttributes.addFlashAttribute("message",
                "Project \"" + pname + "\" created successfully.");
        return Routes.redirect(Routes.MGR_PROJECTS);
    }

    /** Ownership is enforced by ProjectOwnershipInterceptor before this method runs. */
    @GetMapping("/projects/{projectName}")
    public String projectDetails(@PathVariable String projectName,
                                 @RequestParam(defaultValue = "all") String severity,
                                 @RequestParam(defaultValue = "all") String status,
                                 Model model) {
        var bugs = bugService.findBugsByProjectFiltered(projectName, status, severity);
        ProjectHeader header = projectService.findProjectHeader(projectName);
        model.addAttribute("projectName", projectName);
        model.addAttribute("managerName", header.managerName());
        model.addAttribute("startDate",   header.startDate());
        model.addAttribute("team",        projectService.findTeamMembersByProjectName(projectName));
        model.addAttribute("bugs",        bugs);
        model.addAttribute("developers",  projectService.findDevelopersByProjectName(projectName));
        model.addAttribute("severity",    severity);
        model.addAttribute("status",      status);
        model.addAttribute("comments",    commentService.getCommentsGroupedByBug(bugs));
        model.addAttribute("stats",       bugService.getBugStats(projectName));
        return "manager-project-details";
    }

    @PostMapping("/projects/{projectName}/assign")
    public String assignBug(@PathVariable String projectName,
                            @RequestParam String bugId,
                            @RequestParam String devName,
                            Authentication auth,
                            RedirectAttributes redirectAttributes) {
        if (devName == null || devName.isBlank()) {
            redirectAttributes.addFlashAttribute("message", "Please select a developer to assign.");
            return Routes.redirect(Routes.mgrProject(projectName));
        }
        String actorName = userInfoService.getUserName(auth);
        bugService.assignBugToDeveloper(bugId, devName, actorName);
        redirectAttributes.addFlashAttribute("message", "Bug assigned to " + devName + ".");
        return Routes.redirect(Routes.mgrProject(projectName));
    }

    @PostMapping("/projects/{projectName}/bugs/{bugId}/mark-for-close")
    public String markForClose(@PathVariable String projectName,
                               @PathVariable String bugId,
                               Authentication auth,
                               RedirectAttributes redirectAttributes) {
        String actorName = userInfoService.getUserName(auth);
        bugService.markForClose(bugId, actorName);
        redirectAttributes.addFlashAttribute("message", "Bug marked for closing.");
        return Routes.redirect(Routes.mgrProject(projectName));
    }

    @PostMapping("/projects/{projectName}/bugs/{bugId}/close")
    public String closeBug(@PathVariable String projectName,
                           @PathVariable String bugId,
                           Authentication auth,
                           RedirectAttributes redirectAttributes) {
        String closedBy = userInfoService.getUserName(auth);
        int updated = bugService.closeBug(bugId, closedBy);
        redirectAttributes.addFlashAttribute("message",
                updated == 0 ? "Bug could not be closed — mark it first." : "Bug closed.");
        return Routes.redirect(Routes.mgrProject(projectName));
    }
}
