package org.example.blog_spring.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.Optional;

import org.example.blog_spring.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class CommentDao {

    private final JdbcTemplate jdbc;
    private static final RowMapper<Comment> MAPPER = (rs, i) -> mapComment(rs);

    public CommentDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static Comment mapComment(ResultSet rs) throws SQLException {
        return Comment.builder().id(rs.getLong("id")).postId(rs.getLong("post_id"))
                .userId(rs.getLong("user_id")).parentId(rs.getObject("parent_id", Long.class))
                .content(rs.getString("content"))
                .createdAt(toInstant(rs.getTimestamp("created_at")))
                .updatedAt(toInstant(rs.getTimestamp("updated_at"))).build();
    }

    public Comment insert(Comment comment) {
        var sql = """
                INSERT INTO comments (post_id, user_id, parent_id, content, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        var keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            var ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, comment.getPostId());
            ps.setLong(2, comment.getUserId());
            ps.setObject(3, comment.getParentId());
            ps.setString(4, comment.getContent());
            ps.setObject(5, comment.getCreatedAt(), Types.TIMESTAMP_WITH_TIMEZONE);
            ps.setObject(6, comment.getUpdatedAt(), Types.TIMESTAMP_WITH_TIMEZONE);
            return ps;
        }, keyHolder);
        comment.setId(keyHolder.getKey().longValue());
        return comment;
    }

    public Optional<Comment> findById(Long id) {
        var list = jdbc.query("SELECT * FROM comments WHERE id = ?", MAPPER, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Page<Comment> findByPostId(Long postId, Pageable pageable) {
        var sortOrder = "id ASC";
        if (!pageable.getSort().isEmpty()) {
            var sb = new StringBuilder();
            pageable.getSort().forEach(o -> sb.append(sb.length() > 0 ? ", " : "")
                    .append(o.getProperty()).append(" ").append(o.getDirection().name()));
            sortOrder = sb.toString();
        }
        var offset = pageable.getOffset();
        if (offset > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Page offset exceeds Integer.MAX_VALUE");
        }
        var list = jdbc.query(
                "SELECT * FROM comments WHERE post_id = ? ORDER BY " + sortOrder
                        + " LIMIT ? OFFSET ?",
                MAPPER, postId, pageable.getPageSize(), (int) offset);
        var total = jdbc.queryForObject("SELECT COUNT(*) FROM comments WHERE post_id = ?",
                Long.class, postId);
        return new PageImpl<>(list, pageable, total != null ? total : 0);
    }

    public Page<Comment> findByUserId(Long userId, Pageable pageable) {
        var sortOrder = "id ASC";
        if (!pageable.getSort().isEmpty()) {
            var sb = new StringBuilder();
            pageable.getSort().forEach(o -> sb.append(sb.length() > 0 ? ", " : "")
                    .append(o.getProperty()).append(" ").append(o.getDirection().name()));
            sortOrder = sb.toString();
        }
        var offset = pageable.getOffset();
        if (offset > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Page offset exceeds Integer.MAX_VALUE");
        }
        var list = jdbc.query(
                "SELECT * FROM comments WHERE user_id = ? ORDER BY " + sortOrder
                        + " LIMIT ? OFFSET ?",
                MAPPER, userId, pageable.getPageSize(), (int) offset);
        var total = jdbc.queryForObject("SELECT COUNT(*) FROM comments WHERE user_id = ?",
                Long.class, userId);
        return new PageImpl<>(list, pageable, total != null ? total : 0);
    }

    public void update(Comment comment) {
        jdbc.update("UPDATE comments SET content = ?, updated_at = ? WHERE id = ?",
                comment.getContent(), toTimestamp(comment.getUpdatedAt()), comment.getId());
    }

    public void deleteById(Long id) {
        jdbc.update("DELETE FROM comments WHERE id = ?", id);
    }

    public boolean existsById(Long id) {
        var list = jdbc.query("SELECT 1 FROM comments WHERE id = ? LIMIT 1", (rs, i) -> 1, id);
        return !list.isEmpty();
    }

    private static Timestamp toTimestamp(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    private static Instant toInstant(Timestamp ts) {
        return ts == null ? null : ts.toInstant();
    }
}
