package com.startechies.bugtracker.controller;

import com.startechies.bugtracker.model.*;
import com.startechies.bugtracker.service.*;
import com.startechies.bugtracker.util.Routes;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tester")
public class TesterController {

    private final BugService        bugService;
    private final ProjectService    projectService;
    private final UserInfoService   userInfoService;
    private final BugCommentService commentService;

    public TesterController(BugService bugService, ProjectService projectService,
                            UserInfoService userInfoService, BugCommentService commentService) {
        this.bugService      = bugService;
        this.projectService  = projectService;
        this.userInfoService = userInfoService;
        this.commentService  = commentService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        model.addAttribute("bugs", bugService.findBugsCreatedByTester(auth.getName()));
        return "tester-dashboard";
    }

    @GetMapping("/bugs")
    public String bugs(Authentication auth, Model model) {
        var bugs = bugService.findBugsCreatedByTester(auth.getName());
        model.addAttribute("testerBugList", bugs);
        model.addAttribute("comments",      commentService.getCommentsGroupedByBug(bugs));
        return "tester-bugs";
    }

    @GetMapping("/report-bug")
    public String reportBugPage(Authentication auth, Model model) {
        model.addAttribute("projects", projectService.findProjectsForUserEmail(auth.getName()));
        return "tester-report-bug";
    }

    @PostMapping("/report-bug")
    public String reportBug(Authentication auth,
                            @RequestParam String pname,
                            @RequestParam String bugtitle,
                            @RequestParam String descbug,
                            @RequestParam String severity,
                            RedirectAttributes redirectAttributes) {
        String userName = userInfoService.getUserName(auth);
        Severity parsedSeverity = Severity.fromDb(severity);
        if (parsedSeverity == null) parsedSeverity = Severity.MAJOR; // safe default if invalid value submitted
        CreateBugCommand cmd = new CreateBugCommand(
                pname, userName, bugtitle, descbug, parsedSeverity);
        bugService.reportBug(cmd);
        redirectAttributes.addFlashAttribute("message", "Bug reported successfully.");
        return Routes.redirect(Routes.TST_BUGS);
    }

    /**
     * Only succeeds if the tester is the original reporter — enforced at the SQL level
     * via the {@code created_by = ?} guard in
     * {@link com.startechies.bugtracker.repository.BugRepository#updateBugDetails}.
     */
    @PostMapping("/bugs/{bugId}/edit")
    public String editBug(@PathVariable String bugId,
                          @RequestParam String bugtitle,
                          @RequestParam String descbug,
                          @RequestParam String severity,
                          Authentication auth,
                          RedirectAttributes redirectAttributes) {
        String actorName = userInfoService.getUserName(auth);
        UpdateBugCommand cmd = new UpdateBugCommand(
                bugId, actorName, bugtitle, descbug, Severity.fromDb(severity));
        int updated = bugService.editBug(cmd, actorName);
        redirectAttributes.addFlashAttribute("message",
                updated > 0 ? "Bug updated." : "Could not update — bug may be closed or not yours.");
        return Routes.redirect(Routes.TST_BUGS);
    }
}
