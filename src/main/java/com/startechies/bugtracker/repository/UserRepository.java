package com.startechies.bugtracker.repository;

import com.startechies.bugtracker.model.Role;
import com.startechies.bugtracker.model.User;
import com.startechies.bugtracker.model.UserDisplay;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Handles user account operations — registration, authentication, profile.
 * Team membership and project assignment live in {@link ProjectRepository}.
 */
@Repository
public class UserRepository {

    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    static final RowMapper<User> USER_MAPPER = (ResultSet rs, int row) -> {
        User u = new User();
        u.setUserId(rs.getString("user_id"));
        u.setEmailId(rs.getString("email_id"));
        u.setRole(Role.fromDb(rs.getString("role")));
        u.setUserName(rs.getString("user_name"));
        u.setPassword(rs.getString("password"));
        Timestamp ts = rs.getTimestamp("last_login");
        u.setLastLogin(ts == null ? null : ts.toLocalDateTime());
        int counter = rs.getInt("project_counter");
        u.setProjectCounter(rs.wasNull() ? 0 : counter);
        return u;
    };

    // ── Single-user queries ──────────────────────────────────────────────────

    public Optional<User> findByEmail(String email) {
        return jdbcTemplate.query(
                "SELECT * FROM users WHERE email_id = ?", USER_MAPPER, email)
                .stream().findFirst();
    }

    public UserDisplay getDisplayInfo(String email) {
        return findByEmail(email)
                .map(u -> new UserDisplay(
                        u.getUserName(),
                        u.getLastLogin() == null ? "First login"
                                                 : u.getLastLogin().format(DISPLAY_FMT),
                        u.getProjectCounter()))
                .orElseGet(() -> new UserDisplay(email, "First login", 0));
    }

    /** Queries the imported_users allowlist — used during registration. */
    public Optional<User> findImportedByEmail(String email) {
        return jdbcTemplate.query("""
                SELECT user_id, email_id, role, user_name,
                       NULL AS password, NULL AS last_login, 0 AS project_counter
                FROM imported_users WHERE email_id = ?
                """, USER_MAPPER, email)
                .stream().findFirst();
    }

    // ── Writes ───────────────────────────────────────────────────────────────

    public int insertRegisteredUser(User user) {
        return jdbcTemplate.update("""
                INSERT INTO users (user_id, user_name, role, email_id, password, project_counter)
                VALUES (?, ?, ?, ?, ?, 0)
                """,
                user.getUserId(), user.getUserName(),
                user.getRole() != null ? user.getRole().dbValue : null,
                user.getEmailId(), user.getPassword());
    }

    public int updateLastLogin(String email, LocalDateTime time) {
        return jdbcTemplate.update(
                "UPDATE users SET last_login = ? WHERE email_id = ?",
                time != null ? Timestamp.valueOf(time) : null, email);
    }

    public int updatePassword(String email, String encodedPassword) {
        return jdbcTemplate.update(
                "UPDATE users SET password = ? WHERE email_id = ?", encodedPassword, email);
    }
}
