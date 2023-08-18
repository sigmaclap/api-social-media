package com.example.post.mapper;

import com.example.post.dto.PostDto;
import com.example.post.entity.Post;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PostMapper {
    public Post toPost(PostDto postDto) {
        return Post.builder()
                .id(postDto.getId())
                .description(postDto.getDescription())
                .text(postDto.getText())
                .image(postDto.getImage())
                .created(postDto.getCreated())
                .build();
    }

    public PostDto toDtoPost(Post post) {
        return PostDto.builder()
                .id(post.getId())
                .description(post.getDescription())
                .text(post.getText())
                .image(post.getImage())
                .created(post.getCreated())
                .build();
    }
}
