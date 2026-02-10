package org.example.blog_spring.service.impl;

import org.example.blog_spring.cache.CacheManager;
import org.example.blog_spring.dao.TagDao;
import org.example.blog_spring.dto.CreateTagRequest;
import org.example.blog_spring.dto.TagDto;
import org.example.blog_spring.dto.UpdateTagRequest;
import org.example.blog_spring.exception.TagNotFoundException;
import org.example.blog_spring.mapper.TagMapper;
import org.example.blog_spring.service.TagService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TagServiceImpl implements TagService {

    private final TagDao tagDao;
    private final CacheManager cacheManager;

    public TagServiceImpl(TagDao tagDao, CacheManager cacheManager) {
        this.tagDao = tagDao;
        this.cacheManager = cacheManager;
    }

    @Override
    public TagDto createTag(CreateTagRequest request) {
        if (tagDao.existsByName(request.name())) {
            throw new IllegalArgumentException(
                    "Tag name '%s' is already in use".formatted(request.name()));
        }
        if (tagDao.existsBySlug(request.slug())) {
            throw new IllegalArgumentException(
                    "Tag slug '%s' is already in use".formatted(request.slug()));
        }
        var tag = TagMapper.toEntity(request);
        var saved = tagDao.insert(tag);
        cacheManager.putTag(saved);
        cacheManager.invalidatePostListCache();
        return TagMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TagDto getTag(Long id) {
        var tag = cacheManager.getTag(id);
        if (tag == null) {
            tag = tagDao.findById(id).orElseThrow(() -> new TagNotFoundException(id));
            cacheManager.putTag(tag);
        }
        return TagMapper.toDto(tag);
    }

    @Override
    @Transactional(readOnly = true)
    public TagDto getTagBySlug(String slug) {
        var tag = cacheManager.getTagBySlug(slug);
        if (tag == null) {
            tag = tagDao.findBySlug(slug).orElseThrow(() -> new TagNotFoundException(slug));
            cacheManager.putTag(tag);
        }
        return TagMapper.toDto(tag);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TagDto> getTags(Pageable pageable) {
        return tagDao.findAll(pageable).map(TagMapper::toDto);
    }

    @Override
    public TagDto updateTag(Long id, UpdateTagRequest request) {
        var tag = tagDao.findById(id).orElseThrow(() -> new TagNotFoundException(id));

        TagMapper.updateEntity(tag, request);
        tagDao.update(tag);
        cacheManager.putTag(tag);
        cacheManager.invalidatePostListCache();
        return TagMapper.toDto(tag);
    }

    @Override
    public void deleteTag(Long id) {
        if (!tagDao.existsById(id)) {
            throw new TagNotFoundException(id);
        }
        tagDao.deleteById(id);
        cacheManager.removeTag(id);
    }
}
