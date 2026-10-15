package com.startechies.bugtracker.repository;

import com.startechies.bugtracker.model.*;
import com.startechies.bugtracker.util.FilterUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@Repository
public class BugRepository {

    private final JdbcTemplate jdbcTemplate;

    public BugRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ── Row mapper ───────────────────────────────────────────────────────────

    private Bug mapBug(ResultSet rs, int rowNum) throws SQLException {
        Bug bug = new Bug();
        bug.setBugId(rs.getString("bug_id"));
        bug.setBugTitle(rs.getString("bug_title"));
        bug.setBugDesc(rs.getString("bug_desc"));
        bug.setProjectName(rs.getString("project_name"));
        bug.setCreatedBy(rs.getString("created_by"));
        Date openDate = rs.getDate("open_date");
        bug.setOpenDate(openDate == null ? null : openDate.toLocalDate());
        bug.setAssignedTo(rs.getString("assigned_to"));
        bug.setMarkedForClosing(rs.getBoolean("marked_for_closing"));
        bug.setClosedBy(rs.getString("closed_by"));
        bug.setBugStatus(BugStatus.fromDb(rs.getString("bug_status")));
        bug.setSeverity(Severity.fromDb(rs.getString("severity_level")));
        return bug;
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    public List<Bug> findBugsByProjectFiltered(String projectName, String status, String severity) {
        boolean filterStatus   = !FilterUtils.isUnfiltered(status);
        boolean filterSeverity = !FilterUtils.isUnfiltered(severity);

        StringBuilder sql = new StringBuilder(
                "SELECT * FROM bugs WHERE project_name = ?");
        List<Object> params = new ArrayList<>();
        params.add(projectName);

        if (filterStatus) {
            sql.append(" AND bug_status = ?");
            params.add(status);
        }
        if (filterSeverity) {
            sql.append(" AND severity_level = ?");
            params.add(severity);
        }
        sql.append(" ORDER BY open_date DESC, bug_id");

        return jdbcTemplate.query(sql.toString(), this::mapBug, params.toArray());
    }

    public List<Bug> findAssignedBugsForDeveloper(String emailId) {
        return jdbcTemplate.query("""
                SELECT b.*
                FROM bugs b
                INNER JOIN users u ON u.user_name = b.assigned_to
                WHERE u.email_id = ?
                  AND b.bug_status != ?
                ORDER BY b.open_date DESC, b.bug_id
                """, this::mapBug, emailId, BugStatus.CLOSED.dbValue);
    }

    public List<Bug> findBugsCreatedByTester(String emailId) {
        return jdbcTemplate.query("""
                SELECT b.*
                FROM bugs b
                INNER JOIN users u ON u.user_name = b.created_by
                WHERE u.email_id = ?
                ORDER BY b.project_name, b.open_date DESC, b.bug_id
                """, this::mapBug, emailId);
    }

    // ── Bug stats ────────────────────────────────────────────────────────────

    private BugStats pivotStats(String sql, Object... params) {
        BugStats[] acc = { BugStats.empty() };
        jdbcTemplate.query(sql,
                (org.springframework.jdbc.core.ResultSetExtractor<Void>) rs -> {
                    while (rs.next()) {
                        acc[0] = acc[0].accumulate(
                                BugStatus.fromDb(rs.getString("bug_status")),
                                Severity.fromDb(rs.getString("severity_level")),
                                rs.getLong("cnt"));
                    }
                    return null;
                },
                params);
        return acc[0];
    }

    public BugStats getBugStats(String projectName) {
        return pivotStats("""
                SELECT bug_status, severity_level, COUNT(*) AS cnt
                FROM bugs WHERE project_name = ?
                GROUP BY bug_status, severity_level
                """, projectName);
    }

    public BugStats getBugStatsForProjects(List<String> projectNames) {
        if (projectNames == null || projectNames.isEmpty()) return BugStats.empty();
        String placeholders = String.join(",",
                Collections.nCopies(projectNames.size(), "?"));
        return pivotStats("""
                SELECT bug_status, severity_level, COUNT(*) AS cnt
                FROM bugs WHERE project_name IN (""" + placeholders + """
                ) GROUP BY bug_status, severity_level
                """, projectNames.toArray());
    }

    // ── Writes ───────────────────────────────────────────────────────────────

    private String generateBugId() {
        // Use only first 8 chars of UUID for a readable ID; retry on the rare collision
        String id;
        int attempts = 0;
        do {
            id = "BUG-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
            attempts++;
        } while (attempts < 5 && bugIdExists(id));
        return id;
    }

    private boolean bugIdExists(String id) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM bugs WHERE bug_id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    public String insertBug(CreateBugCommand cmd) {
        String id = generateBugId();
        jdbcTemplate.update("""
                INSERT INTO bugs (bug_id, bug_title, bug_desc, project_name, created_by,
                                  open_date, bug_status, severity_level, marked_for_closing)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                cmd.title(),
                cmd.description(),
                cmd.projectName(),
                cmd.createdBy(),
                Date.valueOf(LocalDate.now()),
                BugStatus.OPEN.dbValue,
                cmd.severity() != null ? cmd.severity().dbValue : null,
                false);
        return id;
    }

    /**
     * Updates a bug only if the caller is the original reporter and the bug is not closed.
     * Returns 0 if the bug doesn't exist, is already closed, or belongs to a different reporter.
     */
    public int updateBugDetails(UpdateBugCommand cmd) {
        return jdbcTemplate.update("""
                UPDATE bugs SET bug_title = ?, bug_desc = ?, severity_level = ?
                WHERE bug_id = ?
                  AND created_by = ?
                  AND bug_status != ?
                """,
                cmd.title(),
                cmd.description(),
                cmd.severity() != null ? cmd.severity().dbValue : null,
                cmd.bugId(),
                cmd.requestedBy(),
                BugStatus.CLOSED.dbValue);
    }

    public int markForClose(String bugId) {
        return jdbcTemplate.update(
                "UPDATE bugs SET marked_for_closing = 1 WHERE bug_id = ? AND bug_status != ?",
                bugId, BugStatus.CLOSED.dbValue);
    }

    public int assignBugToDeveloper(String bugId, String developerName) {
        return jdbcTemplate.update("""
                UPDATE bugs SET assigned_to = ?, bug_status = ?
                WHERE bug_id = ?
                """, developerName, BugStatus.ASSIGNED.dbValue, bugId);
    }

    public int closeBug(String bugId, String closedBy) {
        return jdbcTemplate.update("""
                UPDATE bugs SET closed_by = ?, bug_status = ?
                WHERE bug_id = ? AND marked_for_closing = 1
                """, closedBy, BugStatus.CLOSED.dbValue, bugId);
    }

    /** Bugs assigned to this developer within a specific project, excluding closed. */
    public List<Bug> findAssignedBugsForDeveloperByProject(String emailId, String projectName) {
        return jdbcTemplate.query("""
                SELECT b.*
                FROM bugs b
                INNER JOIN users u ON u.user_name = b.assigned_to
                WHERE u.email_id   = ?
                  AND b.project_name = ?
                  AND b.bug_status != ?
                ORDER BY b.open_date DESC, b.bug_id
                """, this::mapBug, emailId, projectName, BugStatus.CLOSED.dbValue);
    }
}
