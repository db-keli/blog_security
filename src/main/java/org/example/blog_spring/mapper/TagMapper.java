package org.example.blog_spring.mapper;

import org.example.blog_spring.domain.Tag;
import org.example.blog_spring.dto.CreateTagRequest;
import org.example.blog_spring.dto.TagDto;
import org.example.blog_spring.dto.UpdateTagRequest;

public final class TagMapper {

    private TagMapper() {}

    public static Tag toEntity(CreateTagRequest request) {
        var tag = new Tag(request.name(), request.slug());
        tag.setDescription(request.description());
        return tag;
    }

    public static void updateEntity(Tag tag, UpdateTagRequest request) {
        tag.setName(request.name());
        tag.setSlug(request.slug());
        tag.setDescription(request.description());
    }

    public static TagDto toDto(Tag tag) {
        return new TagDto(tag.getId(), tag.getName(), tag.getSlug(), tag.getDescription(),
                tag.getCreatedAt());
    }
}

