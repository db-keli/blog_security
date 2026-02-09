package org.example.blog_spring.service;

import org.example.blog_spring.dto.CreateTagRequest;
import org.example.blog_spring.dto.TagDto;
import org.example.blog_spring.dto.UpdateTagRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TagService {

    TagDto createTag(CreateTagRequest request);

    TagDto getTag(Long id);

    TagDto getTagBySlug(String slug);

    Page<TagDto> getTags(Pageable pageable);

    TagDto updateTag(Long id, UpdateTagRequest request);

    void deleteTag(Long id);
}

