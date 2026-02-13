package org.example.blog_spring.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.example.blog_spring.domain.Post;
import org.example.blog_spring.domain.Tag;
import org.example.blog_spring.dto.CreatePostRequest;
import org.example.blog_spring.dto.PostDto;
import org.example.blog_spring.dto.TagSummaryDto;
import org.example.blog_spring.dto.UpdatePostRequest;

public final class PostMapper {

    private PostMapper() {}

    public static Post toEntity(CreatePostRequest request, Long authorId, Set<Tag> tags) {
        var post = new Post(authorId, request.title(), request.content(), request.slug());
        post.setTags(tags != null ? tags : Set.of());
        return post;
    }

    public static void updateEntity(Post post, UpdatePostRequest request, Set<Tag> tags) {
        post.setTitle(request.title());
        post.setContent(request.content());
        post.setSlug(request.slug());
        if (request.status() != null) {
            post.setStatus(request.status());
        }
        post.setTags(tags != null ? tags : Set.of());
        post.setUpdatedAt(java.time.Instant.now());
        if (request.status() == org.example.blog_spring.domain.PostStatus.PUBLISHED
                && post.getPublishedAt() == null) {
            post.setPublishedAt(java.time.Instant.now());
        }
    }

    public static PostDto toDto(Post post) {
        var tagDtos = post.getTags() == null ? Set.<TagSummaryDto>of()
                : post.getTags().stream()
                        .map(tag -> new TagSummaryDto(tag.getId(), tag.getName(), tag.getSlug()))
                        .collect(Collectors.toUnmodifiableSet());
        return new PostDto(post.getId(), post.getAuthorId(), post.getTitle(), post.getContent(),
                post.getSlug(), post.getStatus(), post.getCreatedAt(), post.getUpdatedAt(),
                post.getPublishedAt(), post.getCommentCount(), tagDtos);
    }
}
