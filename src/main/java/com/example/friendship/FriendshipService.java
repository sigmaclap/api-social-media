package com.example.friendship;

import com.example.friendship.dto.FriendshipDto;
import com.example.post.dto.PostDto;

import java.util.List;

public interface FriendshipService {
    FriendshipDto requestFriendship(long followerId, long userId);

    List<FriendshipDto> approveFriendship(long userId, List<Long> ids);

    List<FriendshipDto> rejectFriendship(long userId, List<Long> ids);

    FriendshipDto requestChat(long fromUser, long toUser);

    void deleteFriendshipRequest(long followerId, long subsId);

    void deleteFriendship(long followerId, long subsId);

    List<PostDto> getSubscriptionEvents(Long userId, int from, int size, String sort);
}
