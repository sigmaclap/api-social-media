package com.example.post;

import com.example.exceptions.InvalidDataException;
import com.example.exceptions.PostNotFoundException;
import com.example.exceptions.UserNotFoundException;
import com.example.post.dto.PostDto;
import com.example.post.entity.Post;
import com.example.post.mapper.PostMapper;
import com.example.user.UserRepository;
import com.example.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    public List<PostDto> getAllPosts(Long userId, Integer from, Integer size) {
        List<Post> posts = postRepository
                .findAllByOwner_idOrderByIdAsc(userId, PageRequest.of(from, size)).getContent();
        if (posts.isEmpty()) return Collections.emptyList();
        return posts.stream()
                .map(PostMapper::toDtoPost)
                .collect(Collectors.toList());
    }

    @Override
    public PostDto createPost(Long userid, PostDto dto) {
        Post post = PostMapper.toPost(dto);
        post.setOwner(validateExistsUser(userid));
        return PostMapper.toDtoPost(postRepository.save(post));
    }

    @Override
    public PostDto updatePost(Long userId, PostDto dto, Long postId) {
        Post postToUpdate = PostMapper.toPost(dto);
        User user = validateExistsUser(userId);
        Post post = validateExistsPost(postId);
        validateUserOwnerPost(post, user);
        setPostDetails(postToUpdate, post);
        return PostMapper.toDtoPost(postRepository.save(post));
    }


    @Override
    public void deletePost(Long userId, Long postId) {
        User user = validateExistsUser(userId);
        Post post = validateExistsPost(postId);
        validateUserOwnerPost(post, user);
        postRepository.delete(post);
    }

    private static void setPostDetails(Post postToUpdate, Post post) {
        if (postToUpdate.getText() != null) post.setText(postToUpdate.getText());
        if (postToUpdate.getDescription() != null) post.setDescription(post.getDescription());
        if (postToUpdate.getImage() != null) post.setImage(postToUpdate.getImage());
    }

    private Post validateExistsPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found"));
    }

    private User validateExistsUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private static void validateUserOwnerPost(Post post, User user) {
        if (!post.getOwner().equals(user)) {
            throw new InvalidDataException("Only the user who created the post can be deleted it");
        }
    }
}
