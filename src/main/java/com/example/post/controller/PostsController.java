package com.example.post.controller;

import com.example.post.PostService;
import com.example.post.dto.PostDto;
import com.example.utills.UserHolder;
import com.example.utills.validated.Marker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/secured/posts")
@RequiredArgsConstructor
@Validated
@Tag(name = "Управление постами")
public class PostsController {

    private final PostService postService;
    private final UserHolder userHolder;

    @GetMapping("/{userId}")
    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Получение всех постов конкретного пользователя"
    )
    public List<PostDto>
    getAllPosts(@PathVariable Long userId,
                @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                @RequestParam(defaultValue = "20") @Positive Integer size) {
        return postService.getAllPosts(userId, from, size);
    }

    @PostMapping
    @Validated(Marker.OnCreate.class)
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Создание поста пользователем"
    )
    public PostDto createPost(@Valid @RequestBody PostDto dto) {
        return postService.createPost(userHolder.getUserId(), dto);
    }

    @PatchMapping("/{postId}")
    @Validated(Marker.OnUpdate.class)
    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Обновление поста пользователем"
    )
    public PostDto updatePost(@Valid @RequestBody PostDto dto,
                              @PathVariable Long postId) {
        return postService.updatePost(userHolder.getUserId(), dto, postId);
    }

    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Удаление поста пользователем"
    )
    public void deletePost(@PathVariable Long postId) {
        postService.deletePost(userHolder.getUserId(), postId);
    }
}
