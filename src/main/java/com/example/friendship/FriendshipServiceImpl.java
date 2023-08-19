package com.example.friendship;

import com.example.exceptions.FriendshipNotFoundException;
import com.example.exceptions.InvalidDataException;
import com.example.exceptions.UserNotFoundException;
import com.example.friendship.dto.FriendshipDto;
import com.example.friendship.entity.Friendship;
import com.example.friendship.mapper.FriendshipMapper;
import com.example.post.PostRepository;
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

import java.util.*;
import java.util.stream.Collectors;

import static com.example.friendship.states.ChatState.CLOSED;
import static com.example.friendship.states.ChatState.REQUEST;
import static com.example.friendship.states.FriendshipState.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FriendshipServiceImpl implements FriendshipService {
    private final PostRepository postRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    @Override
    public FriendshipDto requestFriendship(long followerId, long userId) {
        isCheckSendRequestNotToYourself(followerId, userId);
        User follower = validateExistsUser(followerId);
        User friend = validateExistsUser(userId);
        isCheckFriendshipExist(followerId, userId);
        Friendship request = Friendship.builder()
                .follower(follower)
                .friend(friend)
                .state(SUBSCRIBER)
                .chat(CLOSED)
                .build();
        return FriendshipMapper.toDto(friendshipRepository.save(request));
    }


    @Override
    public List<FriendshipDto> approveFriendship(long userId, List<Long> ids) {
        List<Friendship> subList = friendshipRepository.findAllByFollowerIdIn(ids);
        isCheckValidDataToApproveFriendship(userId, subList);
        User friend = userRepository.getReferenceById(userId);
        List<User> subscribers = userRepository.findUserByIdIn(ids);
        List<Friendship> approvedRequests = new ArrayList<>();
        for (User subscriber : subscribers) {
            Optional<Friendship> existsFriendship = friendshipRepository
                    .findByFollowerIdAndFriendId(friend.getId(), subscriber.getId());
            Friendship request;
            if (existsFriendship.isPresent()) {
                request = existsFriendship.get();
                request.setState(FRIENDSHIP);
            } else {
                request = Friendship.builder()
                        .follower(friend)
                        .friend(subscriber)
                        .state(FRIENDSHIP)
                        .chat(CLOSED)
                        .build();
            }
            approvedRequests.add(request);

        }
        List<Friendship> saved = setStatusAndSaveFriendships(approvedRequests, subList);
        return saved.stream()
                .map(FriendshipMapper::toDto)
                .collect(Collectors.toList());
    }


    @Override
    public List<FriendshipDto> rejectFriendship(long userId, List<Long> ids) {
        List<Friendship> subList = friendshipRepository.findAllByFollowerIdIn(ids);
        isCheckValidDataToRejectFriendship(userId, subList);
        subList.forEach(friendship -> friendship.setState(SUBSCRIBER));
        List<Friendship> saved = friendshipRepository.saveAll(subList);
        return saved.stream()
                .map(FriendshipMapper::toDto)
                .collect(Collectors.toList());
    }


    @Override
    public FriendshipDto requestChat(long fromUser, long toUser) {
        Friendship request = validateExistsFriendship(fromUser, toUser);
        isCheckUsersHasFriendship(request);
        request.setChat(REQUEST);
        return FriendshipMapper.toDto(friendshipRepository.save(request));
    }


    @Override
    public void deleteFriendshipRequest(long followerId, long subsId) {
        isCheckExistsFriendshipRequest(followerId, subsId);
        friendshipRepository.deleteByFollowerIdAndFriendId(followerId, subsId);
    }

    @Override
    public void deleteFriendship(long followerId, long subsId) {
        isCheckExistsFriendshipRequest(followerId, subsId);
        friendshipRepository.deleteByFollowerIdAndFriendId(followerId, subsId);
        Friendship friendship = validateExistsFriendship(subsId, followerId);
        User friend = validateExistsUser(followerId);
        User subscriber = validateExistsUser(subsId);
        friendship.setFollower(subscriber);
        friendship.setFriend(friend);
        friendship.setState(SUBSCRIBER);
        friendshipRepository.save(friendship);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostDto> getSubscriptionEvents(Long userId, int from, int size, String sort) {
        List<Post> posts;
        if (sort.equalsIgnoreCase("asc")) {
            posts = postRepository.getSubscribersEventsDateAsc(userId, PageRequest.of(from, size)).getContent();
        } else {
            posts = postRepository.getSubscribersEventsDateDesc(userId, PageRequest.of(from, size)).getContent();
        }
        if (posts.isEmpty()) {
            return Collections.emptyList();
        }
        return posts.stream()
                .map(PostMapper::toDtoPost)
                .collect(Collectors.toList());
    }

    private List<Friendship> setStatusAndSaveFriendships(List<Friendship> approvedRequests, List<Friendship> subList) {
        friendshipRepository.saveAll(approvedRequests);
        subList.forEach(friendship -> friendship.setState(FRIENDSHIP));
        List<Friendship> saved = friendshipRepository.saveAll(subList);
        subList.addAll(approvedRequests);
        return saved;
    }

    private static void isCheckValidDataToApproveFriendship(long userId, List<Friendship> subList) {
        if (subList.isEmpty()) {
            throw new InvalidDataException("Friendship requests were not found");
        }
        boolean matchUser = subList.stream().allMatch(f -> f.getFriend().getId() == userId);
        if (!matchUser) {
            throw new InvalidDataException("You can only change your requests");
        }
        boolean checkStatus = subList.stream().noneMatch(f -> Set.of(SUBSCRIBER).contains(f.getState()));
        if (checkStatus) {
            throw new InvalidDataException("Status for friendship incorrect");
        }
    }

    private static void isCheckValidDataToRejectFriendship(long userId, List<Friendship> subList) {
        if (subList.isEmpty()) {
            throw new InvalidDataException("Friendship requests were not found");
        }
        boolean matchUser = subList.stream().allMatch(f -> f.getFriend().getId() == userId);
        if (!matchUser) {
            throw new InvalidDataException("You can only change your requests");
        }
        boolean checkStatus = subList.stream().noneMatch(f -> Set.of(SUBSCRIBER, FRIENDSHIP).contains(f.getState()));
        if (checkStatus) {
            throw new InvalidDataException("Status for friendship incorrect");
        }
    }

    private void isCheckExistsFriendshipRequest(long followerId, long subsId) {
        boolean checkExistsFriendshipRequest = friendshipRepository
                .existsByFollowerIdAndFriendId(followerId, subsId);
        if (!checkExistsFriendshipRequest) {
            throw new UserNotFoundException("Friendship request no exist.");
        }
    }

    private static void isCheckUsersHasFriendship(Friendship request) {
        if (!request.getState().equals(FRIENDSHIP)) {
            throw new InvalidDataException("Only friends can opened chat each other");
        }
    }

    private Friendship validateExistsFriendship(long followerId, long subsId) {
        return friendshipRepository.findByFollowerIdAndFriendId(followerId, subsId)
                .orElseThrow(() -> new FriendshipNotFoundException("Friendship not found"));
    }

    private User validateExistsUser(long userId) {
        return userRepository.findById(userId)

                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private static void isCheckSendRequestNotToYourself(long followerId, long userId) {
        if (followerId == userId) {
            throw new InvalidDataException("You can't follow yourself.");
        }
    }

    private void isCheckFriendshipExist(long userId, long friendId) {
        if (friendshipRepository.existsByFollowerIdAndFriendIdAndStateNot(userId, friendId, CANCELED)) {
            throw new InvalidDataException("Already friends.");
        }
    }
}
