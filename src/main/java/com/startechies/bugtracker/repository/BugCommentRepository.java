package com.startechies.bugtracker.repository;

import com.startechies.bugtracker.model.BugComment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Repository
public class BugCommentRepository {

    private final JdbcTemplate jdbcTemplate;

    public BugCommentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<BugComment> MAPPER = (ResultSet rs, int row) -> {
        Timestamp ts = rs.getTimestamp("created_at");
        return new BugComment(
                rs.getLong("id"),
                rs.getString("bug_id"),
                rs.getString("author_name"),
                rs.getString("author_role"),
                rs.getString("body"),
                ts == null ? null : ts.toLocalDateTime()
        );
    };

    public List<BugComment> findByBugId(String bugId) {
        return jdbcTemplate.query("""
                SELECT * FROM bug_comments WHERE bug_id = ? ORDER BY created_at
                """, MAPPER, bugId);
    }

    /** Single query for all comments belonging to a set of bug IDs — avoids N+1. */
    public List<BugComment> findByBugIds(Collection<String> bugIds) {
        if (bugIds == null || bugIds.isEmpty()) return Collections.emptyList();
        String placeholders = String.join(",", Collections.nCopies(bugIds.size(), "?"));
        return jdbcTemplate.query(
                "SELECT * FROM bug_comments WHERE bug_id IN (" + placeholders
                        + ") ORDER BY bug_id, created_at",
                MAPPER, bugIds.toArray());
    }

    /**
     * Inserts a comment and returns a copy enriched with the generated id and insertion time.
     * Uses {@code LocalDateTime.now()} for the timestamp — avoids a second SELECT roundtrip
     * since the app server and DB are expected to share the same clock (both configured to UTC
     * via {@code serverTimezone=UTC} in the datasource URL).
     */
    public BugComment insert(BugComment comment) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement("""
                    INSERT INTO bug_comments (bug_id, author_name, author_role, body, created_at)
                    VALUES (?, ?, ?, ?, ?)
                    """, new String[]{"id"});
            ps.setString(1, comment.bugId());
            ps.setString(2, comment.authorName());
            ps.setString(3, comment.authorCssClass());
            ps.setString(4, comment.body());
            ps.setTimestamp(5, Timestamp.valueOf(now));
            return ps;
        }, keyHolder);

        long id = keyHolder.getKey() != null ? keyHolder.getKey().longValue() : -1;
        return comment.withIdAndTime(id, now);
    }
}
