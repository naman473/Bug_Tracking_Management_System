package com.startechies.bugtracker.repository;

import com.startechies.bugtracker.model.ImportedUser;
import com.startechies.bugtracker.model.Role;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Repository
public class ImportUserRepository {

    private final JdbcTemplate jdbcTemplate;

    public ImportUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Returns the set of userIds from {@code candidates} that already exist in the DB.
     * Single query regardless of how many candidates are passed.
     */
    public Set<String> findExistingUserIds(Collection<String> candidates) {
        if (candidates == null || candidates.isEmpty()) return Set.of();
        String placeholders = String.join(",", Collections.nCopies(candidates.size(), "?"));
        return new HashSet<>(jdbcTemplate.queryForList(
                "SELECT user_id FROM imported_users WHERE user_id IN (" + placeholders + ")",
                String.class,
                candidates.toArray()));
    }

    public int insert(ImportedUser user) {
        return jdbcTemplate.update("""
                INSERT INTO imported_users (user_id, email_id, role, user_name)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE user_name = VALUES(user_name), role = VALUES(role)
                """,
                user.userId(),
                user.emailId(),
                user.role() != null ? user.role().dbValue : null,
                user.userName());
    }
}
