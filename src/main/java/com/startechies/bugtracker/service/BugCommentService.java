package com.startechies.bugtracker.service;

import com.startechies.bugtracker.model.Bug;
import com.startechies.bugtracker.model.BugComment;
import com.startechies.bugtracker.model.Role;
import com.startechies.bugtracker.repository.BugCommentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BugCommentService {

    private final BugCommentRepository repo;

    public BugCommentService(BugCommentRepository repo) {
        this.repo = repo;
    }

    public List<BugComment> getComments(String bugId) {
        return repo.findByBugId(bugId);
    }

    /**
     * Accepts a {@link Role} enum so the controller resolves the role once
     * and passes it here — no string manipulation between layers.
     */
    public BugComment addComment(String bugId, String authorName, Role role, String body) {
        String cssClass = role != null ? role.cssClass : "";
        return repo.insert(BugComment.of(bugId, authorName, cssClass, body.strip()));
    }

    /** Single bulk query — no N+1. */
    public Map<String, List<BugComment>> getCommentsGroupedByBug(List<Bug> bugs) {
        if (bugs == null || bugs.isEmpty()) return Map.of();
        List<String> bugIds = bugs.stream().map(Bug::getBugId).toList();
        return repo.findByBugIds(bugIds).stream()
                .collect(Collectors.groupingBy(BugComment::bugId));
    }
}
