package com.example.friendship;

import com.example.friendship.entity.Friendship;
import com.example.friendship.states.FriendshipState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    void deleteByFollowerIdAndFriendId(long followerId, long friendId);

    boolean existsByFollowerIdAndFriendIdAndStateNot(long followerId, long friendId, FriendshipState status);

    boolean existsByFollowerIdAndFriendId(long followerId, long friendId);

    List<Friendship> findAllByFollowerIdIn(List<Long> ids);

    Optional<Friendship> findByFollowerIdAndFriendId(long followerId, long friendId);
}