package com.startechies.bugtracker.service;

import com.startechies.bugtracker.model.*;
import com.startechies.bugtracker.repository.BugActivityRepository;
import com.startechies.bugtracker.repository.BugRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BugService {

    private final BugRepository         bugRepository;
    private final BugActivityRepository activityRepository;

    public BugService(BugRepository bugRepository,
                      BugActivityRepository activityRepository) {
        this.bugRepository      = bugRepository;
        this.activityRepository = activityRepository;
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    public List<Bug> findBugsByProjectFiltered(String projectName, String status, String severity) {
        return bugRepository.findBugsByProjectFiltered(projectName, status, severity);
    }

    public List<Bug> findAssignedBugsForDeveloper(String emailId) {
        return bugRepository.findAssignedBugsForDeveloper(emailId);
    }

    /** Bugs assigned to this developer scoped to a single project (excludes closed). */
    public List<Bug> findAssignedBugsForDeveloperByProject(String emailId, String projectName) {
        return bugRepository.findAssignedBugsForDeveloperByProject(emailId, projectName);
    }

    public List<Bug> findBugsCreatedByTester(String emailId) {
        return bugRepository.findBugsCreatedByTester(emailId);
    }

    public BugStats getBugStats(String projectName) {
        return bugRepository.getBugStats(projectName);
    }

    public BugStats getBugStatsForProjects(List<String> projectNames) {
        return bugRepository.getBugStatsForProjects(projectNames);
    }

    public List<BugActivity> getActivity(String bugId) {
        return activityRepository.findByBugId(bugId);
    }

    // ── Commands ─────────────────────────────────────────────────────────────

    /**
     * Reports a bug and returns its generated ID.
     * Accepts a {@link CreateBugCommand} so callers build the command once —
     * no re-wrapping inside the service.
     */
    @Transactional
    public String reportBug(CreateBugCommand cmd) {
        String bugId = bugRepository.insertBug(cmd);
        activityRepository.record(BugActivity.of(bugId, cmd.createdBy(),
                "REPORTED", "Bug reported by " + cmd.createdBy()));
        return bugId;
    }

    @Transactional
    public int assignBugToDeveloper(String bugId, String developerName, String actorName) {
        int updated = bugRepository.assignBugToDeveloper(bugId, developerName);
        if (updated > 0) {
            activityRepository.record(BugActivity.of(bugId, actorName,
                    "ASSIGNED", "Assigned to " + developerName));
        }
        return updated;
    }

    @Transactional
    public int markForClose(String bugId, String actorName) {
        int updated = bugRepository.markForClose(bugId);
        if (updated > 0) {
            activityRepository.record(BugActivity.of(bugId, actorName,
                    "MARKED_FOR_CLOSE", "Marked for closing by " + actorName));
        }
        return updated;
    }

    @Transactional
    public int closeBug(String bugId, String closedBy) {
        int updated = bugRepository.closeBug(bugId, closedBy);
        if (updated > 0) {
            activityRepository.record(BugActivity.of(bugId, closedBy,
                    "CLOSED", "Closed by " + closedBy));
        }
        return updated;
    }

    /**
     * Updates bug metadata. The {@link UpdateBugCommand#requestedBy()} field is
     * checked at the SQL level — only the original reporter can edit.
     */
    @Transactional
    public int editBug(UpdateBugCommand cmd, String actorName) {
        int updated = bugRepository.updateBugDetails(cmd);
        if (updated > 0) {
            activityRepository.record(BugActivity.of(cmd.bugId(), actorName,
                    "EDITED", "Title/description/severity updated"));
        }
        return updated;
    }
}
