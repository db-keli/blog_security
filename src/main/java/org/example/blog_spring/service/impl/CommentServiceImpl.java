package org.example.blog_spring.service.impl;

import org.example.blog_spring.dto.CommentDto;
import org.example.blog_spring.dto.CreateCommentRequest;
import org.example.blog_spring.dto.UpdateCommentRequest;
import org.example.blog_spring.exception.CommentNotFoundException;
import org.example.blog_spring.exception.PostNotFoundException;
import org.example.blog_spring.exception.UserNotFoundException;
import org.example.blog_spring.mapper.CommentMapper;
import org.example.blog_spring.repository.CommentRepository;
import org.example.blog_spring.repository.PostRepository;
import org.example.blog_spring.repository.UserRepository;
import org.example.blog_spring.service.CommentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentServiceImpl(CommentRepository commentRepository, PostRepository postRepository,
            UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Override
    public CommentDto createComment(CreateCommentRequest request) {
        var post = postRepository.findById(request.postId())
                .orElseThrow(() -> new PostNotFoundException(request.postId()));
        if (!userRepository.existsById(request.userId())) {
            throw new UserNotFoundException(request.userId());
        }
        if (request.parentId() != null && !commentRepository.existsById(request.parentId())) {
            throw new CommentNotFoundException(request.parentId());
        }

        var comment = CommentMapper.toEntity(request);
        var saved = commentRepository.save(comment);

        var newCount = commentRepository.countByPostId(request.postId());
        post.setCommentCount(newCount);
        postRepository.save(post);

        return CommentMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDto getComment(Long id) {
        var comment =
                commentRepository.findById(id).orElseThrow(() -> new CommentNotFoundException(id));
        return CommentMapper.toDto(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentDto> getCommentsForPost(Long postId, Pageable pageable) {
        return commentRepository.findByPostId(postId, pageable).map(CommentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentDto> getCommentsForUser(Long userId, Pageable pageable) {
        return commentRepository.findByUserId(userId, pageable).map(CommentMapper::toDto);
    }

    @Override
    public CommentDto updateComment(Long id, UpdateCommentRequest request) {
        var comment =
                commentRepository.findById(id).orElseThrow(() -> new CommentNotFoundException(id));

        CommentMapper.updateEntity(comment, request);
        var saved = commentRepository.save(comment);
        return CommentMapper.toDto(saved);
    }

    @Override
    public void deleteComment(Long id) {
        var comment =
                commentRepository.findById(id).orElseThrow(() -> new CommentNotFoundException(id));

        var postId = comment.getPostId();

        commentRepository.delete(comment);

        var post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        var newCount = commentRepository.countByPostId(postId);
        post.setCommentCount(newCount);
        postRepository.save(post);
    }
}
