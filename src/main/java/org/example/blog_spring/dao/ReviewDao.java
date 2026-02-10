package org.example.blog_spring.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.example.blog_spring.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class ReviewDao {

    private final JdbcTemplate jdbc;
    private static final RowMapper<Review> MAPPER = (rs, i) -> mapReview(rs);

    public ReviewDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static Review mapReview(ResultSet rs) throws SQLException {
        return Review.builder()
                .id(rs.getLong("id"))
                .postId(rs.getLong("post_id"))
                .userId(rs.getLong("user_id"))
                .rating(rs.getShort("rating"))
                .title(rs.getString("title"))
                .content(rs.getString("content"))
                .verified(rs.getBoolean("is_verified"))
                .createdAt(toInstant(rs.getTimestamp("created_at")))
                .updatedAt(toInstant(rs.getTimestamp("updated_at")))
                .build();
    }

    public Review insert(Review review) {
        var sql = """
                INSERT INTO reviews (post_id, user_id, rating, title, content, is_verified, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        var keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            var ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, review.getPostId());
            ps.setLong(2, review.getUserId());
            ps.setShort(3, review.getRating());
            ps.setString(4, review.getTitle());
            ps.setString(5, review.getContent());
            ps.setBoolean(6, review.isVerified());
            ps.setObject(7, review.getCreatedAt(), Types.TIMESTAMP_WITH_TIMEZONE);
            ps.setObject(8, review.getUpdatedAt(), Types.TIMESTAMP_WITH_TIMEZONE);
            return ps;
        }, keyHolder);
        review.setId(keyHolder.getKey().longValue());
        return review;
    }

    public Optional<Review> findById(Long id) {
        var list = jdbc.query("SELECT * FROM reviews WHERE id = ?", MAPPER, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<Review> findByPostIdAndUserId(Long postId, Long userId) {
        var list = jdbc.query("SELECT * FROM reviews WHERE post_id = ? AND user_id = ?", MAPPER, postId, userId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Page<Review> findByPostId(Long postId, Pageable pageable) {
        var sortOrder = "id ASC";
        if (!pageable.getSort().isEmpty()) {
            var sb = new StringBuilder();
            pageable.getSort().forEach(o ->
                    sb.append(sb.length() > 0 ? ", " : "").append(o.getProperty()).append(" ").append(o.getDirection().name()));
            sortOrder = sb.toString();
        }
        var offset = pageable.getOffset();
        if (offset > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Page offset exceeds Integer.MAX_VALUE");
        }
        var list = jdbc.query("SELECT * FROM reviews WHERE post_id = ? ORDER BY " + sortOrder + " LIMIT ? OFFSET ?",
                MAPPER, postId, pageable.getPageSize(), (int) offset);
        var total = jdbc.queryForObject("SELECT COUNT(*) FROM reviews WHERE post_id = ?", Long.class, postId);
        return new PageImpl<>(list, pageable, total != null ? total : 0);
    }

    public Page<Review> findByUserId(Long userId, Pageable pageable) {
        var sortOrder = "id ASC";
        if (!pageable.getSort().isEmpty()) {
            var sb = new StringBuilder();
            pageable.getSort().forEach(o ->
                    sb.append(sb.length() > 0 ? ", " : "").append(o.getProperty()).append(" ").append(o.getDirection().name()));
            sortOrder = sb.toString();
        }
        var offset = pageable.getOffset();
        if (offset > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Page offset exceeds Integer.MAX_VALUE");
        }
        var list = jdbc.query("SELECT * FROM reviews WHERE user_id = ? ORDER BY " + sortOrder + " LIMIT ? OFFSET ?",
                MAPPER, userId, pageable.getPageSize(), (int) offset);
        var total = jdbc.queryForObject("SELECT COUNT(*) FROM reviews WHERE user_id = ?", Long.class, userId);
        return new PageImpl<>(list, pageable, total != null ? total : 0);
    }

    public void update(Review review) {
        jdbc.update("""
                UPDATE reviews SET rating = ?, title = ?, content = ?, is_verified = ?, updated_at = ?
                WHERE id = ?
                """, review.getRating(), review.getTitle(), review.getContent(), review.isVerified(),
                toTimestamp(review.getUpdatedAt()), review.getId());
    }

    public void deleteById(Long id) {
        jdbc.update("DELETE FROM reviews WHERE id = ?", id);
    }

    public boolean existsById(Long id) {
        var list = jdbc.query("SELECT 1 FROM reviews WHERE id = ? LIMIT 1", (rs, i) -> 1, id);
        return !list.isEmpty();
    }

    private static Timestamp toTimestamp(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    private static Instant toInstant(Timestamp ts) {
        return ts == null ? null : ts.toInstant();
    }
}
