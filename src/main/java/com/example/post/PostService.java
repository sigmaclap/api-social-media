package com.example.post;

import com.example.post.dto.PostDto;

import java.util.List;

public interface PostService {
    List<PostDto> getAllPosts(Long userId, Integer from, Integer size);

    PostDto createPost(Long userid, PostDto dto);

    PostDto updatePost(Long userId, PostDto dto, Long postId);

    void deletePost(Long userId, Long postId);
}
