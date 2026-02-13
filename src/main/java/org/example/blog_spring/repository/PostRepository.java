package org.example.blog_spring.repository;

import java.util.Optional;

import org.example.blog_spring.domain.Post;
import org.example.blog_spring.domain.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

  Optional<Post> findBySlug(String slug);

  Page<Post> findByStatus(PostStatus status, Pageable pageable);

  Page<Post> findByAuthorId(Long authorId, Pageable pageable);

  @Query("""
      SELECT DISTINCT p FROM Post p
      JOIN p.tags t
      WHERE t.slug = :tagSlug
      """)
  Page<Post> findByTagSlug(@Param("tagSlug") String tagSlug, Pageable pageable);

  @Query("""
      SELECT DISTINCT p FROM Post p
      LEFT JOIN p.tags t
      WHERE (:status IS NULL OR p.status = :status)
        AND (:authorId IS NULL OR p.author.id = :authorId)
        AND (:tagSlug IS NULL OR t.slug = :tagSlug)
        AND (
              :search IS NULL
           OR LOWER(CAST(p.title AS string)) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(CAST(p.content AS string)) LIKE LOWER(CONCAT('%', :search, '%'))
        )
      """)
  Page<Post> search(@Param("status") PostStatus status, @Param("authorId") Long authorId,
      @Param("tagSlug") String tagSlug, @Param("search") String search, Pageable pageable);
}

