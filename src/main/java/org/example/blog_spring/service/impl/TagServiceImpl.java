package org.example.blog_spring.service.impl;

import org.example.blog_spring.dto.CreateTagRequest;
import org.example.blog_spring.dto.TagDto;
import org.example.blog_spring.dto.UpdateTagRequest;
import org.example.blog_spring.exception.TagNotFoundException;
import org.example.blog_spring.mapper.TagMapper;
import org.example.blog_spring.repository.TagRepository;
import org.example.blog_spring.service.TagService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
    @Caching(evict = {
            @CacheEvict(cacheNames = "tags", allEntries = true),
            @CacheEvict(cacheNames = "tagsBySlug", allEntries = true),
            @CacheEvict(cacheNames = "postLists", allEntries = true)
    })
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
    @Cacheable(cacheNames = "tags", key = "#id")
    public TagDto getTag(Long id) {
        var tag = tagRepository.findById(id).orElseThrow(() -> new TagNotFoundException(id));
        return TagMapper.toDto(tag);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "tagsBySlug", key = "#slug")
    public TagDto getTagBySlug(String slug) {
        var tag = tagRepository.findBySlug(slug).orElseThrow(() -> new TagNotFoundException(slug));
        return TagMapper.toDto(tag);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "tagLists",
            key = "T(java.util.Objects).hash(#pageable.pageNumber, #pageable.pageSize)")
    public Page<TagDto> getTags(Pageable pageable) {
        return tagRepository.findAll(pageable).map(TagMapper::toDto);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "tags", key = "#id"),
            @CacheEvict(cacheNames = "tagsBySlug", allEntries = true),
            @CacheEvict(cacheNames = "tagLists", allEntries = true),
            @CacheEvict(cacheNames = "postLists", allEntries = true)
    })
    public TagDto updateTag(Long id, UpdateTagRequest request) {
        var tag = tagRepository.findById(id).orElseThrow(() -> new TagNotFoundException(id));

        TagMapper.updateEntity(tag, request);
        var saved = tagRepository.save(tag);
        return TagMapper.toDto(saved);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "tags", key = "#id"),
            @CacheEvict(cacheNames = "tagsBySlug", allEntries = true),
            @CacheEvict(cacheNames = "tagLists", allEntries = true),
            @CacheEvict(cacheNames = "postLists", allEntries = true)
    })
    public void deleteTag(Long id) {
        if (!tagRepository.existsById(id)) {
            throw new TagNotFoundException(id);
        }
        tagRepository.deleteById(id);
    }
}
