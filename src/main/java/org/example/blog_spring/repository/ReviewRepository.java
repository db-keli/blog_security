package org.example.blog_spring.repository;

import java.util.Optional;
import org.example.blog_spring.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByPostIdAndUserId(Long postId, Long userId);

    Page<Review> findByPostId(Long postId, Pageable pageable);

    Page<Review> findByUserId(Long userId, Pageable pageable);
}

