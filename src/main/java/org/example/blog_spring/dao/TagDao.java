package org.example.blog_spring.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.example.blog_spring.domain.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class TagDao {

    private final JdbcTemplate jdbc;
    private static final RowMapper<Tag> MAPPER = (rs, i) -> mapTag(rs);

    public TagDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static Tag mapTag(ResultSet rs) throws SQLException {
        return Tag.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .slug(rs.getString("slug"))
                .description(rs.getString("description"))
                .createdAt(toInstant(rs.getTimestamp("created_at")))
                .build();
    }

    public Tag insert(Tag tag) {
        var sql = "INSERT INTO tags (name, slug, description, created_at) VALUES (?, ?, ?, ?)";
        var keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            var ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, tag.getName());
            ps.setString(2, tag.getSlug());
            ps.setString(3, tag.getDescription());
            ps.setObject(4, tag.getCreatedAt(), Types.TIMESTAMP_WITH_TIMEZONE);
            return ps;
        }, keyHolder);
        tag.setId(keyHolder.getKey().longValue());
        return tag;
    }

    public Optional<Tag> findById(Long id) {
        var list = jdbc.query("SELECT * FROM tags WHERE id = ?", MAPPER, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<Tag> findBySlug(String slug) {
        var list = jdbc.query("SELECT * FROM tags WHERE slug = ?", MAPPER, slug);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public boolean existsByName(String name) {
        var list = jdbc.query("SELECT 1 FROM tags WHERE name = ? LIMIT 1", (rs, i) -> 1, name);
        return !list.isEmpty();
    }

    public boolean existsBySlug(String slug) {
        var list = jdbc.query("SELECT 1 FROM tags WHERE slug = ? LIMIT 1", (rs, i) -> 1, slug);
        return !list.isEmpty();
    }

    public Page<Tag> findAll(Pageable pageable) {
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
        var list = jdbc.query("SELECT * FROM tags ORDER BY " + sortOrder + " LIMIT ? OFFSET ?",
                MAPPER, pageable.getPageSize(), (int) offset);
        var total = jdbc.queryForObject("SELECT COUNT(*) FROM tags", Long.class);
        return new PageImpl<>(list, pageable, total != null ? total : 0);
    }

    public List<Tag> findByIdIn(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        var placeholders = ids.stream().map(i -> "?").reduce((a, b) -> a + "," + b).orElse("");
        var sql = "SELECT * FROM tags WHERE id IN (" + placeholders + ")";
        return jdbc.query(sql, MAPPER, ids.toArray());
    }

    public void update(Tag tag) {
        jdbc.update("UPDATE tags SET name = ?, slug = ?, description = ? WHERE id = ?",
                tag.getName(), tag.getSlug(), tag.getDescription(), tag.getId());
    }

    public void deleteById(Long id) {
        jdbc.update("DELETE FROM tags WHERE id = ?", id);
    }

    public boolean existsById(Long id) {
        var list = jdbc.query("SELECT 1 FROM tags WHERE id = ? LIMIT 1", (rs, i) -> 1, id);
        return !list.isEmpty();
    }

    private static Instant toInstant(Timestamp ts) {
        return ts == null ? null : ts.toInstant();
    }
}
