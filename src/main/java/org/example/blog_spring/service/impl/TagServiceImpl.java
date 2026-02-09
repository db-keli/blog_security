package org.example.blog_spring.service.impl;

import org.example.blog_spring.dto.CreateTagRequest;
import org.example.blog_spring.dto.TagDto;
import org.example.blog_spring.dto.UpdateTagRequest;
import org.example.blog_spring.mapper.TagMapper;
import org.example.blog_spring.repository.TagRepository;
import org.example.blog_spring.service.TagService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    public TagServiceImpl(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    public TagDto createTag(CreateTagRequest request) {
        if (tagRepository.existsByName(request.name())) {
            throw new IllegalArgumentException(
                    "Tag name '%s' is already in use".formatted(request.name()));
        }
        if (tagRepository.existsBySlug(request.slug())) {
            throw new IllegalArgumentException(
                    "Tag slug '%s' is already in use".formatted(request.slug()));
        }
        var tag = TagMapper.toEntity(request);
        var saved = tagRepository.save(tag);
        return TagMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TagDto getTag(Long id) {
        var tag = tagRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Tag with id %d not found".formatted(id)));
        return TagMapper.toDto(tag);
    }

    @Override
    @Transactional(readOnly = true)
    public TagDto getTagBySlug(String slug) {
        var tag = tagRepository.findBySlug(slug).orElseThrow(
                () -> new IllegalArgumentException("Tag with slug '%s' not found".formatted(slug)));
        return TagMapper.toDto(tag);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TagDto> getTags(Pageable pageable) {
        return tagRepository.findAll(pageable).map(TagMapper::toDto);
    }

    @Override
    public TagDto updateTag(Long id, UpdateTagRequest request) {
        var tag = tagRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Tag with id %d not found".formatted(id)));

        TagMapper.updateEntity(tag, request);
        var saved = tagRepository.save(tag);
        return TagMapper.toDto(saved);
    }

    @Override
    public void deleteTag(Long id) {
        if (!tagRepository.existsById(id)) {
            throw new IllegalArgumentException("Tag with id %d not found".formatted(id));
        }
        tagRepository.deleteById(id);
    }
}

