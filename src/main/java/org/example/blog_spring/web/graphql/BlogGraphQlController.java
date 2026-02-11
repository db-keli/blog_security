package org.example.blog_spring.web.graphql;

import java.util.List;
import org.example.blog_spring.dto.CommentDto;
import org.example.blog_spring.dto.CreateCommentRequest;
import org.example.blog_spring.dto.CreatePostRequest;
import org.example.blog_spring.dto.CreateReviewRequest;
import org.example.blog_spring.dto.CreateTagRequest;
import org.example.blog_spring.dto.CreateUserRequest;
import org.example.blog_spring.dto.PostDto;
import org.example.blog_spring.dto.ReviewDto;
import org.example.blog_spring.dto.TagDto;
import org.example.blog_spring.dto.UpdateCommentRequest;
import org.example.blog_spring.dto.UpdatePostRequest;
import org.example.blog_spring.dto.UpdateReviewRequest;
import org.example.blog_spring.dto.UpdateTagRequest;
import org.example.blog_spring.dto.UpdateUserRequest;
import org.example.blog_spring.dto.UserDto;
import org.example.blog_spring.service.CommentService;
import org.example.blog_spring.service.PostService;
import org.example.blog_spring.service.ReviewService;
import org.example.blog_spring.service.TagService;
import org.example.blog_spring.service.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class BlogGraphQlController {

    private final UserService userService;
    private final PostService postService;
    private final TagService tagService;
    private final CommentService commentService;
    private final ReviewService reviewService;

    public BlogGraphQlController(
            UserService userService,
            PostService postService,
            TagService tagService,
            CommentService commentService,
            ReviewService reviewService
    ) {
        this.userService = userService;
        this.postService = postService;
        this.tagService = tagService;
        this.commentService = commentService;
        this.reviewService = reviewService;
    }

    // region Users

    @QueryMapping
    public List<UserDto> users(@Argument int page, @Argument int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userService.getUsers(pageable).getContent();
    }

    @QueryMapping
    public UserDto user(@Argument Long id) {
        return userService.getUser(id);
    }

    @MutationMapping
    public UserDto createUser(@Argument("input") CreateUserInput input) {
        var request = new CreateUserRequest(input.username(), input.email(), input.fullName());
        return userService.createUser(request);
    }

    @MutationMapping
    public UserDto updateUser(@Argument Long id, @Argument("input") UpdateUserInput input) {
        var request = new UpdateUserRequest(input.username(), input.email(), input.fullName());
        return userService.updateUser(id, request);
    }

    @MutationMapping
    public Boolean deleteUser(@Argument Long id) {
        userService.deleteUser(id);
        return true;
    }

    // endregion

    // region Posts

    @QueryMapping
    public List<PostDto> posts(
            @Argument int page,
            @Argument int size,
            @Argument Long authorId,
            @Argument String tag,
            @Argument String search,
            @Argument Boolean publishedOnly
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return postService.getPosts(authorId, tag, search, publishedOnly, pageable).getContent();
    }

    @QueryMapping
    public PostDto post(@Argument Long id) {
        return postService.getPost(id);
    }

    @QueryMapping
    public PostDto postBySlug(@Argument String slug) {
        return postService.getPostBySlug(slug);
    }

    @MutationMapping
    public PostDto createPost(@Argument("input") CreatePostInput input) {
        var request = new CreatePostRequest(
                input.authorId(),
                input.title(),
                input.content(),
                input.slug(),
                input.tagIds()
        );
        return postService.createPost(request);
    }

    @MutationMapping
    public PostDto updatePost(@Argument Long id, @Argument("input") UpdatePostInput input) {
        var request = new UpdatePostRequest(
                input.title(),
                input.content(),
                input.slug(),
                input.status() != null ? org.example.blog_spring.domain.PostStatus
                        .valueOf(input.status())
                        : null,
                input.tagIds()
        );
        return postService.updatePost(id, request);
    }

    @MutationMapping
    public Boolean deletePost(@Argument Long id) {
        postService.deletePost(id);
        return true;
    }

    // endregion

    // region Tags

    @QueryMapping
    public List<TagDto> tags(@Argument int page, @Argument int size) {
        Pageable pageable = PageRequest.of(page, size);
        return tagService.getTags(pageable).getContent();
    }

    @QueryMapping
    public TagDto tagBySlug(@Argument String slug) {
        return tagService.getTagBySlug(slug);
    }

    @MutationMapping
    public TagDto createTag(@Argument("input") CreateTagInput input) {
        var request = new CreateTagRequest(input.name(), input.slug(), input.description());
        return tagService.createTag(request);
    }

    @MutationMapping
    public TagDto updateTag(@Argument Long id, @Argument("input") UpdateTagInput input) {
        var request = new UpdateTagRequest(input.name(), input.slug(), input.description());
        return tagService.updateTag(id, request);
    }

    @MutationMapping
    public Boolean deleteTag(@Argument Long id) {
        tagService.deleteTag(id);
        return true;
    }

    // endregion

    // region Comments

    @QueryMapping
    public List<CommentDto> commentsByPost(
            @Argument Long postId,
            @Argument int page,
            @Argument int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return commentService.getCommentsForPost(postId, pageable).getContent();
    }

    @MutationMapping
    public CommentDto createComment(@Argument("input") CreateCommentInput input) {
        var request = new CreateCommentRequest(
                input.postId(),
                input.userId(),
                input.parentId(),
                input.content()
        );
        return commentService.createComment(request);
    }

    @MutationMapping
    public CommentDto updateComment(@Argument Long id, @Argument("input") UpdateCommentInput input) {
        var request = new UpdateCommentRequest(input.content());
        return commentService.updateComment(id, request);
    }

    @MutationMapping
    public Boolean deleteComment(@Argument Long id) {
        commentService.deleteComment(id);
        return true;
    }

    // endregion

    // region Reviews

    @QueryMapping
    public List<ReviewDto> reviewsByPost(
            @Argument Long postId,
            @Argument int page,
            @Argument int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return reviewService.getReviewsForPost(postId, pageable).getContent();
    }

    @MutationMapping
    public ReviewDto createReview(@Argument("input") CreateReviewInput input) {
        var request = new CreateReviewRequest(
                input.postId(),
                input.userId(),
                (short) input.rating(),
                input.title(),
                input.content(),
                input.verified()
        );
        return reviewService.createReview(request);
    }

    @MutationMapping
    public ReviewDto updateReview(@Argument Long id, @Argument("input") UpdateReviewInput input) {
        var request = new UpdateReviewRequest(
                (short) input.rating(),
                input.title(),
                input.content(),
                input.verified()
        );
        return reviewService.updateReview(id, request);
    }

    @MutationMapping
    public Boolean deleteReview(@Argument Long id) {
        reviewService.deleteReview(id);
        return true;
    }

    // endregion

    // region GraphQL input records

    public record CreateUserInput(String username, String email, String fullName) {
    }

    public record UpdateUserInput(String username, String email, String fullName) {
    }

    public record CreatePostInput(
            Long authorId,
            String title,
            String content,
            String slug,
            java.util.Set<Long> tagIds
    ) {
    }

    public record UpdatePostInput(
            String title,
            String content,
            String slug,
            String status,
            java.util.Set<Long> tagIds
    ) {
    }

    public record CreateTagInput(String name, String slug, String description) {
    }

    public record UpdateTagInput(String name, String slug, String description) {
    }

    public record CreateCommentInput(
            Long postId,
            Long userId,
            Long parentId,
            String content
    ) {
    }

    public record UpdateCommentInput(String content) {
    }

    public record CreateReviewInput(
            Long postId,
            Long userId,
            int rating,
            String title,
            String content,
            Boolean verified
    ) {
    }

    public record UpdateReviewInput(
            int rating,
            String title,
            String content,
            Boolean verified
    ) {
    }

    // endregion
}

