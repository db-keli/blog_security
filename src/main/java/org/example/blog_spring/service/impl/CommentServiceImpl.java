package org.example.blog_spring.service.impl;

import org.example.blog_spring.dao.CommentDao;
import org.example.blog_spring.dao.PostDao;
import org.example.blog_spring.dao.UserDao;
import org.example.blog_spring.dto.CommentDto;
import org.example.blog_spring.dto.CreateCommentRequest;
import org.example.blog_spring.dto.UpdateCommentRequest;
import org.example.blog_spring.exception.CommentNotFoundException;
import org.example.blog_spring.exception.PostNotFoundException;
import org.example.blog_spring.exception.UserNotFoundException;
import org.example.blog_spring.mapper.CommentMapper;
import org.example.blog_spring.service.CommentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentDao commentDao;
    private final PostDao postDao;
    private final UserDao userDao;

    public CommentServiceImpl(CommentDao commentDao, PostDao postDao, UserDao userDao) {
        this.commentDao = commentDao;
        this.postDao = postDao;
        this.userDao = userDao;
    }

    @Override
    public CommentDto createComment(CreateCommentRequest request) {
        if (!postDao.existsById(request.postId())) {
            throw new PostNotFoundException(request.postId());
        }
        if (!userDao.existsById(request.userId())) {
            throw new UserNotFoundException(request.userId());
        }
        if (request.parentId() != null && !commentDao.existsById(request.parentId())) {
            throw new CommentNotFoundException(request.parentId());
        }

        var comment = CommentMapper.toEntity(request);
        var saved = commentDao.insert(comment);
        return CommentMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDto getComment(Long id) {
        var comment = commentDao.findById(id).orElseThrow(() -> new CommentNotFoundException(id));
        return CommentMapper.toDto(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentDto> getCommentsForPost(Long postId, Pageable pageable) {
        return commentDao.findByPostId(postId, pageable).map(CommentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentDto> getCommentsForUser(Long userId, Pageable pageable) {
        return commentDao.findByUserId(userId, pageable).map(CommentMapper::toDto);
    }

    @Override
    public CommentDto updateComment(Long id, UpdateCommentRequest request) {
        var comment = commentDao.findById(id).orElseThrow(() -> new CommentNotFoundException(id));

        CommentMapper.updateEntity(comment, request);
        commentDao.update(comment);
        return CommentMapper.toDto(comment);
    }

    @Override
    public void deleteComment(Long id) {
        if (!commentDao.existsById(id)) {
            throw new CommentNotFoundException(id);
        }
        commentDao.deleteById(id);
    }
}
