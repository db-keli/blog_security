package org.example.blog_spring.dao;

import java.util.Optional;
import org.example.blog_spring.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.List;

@Repository
public class UserDao {

    private final JdbcTemplate jdbc;
    private static final RowMapper<User> MAPPER = (rs, i) -> mapUser(rs);

    public UserDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static User mapUser(ResultSet rs) throws SQLException {
        return User.builder()
                .id(rs.getLong("id"))
                .username(rs.getString("username"))
                .email(rs.getString("email"))
                .passwordHash(rs.getString("password_hash"))
                .displayName(rs.getString("display_name"))
                .bio(rs.getString("bio"))
                .createdAt(toInstant(rs.getTimestamp("created_at")))
                .updatedAt(toInstant(rs.getTimestamp("updated_at")))
                .build();
    }

    public User insert(User user) {
        var sql = """
                INSERT INTO users (username, email, password_hash, display_name, bio, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        var keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            var ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getDisplayName());
            ps.setString(5, user.getBio());
            ps.setObject(6, user.getCreatedAt(), Types.TIMESTAMP_WITH_TIMEZONE);
            ps.setObject(7, user.getUpdatedAt(), Types.TIMESTAMP_WITH_TIMEZONE);
            return ps;
        }, keyHolder);
        user.setId(keyHolder.getKey().longValue());
        return user;
    }

    public Optional<User> findById(Long id) {
        var sql = "SELECT * FROM users WHERE id = ?";
        var list = jdbc.query(sql, MAPPER, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Page<User> findAll(Pageable pageable) {
        var sortOrder = "id ASC";
        if (!pageable.getSort().isEmpty()) {
            var sb = new StringBuilder();
            pageable.getSort().forEach(o ->
                    sb.append(sb.length() > 0 ? ", " : "").append(o.getProperty()).append(" ").append(o.getDirection().name()));
            sortOrder = sb.toString();
        }
        var sql = "SELECT * FROM users ORDER BY " + sortOrder + " LIMIT ? OFFSET ?";
        var offset = pageable.getOffset();
        if (offset > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Page offset exceeds Integer.MAX_VALUE");
        }
        var list = jdbc.query(sql, MAPPER, pageable.getPageSize(), (int) offset);

        var countSql = "SELECT COUNT(*) FROM users";
        var total = jdbc.queryForObject(countSql, Long.class);

        return new PageImpl<>(list, pageable, total != null ? total : 0);
    }

    public boolean existsByEmail(String email) {
        var list = jdbc.query("SELECT 1 FROM users WHERE email = ? LIMIT 1", (rs, i) -> 1, email);
        return !list.isEmpty();
    }

    public boolean existsByUsername(String username) {
        var list = jdbc.query("SELECT 1 FROM users WHERE username = ? LIMIT 1", (rs, i) -> 1, username);
        return !list.isEmpty();
    }

    public Optional<User> findByEmail(String email) {
        var sql = "SELECT * FROM users WHERE email = ?";
        var list = jdbc.query(sql, MAPPER, email);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public void update(User user) {
        var sql = """
                UPDATE users SET username = ?, email = ?, password_hash = ?, display_name = ?, bio = ?, updated_at = ?
                WHERE id = ?
                """;
        jdbc.update(sql, user.getUsername(), user.getEmail(), user.getPasswordHash(),
                user.getDisplayName(), user.getBio(), toTimestamp(user.getUpdatedAt()), user.getId());
    }

    public void deleteById(Long id) {
        jdbc.update("DELETE FROM users WHERE id = ?", id);
    }

    public long count() {
        var r = jdbc.queryForObject("SELECT COUNT(*) FROM users", Long.class);
        return r != null ? r : 0;
    }

    public boolean existsById(Long id) {
        var list = jdbc.query("SELECT 1 FROM users WHERE id = ? LIMIT 1", (rs, i) -> 1, id);
        return !list.isEmpty();
    }

    private static Timestamp toTimestamp(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    private static Instant toInstant(Timestamp ts) {
        return ts == null ? null : ts.toInstant();
    }
}
