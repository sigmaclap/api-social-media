package com.example.friendship;

import com.example.exceptions.FriendshipNotFoundException;
import com.example.exceptions.InvalidDataException;
import com.example.exceptions.UserNotFoundException;
import com.example.friendship.dto.FriendshipDto;
import com.example.friendship.entity.Friendship;
import com.example.friendship.mapper.FriendshipMapper;
import com.example.friendship.states.FriendshipState;
import com.example.post.PostRepository;
import com.example.post.dto.PostDto;
import com.example.post.entity.Post;
import com.example.user.UserRepository;
import com.example.user.entity.User;
import com.example.user.mapper.UserMapper;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.*;

import static com.example.friendship.states.ChatState.CLOSED;
import static com.example.friendship.states.ChatState.REQUEST;
import static com.example.friendship.states.FriendshipState.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendshipServiceImplTest {
    @InjectMocks
    FriendshipServiceImpl friendshipService;
    @Mock
    UserRepository userRepository;
    @Mock
    PostRepository postRepository;
    @Mock
    FriendshipRepository friendshipRepository;
    private final EasyRandom generator = new EasyRandom();

    @Test
    void testRequestFriendship_ThrowsInvalidDataException() {
        long followerId = 1L;
        long userId = 1L;

        InvalidDataException expectedException = new InvalidDataException("You can't follow yourself.");

        Exception exception = assertThrows(InvalidDataException.class, () -> {
            friendshipService.requestFriendship(followerId, userId);
        });

        assertEquals(expectedException.getMessage(), exception.getMessage());
    }

    @Test
    void testRequestFriendship_ThrowsUserNotFoundExceptionForFollower() {
        long followerId = 1L;
        long userId = 2L;

        UserNotFoundException expectedException = new UserNotFoundException("User not found");

        when(userRepository.findById(any())).thenReturn(Optional.empty());

        Exception exception = assertThrows(UserNotFoundException.class, () ->
                friendshipService.requestFriendship(followerId, userId));

        assertEquals(expectedException.getMessage(), exception.getMessage());
    }

    @Test
    void testRequestFriendship_ThrowsUserNotFoundExceptionForFriend() {
        long followerId = 1L;
        long userId = 2L;

        UserNotFoundException expectedException = new UserNotFoundException("User not found");

        when(userRepository.findById(followerId)).thenReturn(Optional.of(new User()));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(UserNotFoundException.class, () -> {
            friendshipService.requestFriendship(followerId, userId);
        });

        assertEquals(expectedException.getMessage(), exception.getMessage());
    }

    @Test
    void testRequestFriendship_ThrowsInvalidDataExceptionForExistingFriendship() {
        long followerId = 1L;
        long userId = 2L;

        InvalidDataException expectedException = new InvalidDataException("Already friends.");

        when(userRepository.findById(any())).thenReturn(Optional.of(new User()));
        when(userRepository.findById(any())).thenReturn(Optional.of(new User()));
        when(friendshipRepository
                .existsByFollowerIdAndFriendIdAndStateNot(followerId, userId, FriendshipState.CANCELED))
                .thenReturn(true);

        Exception exception = assertThrows(InvalidDataException.class, () -> {
            friendshipService.requestFriendship(followerId, userId);
        });

        assertEquals(expectedException.getMessage(), exception.getMessage());
    }

    @Test
    void testRequestFriendship_Success() {
        long followerId = 1L;
        long userId = 2L;

        User follower = new User();
        follower.setId(followerId);
        User friend = new User();
        friend.setId(userId);
        FriendshipDto expectedFriendshipDto = new FriendshipDto();
        expectedFriendshipDto.setFriend(UserMapper.toDtoUser(friend));
        expectedFriendshipDto.setFollowerId(followerId);
        Friendship expectedFriendship = FriendshipMapper.toEntity(expectedFriendshipDto);
        expectedFriendship.setFollower(follower);
        when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
        when(userRepository.findById(userId)).thenReturn(Optional.of(friend));
        when(friendshipRepository
                .existsByFollowerIdAndFriendIdAndStateNot(followerId, userId, FriendshipState.CANCELED))
                .thenReturn(false);
        when(friendshipRepository.save(any(Friendship.class))).thenReturn(expectedFriendship);
        FriendshipDto expected = FriendshipMapper.toDto(expectedFriendship);

        FriendshipDto friendshipDto = friendshipService.requestFriendship(followerId, userId);

        assertNotNull(friendshipDto);
        assertEquals(expected, friendshipDto);
    }

    @Test
    void approveFriendship_NoValidStatus_ThrowInvalidDataException() {
        long userId = 1L;
        List<Long> ids = List.of(2L, 3L);

        User friend = new User();
        friend.setId(userId);

        User subscriber1 = new User();
        subscriber1.setId(2L);

        User subscriber2 = new User();
        subscriber2.setId(3L);

        Friendship friendship1 = new Friendship();
        friendship1.setFollower(friend);
        friendship1.setFriend(subscriber1);
        friendship1.setState(CANCELED);
        friendship1.setChat(CLOSED);

        Friendship friendship2 = new Friendship();
        friendship2.setFollower(friend);
        friendship2.setFriend(subscriber2);
        friendship2.setState(FRIENDSHIP);
        friendship2.setChat(CLOSED);


        List<Friendship> friendships = List.of(friendship1, friendship2);

        when(friendshipRepository.findAllByFollowerIdIn(ids)).thenReturn(friendships);

        assertThrows(InvalidDataException.class, () -> {
            friendshipService.approveFriendship(userId, ids);
        });
    }


    @Test
    void testApproveFriendship() {
        long userId = 1;
        List<Long> ids = Arrays.asList(1L, 2L);
        User follower = new User();
        follower.setId(userId);
        User follower1 = new User();
        follower1.setId(2L);
        User friend1 = new User();
        friend1.setId(3L);
        User follower2 = new User();
        follower2.setId(4L);

        Friendship friendship1 = new Friendship();
        friendship1.setId(2L);
        friendship1.setFollower(follower);
        friendship1.setFriend(friend1);
        friendship1.setState(SUBSCRIBER);
        Friendship friendship2 = new Friendship();
        friendship2.setId(3L);
        friendship2.setFollower(follower2);
        friendship2.setFriend(friend1);
        friendship2.setState(SUBSCRIBER); // Создаем mock объект Friendship
        List<Friendship> subList = new ArrayList<>(); // Создаем mock объект List<Friendship>
        subList.add(friendship1);
        subList.add(friendship2);
        List<User> subscribers = new ArrayList<>();
        subscribers.add(follower);
        subscribers.add(follower1);
        when(friendshipRepository.findAllByFollowerIdIn(ids)).thenReturn(subList);
        when(userRepository.getReferenceById(friend1.getId())).thenReturn(friend1);
        when(userRepository.findUserByIdIn(ids)).thenReturn(subscribers);
        when(friendshipRepository.findByFollowerIdAndFriendId(friend1.getId(), follower.getId()))
                .thenReturn(Optional.of(friendship1));
        when(friendshipRepository.findByFollowerIdAndFriendId(friend1.getId(), follower.getId()))
                .thenReturn(Optional.empty());


        List<FriendshipDto> expected = new ArrayList<>();

        List<FriendshipDto> result = friendshipService.approveFriendship(friend1.getId(), ids);

        assertEquals(expected, result);

        verify(friendshipRepository, times(1)).saveAll(subList);
    }

    @Test
    void testApproveFriendship_WhenSubListEmpty_thenShouldReturnedThrow() {
        List<Long> ids = List.of(1L, 2L);

        when(friendshipRepository.findAllByFollowerIdIn(ids)).thenReturn(Collections.emptyList());

        assertThrows(InvalidDataException.class,
                () -> friendshipService.approveFriendship(3L, ids));
    }

    @Test
    void testApproveFriendship_WithValidData_ShouldReturnThrowInvalidDataException() {
        long userId = 1;
        List<Long> ids = Arrays.asList(1L, 2L);
        User follower = new User();
        follower.setId(userId);
        User follower1 = new User();
        follower.setId(2L);
        User friend1 = new User();
        friend1.setId(3L);

        Friendship friendship1 = new Friendship();
        friendship1.setId(2L);
        friendship1.setFollower(follower);
        friendship1.setFriend(friend1);
        friendship1.setState(CANCELED);
        Friendship friendship2 = new Friendship();
        friendship2.setId(3L);
        friendship2.setFollower(follower1);
        friendship2.setFriend(friend1);
        friendship2.setState(CANCELED);
        List<Friendship> sublist = Arrays.asList(friendship1, friendship2);
        InvalidDataException expected = new InvalidDataException("Status for friendship incorrect");

        when(friendshipRepository.findAllByFollowerIdIn(ids))
                .thenReturn(Arrays.asList(friendship1, friendship2));

        Exception exception = assertThrows(InvalidDataException.class,
                () -> friendshipService.approveFriendship(3L, ids));

        assertEquals(expected.getMessage(), exception.getMessage());
        verify(friendshipRepository, never()).saveAll(sublist);
    }


    @Test
    void testRejectFriendship_WithValidData_ShouldReturnUpdatedFriendshipList() {
        long userId = 1;
        List<Long> ids = Arrays.asList(1L, 2L);
        User follower = new User();
        follower.setId(userId);
        User follower1 = new User();
        follower.setId(2L);
        User friend1 = new User();
        friend1.setId(3L);

        Friendship friendship1 = new Friendship();
        friendship1.setId(2L);
        friendship1.setFollower(follower);
        friendship1.setFriend(friend1);
        friendship1.setState(SUBSCRIBER);
        Friendship friendship2 = new Friendship();
        friendship2.setId(3L);
        friendship2.setFollower(follower1);
        friendship2.setFriend(friend1);
        friendship2.setState(SUBSCRIBER);

        when(friendshipRepository.findAllByFollowerIdIn(ids))
                .thenReturn(Arrays.asList(friendship1, friendship2));
        when(friendshipRepository.saveAll(anyList()))
                .thenReturn(Arrays.asList(friendship1, friendship2));

        List<FriendshipDto> result = friendshipService.rejectFriendship(3L, ids);

        assertEquals(2, result.size());
        assertEquals(FriendshipState.SUBSCRIBER, result.get(0).getState());
        assertEquals(FriendshipState.SUBSCRIBER, result.get(1).getState());
    }

    @Test
    void testRejectFriendship_WithValidData_ShouldReturnThrowInvalidDataException() {
        long userId = 1;
        List<Long> ids = Arrays.asList(1L, 2L);
        User follower = new User();
        follower.setId(userId);
        User follower1 = new User();
        follower.setId(2L);
        User friend1 = new User();
        friend1.setId(3L);

        Friendship friendship1 = new Friendship();
        friendship1.setId(2L);
        friendship1.setFollower(follower);
        friendship1.setFriend(friend1);
        friendship1.setState(CANCELED);
        Friendship friendship2 = new Friendship();
        friendship2.setId(3L);
        friendship2.setFollower(follower1);
        friendship2.setFriend(friend1);
        friendship2.setState(CANCELED);
        List<Friendship> sublist = Arrays.asList(friendship1, friendship2);
        InvalidDataException expected = new InvalidDataException("Status for friendship incorrect");

        when(friendshipRepository.findAllByFollowerIdIn(ids))
                .thenReturn(Arrays.asList(friendship1, friendship2));

        Exception exception = assertThrows(InvalidDataException.class,
                () -> friendshipService.rejectFriendship(3L, ids));

        assertEquals(expected.getMessage(), exception.getMessage());
        verify(friendshipRepository, never()).saveAll(sublist);
    }

    @Test
    void testRejectFriendship_WithValidData_ShouldReturnThrowInvalidDataExceptionYourRequest() {
        long userId = 1;
        List<Long> ids = Arrays.asList(1L, 2L);
        User follower = new User();
        follower.setId(userId);
        User follower1 = new User();
        follower.setId(2L);
        User friend1 = new User();
        friend1.setId(3L);

        Friendship friendship1 = new Friendship();
        friendship1.setId(2L);
        friendship1.setFollower(follower);
        friendship1.setFriend(friend1);
        friendship1.setState(CANCELED);
        Friendship friendship2 = new Friendship();
        friendship2.setId(3L);
        friendship2.setFollower(follower1);
        friendship2.setFriend(friend1);
        friendship2.setState(CANCELED);
        List<Friendship> sublist = Arrays.asList(friendship1, friendship2);
        InvalidDataException expected = new InvalidDataException("You can only change your requests");

        when(friendshipRepository.findAllByFollowerIdIn(ids))
                .thenReturn(Arrays.asList(friendship1, friendship2));

        Exception exception = assertThrows(InvalidDataException.class,
                () -> friendshipService.rejectFriendship(userId, ids));

        assertEquals(expected.getMessage(), exception.getMessage());
        verify(friendshipRepository, never()).saveAll(sublist);
    }

    @Test
    void testRejectFriendship_WithNoFriendshipRequests_ShouldThrowInvalidDataException() {
        long userId = 1;
        List<Long> ids = Arrays.asList(2L, 3L, 4L);

        when(friendshipRepository.findAllByFollowerIdIn(ids)).thenReturn(Collections.emptyList());

        assertThrows(InvalidDataException.class,
                () -> friendshipService.rejectFriendship(userId, ids));
    }


    @Test
    void testRequestChat_Successful() {
        User fromUser = new User();
        User toUser = new User();
        toUser.setId(2L);
        fromUser.setId(1L);
        Friendship friendship = new Friendship();
        friendship.setFollower(fromUser);
        friendship.setFriend(toUser);
        friendship.setState(FRIENDSHIP);
        when(friendshipRepository.findByFollowerIdAndFriendId(fromUser.getId(), toUser.getId()))
                .thenReturn(Optional.of(friendship));
        when(friendshipRepository.save(any(Friendship.class))).thenReturn(friendship);

        FriendshipDto result = friendshipService.requestChat(fromUser.getId(), toUser.getId());

        assertNotNull(result);
        assertEquals(REQUEST, friendship.getChat());
    }

    @Test
    void testRequestChat_FriendshipNotFound() {
        long fromUser = 1;
        long toUser = 2;
        when(friendshipRepository.findByFollowerIdAndFriendId(fromUser, toUser))
                .thenReturn(Optional.empty());

        assertThrows(FriendshipNotFoundException.class,
                () -> friendshipService.requestChat(fromUser, toUser));
    }

    @Test
    void testRequestChat_InvalidData() {
        long fromUser = 1;
        long toUser = 2;
        Friendship friendship = new Friendship();
        friendship.setState(CANCELED);
        when(friendshipRepository.findByFollowerIdAndFriendId(fromUser, toUser)).thenReturn(Optional.of(friendship));

        assertThrows(InvalidDataException.class,
                () -> friendshipService.requestChat(fromUser, toUser));
    }

    @Test
    void testDeleteFriendshipRequest() {
        long followerId = 1;
        long subsId = 2;

        when(friendshipRepository.existsByFollowerIdAndFriendId(followerId, subsId)).thenReturn(true);
        friendshipService.deleteFriendshipRequest(followerId, subsId);

        verify(friendshipRepository, times(1)).deleteByFollowerIdAndFriendId(followerId, subsId);
    }

    @Test
    void testDeleteFriendshipRequest_UserNotFoundException() {
        long followerId = 1;
        long subsId = 2;

        when(friendshipRepository.existsByFollowerIdAndFriendId(followerId, subsId)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> friendshipService.deleteFriendshipRequest(followerId, subsId));
        verify(friendshipRepository, never()).deleteByFollowerIdAndFriendId(followerId, subsId);
    }

    @Test
    void testDeleteFriendship() {
        long followerId = 1;
        long subsId = 2;
        Friendship friendship = new Friendship();
        User friend = new User();
        User subscriber = new User();
        when(friendshipRepository.existsByFollowerIdAndFriendId(followerId, subsId))
                .thenReturn(true);
        when(friendshipRepository.findByFollowerIdAndFriendId(subsId, followerId))
                .thenReturn(Optional.of(friendship));
        when(userRepository.findById(followerId)).thenReturn(Optional.of(friend));
        when(userRepository.findById(subsId)).thenReturn(Optional.of(subscriber));

        friendshipService.deleteFriendship(followerId, subsId);

        verify(friendshipRepository, times(1)).deleteByFollowerIdAndFriendId(followerId, subsId);
        verify(friendshipRepository, times(1)).save(friendship);
        assertEquals(subscriber, friendship.getFollower());
        assertEquals(friend, friendship.getFriend());
        assertEquals(SUBSCRIBER, friendship.getState());
    }

    @Test
    void testDeleteFriendship_ThrowsUserNotFoundException() {
        long followerId = 1;
        long subsId = 2;
        when(friendshipRepository.existsByFollowerIdAndFriendId(followerId, subsId)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> friendshipService.deleteFriendship(followerId, subsId));

        verify(friendshipRepository, never()).deleteByFollowerIdAndFriendId(anyLong(), anyLong());
        verify(friendshipRepository, never()).save(any(Friendship.class));
    }

    @Test
    void testDeleteFriendship_ThrowsFriendshipNotFoundException() {
        long followerId = 1;
        long subsId = 2;
        when(friendshipRepository.existsByFollowerIdAndFriendId(followerId, subsId)).thenReturn(true);
        when(friendshipRepository.findByFollowerIdAndFriendId(subsId, followerId)).thenReturn(Optional.empty());

        assertThrows(FriendshipNotFoundException.class,
                () -> friendshipService.deleteFriendship(followerId, subsId));

        verify(friendshipRepository, times(1)).deleteByFollowerIdAndFriendId(followerId, subsId);
        verify(friendshipRepository, never()).save(any(Friendship.class));
    }

    @Test
    void testGetSubscriptionEvents_ReturnsListPostDto() {
        Long userId = 1L;
        int from = 0;
        int size = 10;
        String sort = "asc";

        List<Post> mockPosts = new ArrayList<>();
        mockPosts.add(new Post());

        when(postRepository.getSubscribersEventsDateAsc(userId, PageRequest.of(from, size)))
                .thenReturn(new PageImpl<>(mockPosts));

        List<PostDto> result = friendshipService.getSubscriptionEvents(userId, from, size, sort);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testGetSubscriptionEvents_ReturnsEmptyListPostDto() {
        Long userId = 1L;
        int from = 0;
        int size = 10;
        String sort = "asc";

        List<Post> mockPosts = new ArrayList<>();

        when(postRepository.getSubscribersEventsDateAsc(userId, PageRequest.of(from, size)))
                .thenReturn(new PageImpl<>(mockPosts));

        List<PostDto> result = friendshipService.getSubscriptionEvents(userId, from, size, sort);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetSubscriptionEvents_CallsGetSubscribersEventsDateAsc() {
        Long userId = 1L;
        int from = 0;
        int size = 10;
        String sort = "asc";

        when(postRepository.getSubscribersEventsDateAsc(userId, PageRequest.of(from, size)))
                .thenReturn(new PageImpl<>(new ArrayList<>()));

        friendshipService.getSubscriptionEvents(userId, from, size, sort);

        verify(postRepository, times(1))
                .getSubscribersEventsDateAsc(userId, PageRequest.of(from, size));
    }

    @Test
    void testGetSubscriptionEvents_CallsGetSubscribersEventsDateDesc() {
        Long userId = 1L;
        int from = 0;
        int size = 10;
        String sort = "desc";

        when(postRepository.getSubscribersEventsDateDesc(userId, PageRequest.of(from, size)))
                .thenReturn(new PageImpl<>(new ArrayList<>()));

        friendshipService.getSubscriptionEvents(userId, from, size, sort);

        verify(postRepository, times(1))
                .getSubscribersEventsDateDesc(userId, PageRequest.of(from, size));
    }
}