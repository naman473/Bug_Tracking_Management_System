package com.startechies.bugtracker.controller;

import com.startechies.bugtracker.model.AppUserDetails;
import com.startechies.bugtracker.model.Role;
import com.startechies.bugtracker.service.BugCommentService;
import com.startechies.bugtracker.service.UserInfoService;
import com.startechies.bugtracker.util.Routes;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class BugCommentController {

    private final BugCommentService commentService;
    private final UserInfoService   userInfoService;

    public BugCommentController(BugCommentService commentService, UserInfoService userInfoService) {
        this.commentService  = commentService;
        this.userInfoService = userInfoService;
    }

    @PostMapping("/bugs/{bugId}/comments")
    public String addComment(@PathVariable String bugId,
                             @RequestParam String body,
                             @RequestParam(required = false) String returnTo,
                             Authentication auth,
                             RedirectAttributes redirectAttributes) {
        // Read Role directly from AppUserDetails principal — no stream needed
        Role role = auth.getPrincipal() instanceof AppUserDetails d ? d.getRole() : null;

        if (body == null || body.isBlank()) {
            redirectAttributes.addFlashAttribute("commentError", "Comment cannot be empty.");
        } else {
            String authorName = userInfoService.getUserName(auth);
            commentService.addComment(bugId, authorName, role, body);
            redirectAttributes.addFlashAttribute("commentSuccess", "Comment added.");
        }
        return Routes.redirect(safeReturnPath(returnTo, role));
    }

    private String safeReturnPath(String returnTo, Role role) {
        if (returnTo != null
                && (returnTo.startsWith("/manager/")
                        || returnTo.startsWith("/developer/")
                        || returnTo.startsWith("/tester/"))) {
            return returnTo;
        }
        return role != null ? role.dashboardPath : Routes.HOME;
    }
}
