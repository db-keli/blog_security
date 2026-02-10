package org.example.blog_spring.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.example.blog_spring.domain.Post;
import org.example.blog_spring.domain.PostStatus;
import org.example.blog_spring.domain.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class PostDao {

    private final JdbcTemplate jdbc;
    private final TagDao tagDao;
    private static final RowMapper<Post> MAPPER = (rs, i) -> mapPost(rs);

    public PostDao(JdbcTemplate jdbc, TagDao tagDao) {
        this.jdbc = jdbc;
        this.tagDao = tagDao;
    }

    private static Post mapPost(ResultSet rs) throws SQLException {
        var status = rs.getString("status");
        return Post.builder()
                .id(rs.getLong("id"))
                .authorId(rs.getLong("author_id"))
                .title(rs.getString("title"))
                .content(rs.getString("content"))
                .slug(rs.getString("slug"))
                .status(status != null ? PostStatus.valueOf(status) : PostStatus.DRAFT)
                .createdAt(toInstant(rs.getTimestamp("created_at")))
                .updatedAt(toInstant(rs.getTimestamp("updated_at")))
                .publishedAt(toInstant(rs.getTimestamp("published_at")))
                .tags(new HashSet<>())
                .build();
    }

    public Post insert(Post post, Set<Long> tagIds) {
        var sql = """
                INSERT INTO posts (author_id, title, content, slug, status, created_at, updated_at, published_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        var keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            var ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, post.getAuthorId());
            ps.setString(2, post.getTitle());
            ps.setString(3, post.getContent());
            ps.setString(4, post.getSlug());
            ps.setString(5, post.getStatus().name());
            ps.setObject(6, post.getCreatedAt(), Types.TIMESTAMP_WITH_TIMEZONE);
            ps.setObject(7, post.getUpdatedAt(), Types.TIMESTAMP_WITH_TIMEZONE);
            ps.setObject(8, post.getPublishedAt(), Types.TIMESTAMP_WITH_TIMEZONE);
            return ps;
        }, keyHolder);
        var id = keyHolder.getKey().longValue();
        post.setId(id);
        if (tagIds != null && !tagIds.isEmpty()) {
            for (var tagId : tagIds) {
                jdbc.update("INSERT INTO post_tags (post_id, tag_id) VALUES (?, ?)", id, tagId);
            }
        }
        return post;
    }

    public Optional<Post> findById(Long id) {
        var list = jdbc.query("SELECT * FROM posts WHERE id = ?", MAPPER, id);
        if (list.isEmpty()) return Optional.empty();
        var post = list.get(0);
        loadTags(post);
        return Optional.of(post);
    }

    public Optional<Post> findBySlug(String slug) {
        var list = jdbc.query("SELECT * FROM posts WHERE slug = ?", MAPPER, slug);
        if (list.isEmpty()) return Optional.empty();
        var post = list.get(0);
        loadTags(post);
        return Optional.of(post);
    }

    private void loadTags(Post post) {
        var tagRows = jdbc.query("SELECT tag_id FROM post_tags WHERE post_id = ?", (rs, i) -> rs.getLong("tag_id"), post.getId());
        if (!tagRows.isEmpty()) {
            var tags = tagDao.findByIdIn(Set.copyOf(tagRows));
            post.setTags(Set.copyOf(tags));
        }
    }

    public Page<Post> findAll(Pageable pageable) {
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
        var list = jdbc.query("SELECT * FROM posts ORDER BY " + sortOrder + " LIMIT ? OFFSET ?",
                MAPPER, pageable.getPageSize(), (int) offset);
        list.forEach(this::loadTags);
        var total = jdbc.queryForObject("SELECT COUNT(*) FROM posts", Long.class);
        return new PageImpl<>(list, pageable, total != null ? total : 0);
    }

    public Page<Post> search(PostStatus status, Long authorId, String tagSlug, String search, Pageable pageable) {
        var where = new StringBuilder("WHERE 1=1");
        var params = new java.util.ArrayList<Object>();
        if (status != null) {
            where.append(" AND p.status = ?");
            params.add(status.name());
        }
        if (authorId != null) {
            where.append(" AND p.author_id = ?");
            params.add(authorId);
        }
        if (tagSlug != null && !tagSlug.isBlank()) {
            where.append(" AND EXISTS (SELECT 1 FROM post_tags pt JOIN tags t ON pt.tag_id = t.id WHERE pt.post_id = p.id AND t.slug = ?)");
            params.add(tagSlug);
        }
        if (search != null && !search.isBlank()) {
            where.append(" AND (LOWER(p.title) LIKE LOWER(?) OR LOWER(p.content) LIKE LOWER(?))");
            var p = "%" + search + "%";
            params.add(p);
            params.add(p);
        }
        var sortOrder = "p.id ASC";
        if (!pageable.getSort().isEmpty()) {
            var sb = new StringBuilder();
            pageable.getSort().forEach(o -> {
                var col = "p." + toColumn(o.getProperty());
                sb.append(sb.length() > 0 ? ", " : "").append(col).append(" ").append(o.getDirection().name());
            });
            sortOrder = sb.toString();
        }
        var offset = pageable.getOffset();
        if (offset > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Page offset exceeds Integer.MAX_VALUE");
        }
        var listSql = "SELECT p.* FROM posts p " + where + " ORDER BY " + sortOrder + " LIMIT ? OFFSET ?";
        params.add(pageable.getPageSize());
        params.add((int) offset);
        var list = jdbc.query(listSql, MAPPER, params.toArray());
        list.forEach(this::loadTags);

        var countSql = "SELECT COUNT(*) FROM posts p " + where;
        var countParams = params.subList(0, params.size() - 2).toArray();
        var total = jdbc.queryForObject(countSql, Long.class, countParams);
        return new PageImpl<>(list, pageable, total != null ? total : 0);
    }

    public void update(Post post, Set<Long> tagIds) {
        jdbc.update("""
                UPDATE posts SET title = ?, content = ?, slug = ?, status = ?, updated_at = ?, published_at = ?
                WHERE id = ?
                """, post.getTitle(), post.getContent(), post.getSlug(), post.getStatus().name(),
                toTimestamp(post.getUpdatedAt()), toTimestamp(post.getPublishedAt()), post.getId());
        jdbc.update("DELETE FROM post_tags WHERE post_id = ?", post.getId());
        if (tagIds != null && !tagIds.isEmpty()) {
            for (var tagId : tagIds) {
                jdbc.update("INSERT INTO post_tags (post_id, tag_id) VALUES (?, ?)", post.getId(), tagId);
            }
        }
    }

    public void deleteById(Long id) {
        jdbc.update("DELETE FROM post_tags WHERE post_id = ?", id);
        jdbc.update("DELETE FROM posts WHERE id = ?", id);
    }

    public boolean existsById(Long id) {
        var list = jdbc.query("SELECT 1 FROM posts WHERE id = ? LIMIT 1", (rs, i) -> 1, id);
        return !list.isEmpty();
    }

    private static Timestamp toTimestamp(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    private static Instant toInstant(Timestamp ts) {
        return ts == null ? null : ts.toInstant();
    }

    private static String toColumn(String property) {
        return switch (property) {
            case "authorId" -> "author_id";
            case "createdAt" -> "created_at";
            case "updatedAt" -> "updated_at";
            case "publishedAt" -> "published_at";
            default -> property;
        };
    }
}
