package com.example.user;

import com.example.exceptions.UserNotFoundException;
import com.example.user.dto.UserDto;
import com.example.user.entity.User;
import com.example.user.mapper.UserMapper;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplUnitTest {
    @InjectMocks
    UserServiceImpl userService;

    @Mock
    UserRepository repository;
    @Mock
    UserDetailsImpl userDetails;
    private final EasyRandom generator = new EasyRandom();

    @Test
    void testGetUsers_whenIdsNull_thenReturnedListUsers() {
        Pageable pagination = PageRequest.of(0, 10);
        List<Long> ids = null;
        Page<User> users = new PageImpl<>(List.of(generator.nextObject(User.class)));
        when(repository.findAll(pagination)).thenReturn(users);
        List<UserDto> expectedUsers = users.stream()
                .map(UserMapper::toDtoUser)
                .collect(Collectors.toList());

        List<UserDto> actualUsers = userService.getUsers(ids, 0, 10);

        assertEquals(expectedUsers.size(), actualUsers.size());
        assertEquals(expectedUsers.get(0), actualUsers.get(0));
        verify(repository, times(1)).findAll(pagination);
    }

    @Test
    void testGetUsers_whenIdsNotNull_thenReturnedListUsers() {
        Pageable pagination = PageRequest.of(0, 10);
        User userToSave = generator.nextObject(User.class);
        List<Long> ids = List.of(userToSave.getId());
        Page<User> users = new PageImpl<>(List.of(userToSave));
        when(repository.findAllByIdIn(ids, pagination)).thenReturn(users);
        List<UserDto> expectedList = users.stream()
                .map(UserMapper::toDtoUser)
                .collect(Collectors.toList());

        List<UserDto> actualUsers = userService.getUsers(ids, 0, 10);

        assertEquals(expectedList.size(), actualUsers.size());
        assertEquals(expectedList.get(0), actualUsers.get(0));
        verify(repository, times(1)).findAllByIdIn(ids, pagination);
    }

    @Test
    void createUser_whenValidData_thenReturnedCreateUser() {
        UserDto userDto = generator.nextObject(UserDto.class);
        User userToSave = UserMapper.toUser(userDto);
        when(repository.save(userToSave)).thenReturn(userToSave);
        UserDto expectedUser = UserMapper.toDtoUser(userToSave);

        UserDto actualUser = userService.createUser(userDto);

        assertEquals(expectedUser.getUsername(), actualUser.getUsername());
        verify(repository, times(1)).save(userToSave);
    }

    @Test
    void deleteUser_whenUserExists_thenSuccessDeleted() {
        User userToDelete = generator.nextObject(User.class);
        when(repository.findById(userToDelete.getId())).thenReturn(Optional.of(userToDelete));

        userService.deleteUser(userToDelete.getId());

        verify(repository, times(1)).delete(userToDelete);
    }

    @Test
    void loadUserByUsername_whenInvalidData_ThenLoadUserName() {
        when(repository.findUserByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.loadUserByUsername(anyString()));
    }
}