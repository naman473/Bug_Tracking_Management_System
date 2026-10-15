package com.startechies.bugtracker.repository;

import com.startechies.bugtracker.model.BugActivity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class BugActivityRepository {

    private final JdbcTemplate jdbcTemplate;

    public BugActivityRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<BugActivity> findByBugId(String bugId) {
        return jdbcTemplate.query("""
                SELECT * FROM bug_activity WHERE bug_id = ?
                ORDER BY occurred_at DESC
                """,
                (rs, row) -> new BugActivity(
                        rs.getLong("id"),
                        rs.getString("bug_id"),
                        rs.getString("actor_name"),
                        rs.getString("action_type"),
                        rs.getString("detail"),
                        rs.getTimestamp("occurred_at") != null
                                ? rs.getTimestamp("occurred_at").toLocalDateTime() : null
                ), bugId);
    }

    public void record(BugActivity event) {
        jdbcTemplate.update("""
                INSERT INTO bug_activity (bug_id, actor_name, action_type, detail, occurred_at)
                VALUES (?, ?, ?, ?, ?)
                """,
                event.bugId(), event.actorName(), event.actionType(),
                event.detail(), Timestamp.valueOf(LocalDateTime.now()));
    }
}
