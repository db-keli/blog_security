package org.example.blog_spring.repository;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.example.blog_spring.domain.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TagRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private TagRepository tagRepository;

    private String unique() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private Tag buildTag(String name, String slug) {
        return Tag.builder().name(name).slug(slug).description("desc").createdAt(Instant.now())
                .build();
    }

    @Test
    void findBySlug_returnsTag() {
        var id = unique();
        var saved = tagRepository.save(buildTag("Java-" + id, "java-" + id));

        var found = tagRepository.findBySlug("java-" + id);
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    void existsByName_and_existsBySlug() {
        var id = unique();
        tagRepository.save(buildTag("GraphQL-" + id, "graphql-" + id));

        assertThat(tagRepository.existsByName("GraphQL-" + id)).isTrue();
        assertThat(tagRepository.existsBySlug("graphql-" + id)).isTrue();
    }

    @Test
    void findByIdIn_returnsExpectedTags() {
        var id = unique();
        var t1 = tagRepository.save(buildTag("Spring-" + id, "spring-" + id));
        var t2 = tagRepository.save(buildTag("JPA-" + id, "jpa-" + id));

        var result = tagRepository.findByIdIn(Set.of(t1.getId(), t2.getId()));
        assertThat(result).extracting(Tag::getSlug).containsExactlyInAnyOrder("spring-" + id, "jpa-" + id);
    }
}

