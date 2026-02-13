package org.example.blog_spring.repository;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.example.blog_spring.domain.Post;
import org.example.blog_spring.domain.PostStatus;
import org.example.blog_spring.domain.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

class PostRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    private String unique() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private Long ensureAuthor() {
        var id = unique();
        return userRepository.save(org.example.blog_spring.domain.User.builder()
                .username("author-" + id).email("author-" + id + "@example.com").displayName("Author")
                .passwordHash("").createdAt(Instant.now()).updatedAt(Instant.now()).build()).getId();
    }

    private Tag ensureTag(String name, String slug) {
        var id = unique();
        return tagRepository.save(Tag.builder().name(name + "-" + id).slug(slug + "-" + id)
                .createdAt(Instant.now()).build());
    }

    private Post buildPost(Long authorId, String title, String slug, PostStatus status) {
        var now = Instant.now();
        return Post.builder().authorId(authorId).title(title).content("content").slug(slug + "-" + unique())
                .status(status).createdAt(now).updatedAt(now).build();
    }

    @Test
    void findBySlug_returnsPost() {
        var authorId = ensureAuthor();
        var post = buildPost(authorId, "Title", "slug", PostStatus.PUBLISHED);
        post = postRepository.save(post);

        var found = postRepository.findBySlug(post.getSlug());
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(post.getId());
    }

    @Test
    void findByStatus_returnsOnlyMatchingStatus() {
        var authorId = ensureAuthor();
        var id = unique();
        postRepository.save(buildPost(authorId, "Draft", "draft-" + id, PostStatus.DRAFT));
        postRepository.save(buildPost(authorId, "Published", "pub-" + id, PostStatus.PUBLISHED));

        var page = postRepository.findByStatus(PostStatus.PUBLISHED, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().getStatus()).isEqualTo(PostStatus.PUBLISHED);
    }

    @Test
    void findByAuthorId_returnsPostsForAuthor() {
        var authorId1 = ensureAuthor();
        var id = unique();
        var authorId2 = userRepository.save(org.example.blog_spring.domain.User.builder()
                .username("other-" + id).email("other-" + id + "@example.com").displayName("Other")
                .passwordHash("").createdAt(Instant.now()).updatedAt(Instant.now()).build()).getId();

        postRepository.save(buildPost(authorId1, "A1 Post1", "a1-p1-" + id, PostStatus.DRAFT));
        postRepository.save(buildPost(authorId2, "A2 Post1", "a2-p1-" + id, PostStatus.DRAFT));

        var page = postRepository.findByAuthorId(authorId1, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().getAuthorId()).isEqualTo(authorId1);
    }

    @Test
    void findByTagSlug_returnsTaggedPosts() {
        var authorId = ensureAuthor();
        var tag = ensureTag("Java", "java");
        var otherTag = ensureTag("Other", "other");

        var postWithJava = buildPost(authorId, "With Java", "with-java", PostStatus.PUBLISHED);
        postWithJava.setTags(Set.of(tag));
        postWithJava = postRepository.save(postWithJava);

        var postWithoutJava = buildPost(authorId, "Other", "without-java", PostStatus.PUBLISHED);
        postWithoutJava.setTags(Set.of(otherTag));
        postRepository.save(postWithoutJava);

        var page = postRepository.findByTagSlug(tag.getSlug(), PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().getId()).isEqualTo(postWithJava.getId());
    }

    @Test
    void search_withSearchTerm_filtersByTitleOrContent() {
        var authorId = ensureAuthor();
        var tag = ensureTag("GraphQL", "graphql");

        var post1 = buildPost(authorId, "GraphQL intro", "graphql-intro", PostStatus.PUBLISHED);
        post1.setTags(Set.of(tag));
        postRepository.save(post1);

        var post2 = buildPost(authorId, "Other", "other", PostStatus.PUBLISHED);
        postRepository.save(post2);

        var page = postRepository.search(PostStatus.PUBLISHED, authorId, tag.getSlug(), "graphql",
                PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().getTitle()).containsIgnoringCase("graphql");
    }
}

