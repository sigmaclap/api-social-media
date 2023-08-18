package com.example.post;

import com.example.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findAllByOwner_idOrderByIdAsc(Long ownerId, Pageable pageable);

    @Query(value = "SELECT p.id , p.description , p.\"text\" , p.image , p.owner_id , p.created_date\n" +
            "FROM posts p\n" +
            "JOIN friendship f ON p.owner_id = f.friend_id\n" +
            "WHERE f.follower_id = ?1 AND (f.state = 'SUBSCRIBER' OR f.state = 'FRIENDSHIP')\n" +
            "ORDER BY p.created_date DESC", nativeQuery = true)
    Page<Post> getSubscribersEventsDateDesc(Long userId, Pageable pageable);

    @Query(value = "SELECT p.id , p.description , p.\"text\" , p.image , p.owner_id , p.created_date\n" +
            "FROM posts p\n" +
            "JOIN friendship f ON p.owner_id = f.friend_id\n" +
            "WHERE f.follower_id = ?1 AND (f.state = 'SUBSCRIBER' OR f.state = 'FRIENDSHIP')\n" +
            "ORDER BY p.created_date ASC", nativeQuery = true)
    Page<Post> getSubscribersEventsDateAsc(Long userId, Pageable pageable);
}