package org.example.blog_spring.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.blog_spring.dto.ApiResponse;
import org.example.blog_spring.dto.CreateTagRequest;
import org.example.blog_spring.dto.TagDto;
import org.example.blog_spring.dto.UpdateTagRequest;
import org.example.blog_spring.service.TagService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tags")
@Tag(name = "Tags", description = "Tag management APIs")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping
    @Operation(summary = "Create a new tag")
    public ResponseEntity<ApiResponse<TagDto>> createTag(
            @Valid @RequestBody CreateTagRequest request
    ) {
        var tagDto = tagService.createTag(request);
        var response = ApiResponse.success(HttpStatus.CREATED, "Tag created successfully", tagDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single tag by id")
    public ResponseEntity<ApiResponse<TagDto>> getTag(@PathVariable Long id) {
        var tagDto = tagService.getTag(id);
        var response = ApiResponse.success(HttpStatus.OK, "Tag retrieved successfully", tagDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get a single tag by slug")
    public ResponseEntity<ApiResponse<TagDto>> getTagBySlug(@PathVariable String slug) {
        var tagDto = tagService.getTagBySlug(slug);
        var response =
                ApiResponse.success(HttpStatus.OK, "Tag retrieved successfully", tagDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List tags with pagination")
    public ResponseEntity<ApiResponse<Page<TagDto>>> getTags(Pageable pageable) {
        var tags = tagService.getTags(pageable);
        var response = ApiResponse.success(HttpStatus.OK, "Tags retrieved successfully", tags);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing tag")
    public ResponseEntity<ApiResponse<TagDto>> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTagRequest request
    ) {
        var updated = tagService.updateTag(id, request);
        var response = ApiResponse.success(HttpStatus.OK, "Tag updated successfully", updated);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a tag")
    public ResponseEntity<ApiResponse<Void>> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        var response =
                ApiResponse.<Void>success(HttpStatus.NO_CONTENT, "Tag deleted successfully", null);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }
}

