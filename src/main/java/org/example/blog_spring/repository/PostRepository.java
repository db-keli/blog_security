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

    Page<Post> findAllByStatus(PostStatus status, Pageable pageable);

    Page<Post> findAllByAuthorId(Long authorId, Pageable pageable);

    Page<Post> findAllByStatusAndAuthorId(PostStatus status, Long authorId, Pageable pageable);

    Page<Post> findAllByTags_Slug(String slug, Pageable pageable);

    Page<Post> findAllByStatusAndTags_Slug(PostStatus status, String slug, Pageable pageable);

    @Query("""
            select p from Post p
            where (:status is null or p.status = :status)
              and (:authorId is null or p.author.id = :authorId)
              and (:tagSlug is null or exists (
                    select 1 from p.tags t where t.slug = :tagSlug
              ))
              and (:search is null
                   or lower(p.title) like lower(concat('%', :search, '%'))
                   or lower(p.content) like lower(concat('%', :search, '%')))
            """)
    Page<Post> search(
            @Param("status") PostStatus status,
            @Param("authorId") Long authorId,
            @Param("tagSlug") String tagSlug,
            @Param("search") String search,
            Pageable pageable
    );
}

