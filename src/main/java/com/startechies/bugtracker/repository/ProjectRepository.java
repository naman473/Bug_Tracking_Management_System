package com.startechies.bugtracker.repository;

import com.startechies.bugtracker.model.Project;
import com.startechies.bugtracker.model.ProjectHeader;
import com.startechies.bugtracker.model.ProjectState;
import com.startechies.bugtracker.model.Role;
import com.startechies.bugtracker.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

/**
 * Handles projects, team membership, and project-scoped user queries.
 * No dependency on {@link UserRepository} — all queries are self-contained.
 */
@Repository
public class ProjectRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProjectRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ── Row mappers ──────────────────────────────────────────────────────────

    private Project mapProject(ResultSet rs, int rowNum) throws SQLException {
        Date startDate = rs.getDate("start_date");
        return new Project(
                rs.getString("project_id"),
                rs.getString("project_name"),
                rs.getString("project_desc"),
                startDate == null ? null : startDate.toLocalDate(),
                ProjectState.fromDb(rs.getString("project_state"))
        );
    }

    private static final RowMapper<User> USER_MAPPER = UserRepository.USER_MAPPER;

    // ── Project queries ──────────────────────────────────────────────────────

    public List<Project> findProjectsForUserEmail(String emailId) {
        return jdbcTemplate.query("""
                SELECT p.*
                FROM project p
                INNER JOIN team t ON t.project_id = p.project_id
                INNER JOIN users u ON u.user_id = t.user_id
                WHERE u.email_id = ?
                ORDER BY p.project_name
                """, this::mapProject, emailId);
    }

    public ProjectHeader findProjectHeader(String projectName) {
        ProjectHeader result = jdbcTemplate.query("""
                SELECT u.user_name  AS manager_name,
                       p.start_date AS start_date
                FROM project p
                LEFT JOIN team t  ON t.project_id = p.project_id AND t.role = ?
                LEFT JOIN users u ON u.user_id     = t.user_id
                WHERE p.project_name = ?
                LIMIT 1
                """,
                rs -> rs.next()
                        ? new ProjectHeader(
                                rs.getString("manager_name"),
                                rs.getDate("start_date") != null
                                        ? rs.getDate("start_date").toLocalDate() : null)
                        : null,
                Role.PROJECT_MANAGER.dbValue, projectName);
        return result != null ? result : ProjectHeader.empty();
    }

    public boolean ownsProject(String projectName, String managerEmail) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM project p
                INNER JOIN team t  ON t.project_id = p.project_id
                INNER JOIN users u ON u.user_id     = t.user_id
                WHERE p.project_name = ? AND u.email_id = ? AND t.role = ?
                """, Integer.class, projectName, managerEmail, Role.PROJECT_MANAGER.dbValue);
        return count != null && count > 0;
    }

    // ── Team queries ─────────────────────────────────────────────────────────

    /**
     * Returns developers with zero projects on their counter — eligible to be added to a new project.
     */
    public List<User> findEligibleDevelopers() {
        return jdbcTemplate.query("""
                SELECT * FROM users
                WHERE role = ? AND project_counter < 2
                ORDER BY user_name
                """, USER_MAPPER, Role.DEVELOPER.dbValue);
    }

    /**
     * Two queries instead of N+1: co-project testers first, then fresh testers.
     * Deduplicates and sorts alphabetically.
     */
    public List<String> findEligibleTesters(String projectManagerEmailId) {
        List<String> fromSameProjects = jdbcTemplate.queryForList("""
                SELECT DISTINCT u.user_name
                FROM users u
                INNER JOIN team t  ON t.user_id    = u.user_id
                INNER JOIN team tm ON tm.project_id = t.project_id
                INNER JOIN users m ON m.user_id     = tm.user_id
                WHERE m.email_id = ? AND u.role = ? AND u.project_counter < 2
                ORDER BY u.user_name
                """, String.class, projectManagerEmailId, Role.TESTER.dbValue);

        List<String> freshTesters = jdbcTemplate.queryForList("""
                SELECT user_name FROM users
                WHERE role = ? AND project_counter < 2
                ORDER BY user_name
                """, String.class, Role.TESTER.dbValue);

        LinkedHashSet<String> merged = new LinkedHashSet<>(fromSameProjects);
        merged.addAll(freshTesters);
        return merged.stream().sorted().toList();
    }

    /**
     * Returns team members for a project ordered by role (Manager → Tester → Developer),
     * then alphabetically. Role values come from the Role enum — not user input — so
     * string interpolation here is safe.
     */
    public List<User> findTeamMembersByProjectName(String projectName) {
        String sql = "SELECT u.* FROM users u"
                + " INNER JOIN team t    ON t.user_id    = u.user_id"
                + " INNER JOIN project p ON p.project_id = t.project_id"
                + " WHERE p.project_name = ?"
                + " ORDER BY CASE u.role"
                + " WHEN '" + Role.PROJECT_MANAGER.dbValue + "' THEN 1"
                + " WHEN '" + Role.TESTER.dbValue + "' THEN 2"
                + " ELSE 3 END, u.user_name";
        return jdbcTemplate.query(sql, USER_MAPPER, projectName);
    }

    public List<User> findDevelopersByProjectName(String projectName) {
        return jdbcTemplate.query("""
                SELECT DISTINCT u.*
                FROM users u
                INNER JOIN team t ON t.user_id = u.user_id
                INNER JOIN project p ON p.project_id = t.project_id
                WHERE u.role = ? AND p.project_name = ?
                ORDER BY u.user_name
                """, USER_MAPPER, Role.DEVELOPER.dbValue, projectName);
    }

    // ── Writes ───────────────────────────────────────────────────────────────

    private String generateProjectId() {
        return "PRJ-" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    @Transactional
    public Project addProject(Project project, String managerEmail) {
        Project withId = project.withId(generateProjectId());

        jdbcTemplate.update("""
                INSERT INTO project (project_id, project_name, project_desc, start_date, project_state)
                VALUES (?, ?, ?, ?, ?)
                """,
                withId.projectId(), withId.projectName(), withId.projectDesc(),
                Date.valueOf(withId.startDate()), withId.projectState().dbValue);

        String managerUserId = jdbcTemplate.query(
                "SELECT user_id FROM users WHERE email_id = ?",
                rs -> rs.next() ? rs.getString(1) : null, managerEmail);

        if (managerUserId != null) {
            jdbcTemplate.update(
                    "INSERT INTO team (user_id, project_id, role) VALUES (?, ?, ?)",
                    managerUserId, withId.projectId(), Role.PROJECT_MANAGER.dbValue);
            jdbcTemplate.update(
                    "UPDATE users SET project_counter = project_counter + 1 WHERE user_id = ?",
                    managerUserId);
        }
        return withId;
    }

    @Transactional
    public void assignTeamMembers(List<String> usernames, String projectId) {
        if (usernames == null || usernames.isEmpty()) return;

        String placeholders = String.join(",", Collections.nCopies(usernames.size(), "?"));
        List<User> users = jdbcTemplate.query(
                "SELECT * FROM users WHERE user_name IN (" + placeholders + ")",
                USER_MAPPER, usernames.toArray());

        if (users.isEmpty()) return;

        List<Object> checkParams = new ArrayList<>();
        checkParams.add(projectId);
        users.forEach(u -> checkParams.add(u.getUserId()));
        String checkSql = "SELECT user_id FROM team WHERE project_id = ? AND user_id IN ("
                + String.join(",", Collections.nCopies(users.size(), "?")) + ")";
        List<String> alreadyAssigned = jdbcTemplate.queryForList(
                checkSql, String.class, checkParams.toArray());

        List<User> toAdd = users.stream()
                .filter(u -> !alreadyAssigned.contains(u.getUserId()))
                .toList();

        if (toAdd.isEmpty()) return;

        jdbcTemplate.batchUpdate(
                "INSERT INTO team (user_id, project_id, role) VALUES (?, ?, ?)",
                toAdd, toAdd.size(),
                (ps, u) -> {
                    ps.setString(1, u.getUserId());
                    ps.setString(2, projectId);
                    ps.setString(3, u.getRole().dbValue);
                });

        jdbcTemplate.batchUpdate(
                "UPDATE users SET project_counter = project_counter + 1 WHERE user_id = ?",
                toAdd, toAdd.size(),
                (ps, u) -> ps.setString(1, u.getUserId()));
    }
}
