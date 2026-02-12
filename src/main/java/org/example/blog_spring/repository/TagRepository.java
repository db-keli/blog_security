package org.example.blog_spring.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.example.blog_spring.domain.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findBySlug(String slug);

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    @Override
    Page<Tag> findAll(Pageable pageable);

    List<Tag> findByIdIn(Set<Long> ids);
}

