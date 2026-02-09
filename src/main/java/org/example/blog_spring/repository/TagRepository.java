package org.example.blog_spring.repository;

import java.util.Optional;
import java.util.Set;
import org.example.blog_spring.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    Optional<Tag> findBySlug(String slug);

    Set<Tag> findByIdIn(Set<Long> ids);
}

