package com.example.post;

import com.example.exceptions.InvalidDataException;
import com.example.exceptions.PostNotFoundException;
import com.example.exceptions.UserNotFoundException;
import com.example.post.dto.PostDto;
import com.example.post.entity.Post;
import com.example.post.mapper.PostMapper;
import com.example.user.UserRepository;
import com.example.user.entity.User;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceImplUnitTest {
    @InjectMocks
    PostServiceImpl postService;
    @Mock
    PostRepository postRepository;
    @Mock
    UserRepository userRepository;
    private final EasyRandom generator = new EasyRandom();

    @Test
    void testGetAllPostsNotEmpty() {
        Long userId = 1L;
        int from = 0;
        int size = 10;
        List<Post> posts = new ArrayList<>();
        posts.add(Post.builder().id(1L).description("Post 1").build());
        posts.add(Post.builder().id(1L).description("Post 2").build());

        when(postRepository
                .findAllByOwner_idOrderByIdAsc(userId, PageRequest.of(from, size)))
                .thenReturn(new PageImpl<>(posts));

        List<PostDto> result = postService.getAllPosts(userId, from, size);

        assertEquals(posts.size(), result.size());
        assertEquals("Post 1", result.get(0).getDescription());
        assertEquals("Post 2", result.get(1).getDescription());
    }

    @Test
    void testGetAllPostsEmpty() {
        Long userId = 1L;
        int from = 0;
        int size = 10;

        when(postRepository
                .findAllByOwner_idOrderByIdAsc(userId, PageRequest.of(from, size)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        List<PostDto> result = postService.getAllPosts(userId, from, size);

        assertTrue(result.isEmpty());
    }

    @Test
    void testCreatePost_whenValidData_thenCreatePostSuccess() {
        PostDto dto = generator.nextObject(PostDto.class);
        Post postToSave = PostMapper.toPost(dto);
        User user = generator.nextObject(User.class);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(postRepository.save(postToSave)).thenReturn(postToSave);
        PostDto expectedPost = PostMapper.toDtoPost(postToSave);

        PostDto actualPost = postService.createPost(user.getId(), dto);

        assertEquals(expectedPost.getDescription(), actualPost.getDescription());
        verify(postRepository, times(1)).save(postToSave);
    }

    @Test
    void testUpdatePost_whenValidData_thenReturnedUpdatePost() {
        User user = generator.nextObject(User.class);
        PostDto dto = generator.nextObject(PostDto.class);
        Post post = generator.nextObject(Post.class);
        post.setId(1L);
        post.setOwner(user);
        post.setDescription(dto.getDescription());
        post.setImage(dto.getImage());
        post.setText(dto.getText());
        Post postToUpdate = PostMapper.toPost(dto);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(postRepository.save(post)).thenReturn(post);
        PostDto expectedPost = PostMapper.toDtoPost(postToUpdate);

        PostDto actualPost = postService.updatePost(user.getId(), dto, post.getId());

        assertEquals(expectedPost.getDescription(), actualPost.getDescription());
        assertEquals(expectedPost.getText(), actualPost.getText());
        assertEquals(expectedPost.getImage(), actualPost.getImage());
        verify(postRepository, times(1)).save(post);
    }

    @Test
    void testUpdatePost_whenUserNotOwner_thenReturnedThrows() {
        User user = generator.nextObject(User.class);
        PostDto dto = generator.nextObject(PostDto.class);
        Post post = generator.nextObject(Post.class);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        Throwable exception = assertThrows(InvalidDataException.class,
                () -> postService.updatePost(user.getId(), dto, post.getId()));

        assertEquals("Only the user who created the post can be deleted it", exception.getMessage());
    }

    @Test
    void testUpdatePost_whenUserNotFound_thenReturnedThrows() {
        User user = generator.nextObject(User.class);
        PostDto dto = generator.nextObject(PostDto.class);
        Post post = generator.nextObject(Post.class);

        Throwable exception = assertThrows(UserNotFoundException.class,
                () -> postService.updatePost(user.getId(), dto, post.getId()));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testUpdatePost_whenPostNotFound_thenReturnedThrows() {
        User user = generator.nextObject(User.class);
        PostDto dto = generator.nextObject(PostDto.class);
        Post post = generator.nextObject(Post.class);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        Throwable exception = assertThrows(PostNotFoundException.class,
                () -> postService.updatePost(user.getId(), dto, post.getId()));

        assertEquals("Post not found", exception.getMessage());
    }

    @Test
    void testDeletePost() {
        User user = new User();
        user.setId(1L);
        Post post = new Post();
        post.setId(1L);
        post.setOwner(user);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        postService.deletePost(1L, 1L);

        verify(postRepository, times(1)).delete(post);
    }

    @Test
    void testDeletePost_UserNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            postService.deletePost(1L, 1L);
        });

        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    void testDeletePost_PostNotFoundException() {
        User user = new User();
        user.setId(1L);

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(PostNotFoundException.class, () -> {
            postService.deletePost(1L, 1L);
        });

        Mockito.verify(postRepository, Mockito.never()).delete(Mockito.any(Post.class));
    }

    @Test
    void testDeletePost_InvalidUserException() {
        User user = new User();
        user.setId(1L);
        Post post = new Post();
        post.setId(1L);
        post.setOwner(new User());
        post.getOwner().setId(2L);

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        assertThrows(InvalidDataException.class, () -> {
            postService.deletePost(1L, 1L);
        });

        Mockito.verify(postRepository, Mockito.never()).delete(Mockito.any(Post.class));
    }
}