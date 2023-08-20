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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
        checkSendRequestNotToYourself(followerId, userId);
        User follower = validateExistsUser(followerId);
        User friend = validateExistsUser(userId);
        checkFriendshipExist(followerId, userId);
        Friendship request = Friendship.builder()
                .follower(follower)
                .friend(friend)
                .state(SUBSCRIBER)
                .chat(CLOSED)
                .build();
        return FriendshipMapper.toDto(friendshipRepository.save(request));
    }


    @Override
    public List<FriendshipDto> approveFriendship(long userId, List<Long> subscribersIds) {
        List<Friendship> subList = friendshipRepository.findAllByFollowerIdIn(subscribersIds);
        checkValidDataToApproveFriendship(userId, subList);
        User friend = validateExistsUser(userId);
        List<User> subscribers = userRepository.findUserByIdIn(subscribersIds);
        List<Friendship> approvedRequests = getApprovedRequests(friend, subscribers);
        List<Friendship> saved = setStatusAndSaveFriendships(approvedRequests, subList);
        return saved.stream()
                .map(FriendshipMapper::toDto)
                .collect(Collectors.toList());
    }


    @Override
    public List<FriendshipDto> rejectFriendship(long userId, List<Long> subscribersIds) {
        List<Friendship> subList = friendshipRepository.findAllByFollowerIdIn(subscribersIds);
        checkValidDataToRejectFriendship(userId, subList);
        subList.forEach(friendship -> friendship.setState(SUBSCRIBER));
        List<Friendship> saved = friendshipRepository.saveAll(subList);
        return saved.stream()
                .map(FriendshipMapper::toDto)
                .collect(Collectors.toList());
    }


    @Override
    public FriendshipDto requestChat(long fromUser, long toUser) {
        Friendship request = validateExistsFriendship(fromUser, toUser);
        checkUsersHasFriendship(request);
        request.setChat(REQUEST);
        return FriendshipMapper.toDto(friendshipRepository.save(request));
    }


    @Override
    public void deleteFriendshipRequest(long followerId, long subsId) {
        checkExistsFriendshipRequest(followerId, subsId);
        friendshipRepository.deleteByFollowerIdAndFriendId(followerId, subsId);
    }

    @Override
    public void deleteFriendship(long followerId, long subsId) {
        checkExistsFriendshipRequest(followerId, subsId);
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
        List<Post> posts = postRepository.getSubscribersEventsDate(
                userId,
                PageRequest.of(
                        from, size,
                        sort.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                        "created_date"
                )
        ).getContent();
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

    private List<Friendship> getApprovedRequests(User friend, List<User> subscribers) {
        return subscribers.stream()
                .map(subscriber -> {
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
                    return request;
                })
                .collect(Collectors.toList());
    }

    private static void checkValidDataToApproveFriendship(long userId, List<Friendship> subList) {
        if (subList.isEmpty()) {
            throw new InvalidDataException("Friendship requests were not found");
        }
        boolean checkOwnRequests = subList.stream()
                .allMatch(f -> f.getFriend().getId() == userId);
        if (!checkOwnRequests) {
            throw new InvalidDataException("You can only change your requests");
        }
        boolean checkCorrectStatusForUpdateFriendship = subList.stream()
                .noneMatch(f -> Objects.equals(SUBSCRIBER, f.getState()));
        if (checkCorrectStatusForUpdateFriendship) {
            throw new InvalidDataException("Status for friendship incorrect");
        }
    }

    private static void checkValidDataToRejectFriendship(long userId, List<Friendship> subList) {
        if (subList.isEmpty()) {
            throw new InvalidDataException("Friendship requests were not found");
        }
        boolean checkOwnRequests = subList.stream().allMatch(f -> f.getFriend().getId() == userId);
        if (!checkOwnRequests) {
            throw new InvalidDataException("You can only change your requests");
        }
        boolean checkCorrectStatusForUpdateFriendship = subList.stream()
                .noneMatch(f -> Objects.equals(SUBSCRIBER, f.getState()));
        if (checkCorrectStatusForUpdateFriendship) {
            throw new InvalidDataException("Status for reject friendship incorrect");
        }
    }

    private void checkExistsFriendshipRequest(long followerId, long subsId) {
        boolean checkExistsFriendshipRequest = friendshipRepository
                .existsByFollowerIdAndFriendId(followerId, subsId);
        if (!checkExistsFriendshipRequest) {
            throw new UserNotFoundException("Friendship request no exist.");
        }
    }

    private static void checkUsersHasFriendship(Friendship request) {
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

    private static void checkSendRequestNotToYourself(long followerId, long userId) {
        if (followerId == userId) {
            throw new InvalidDataException("You can't follow yourself.");
        }
    }

    private void checkFriendshipExist(long userId, long friendId) {
        if (friendshipRepository.existsByFollowerIdAndFriendIdAndStateNot(userId, friendId, CANCELED)) {
            throw new InvalidDataException("Already friends.");
        }
    }
}
