package org.example.blog_spring.repository;

import java.util.Optional;
import org.example.blog_spring.domain.Post;
import org.example.blog_spring.domain.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findBySlug(String slug);

    Page<Post> findAllByStatus(PostStatus status, Pageable pageable);
}

