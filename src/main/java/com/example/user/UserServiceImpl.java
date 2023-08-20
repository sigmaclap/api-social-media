package com.example.user;

import com.example.exceptions.UserNotFoundException;
import com.example.user.dto.UserDto;
import com.example.user.entity.User;
import com.example.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        Pageable pagination = PageRequest.of(from, size);
        if (ids == null || ids.isEmpty()) {
            return repository.findAll(pagination).stream()
                    .map(UserMapper::toDtoUser)
                    .collect(Collectors.toList());
        } else {
            return repository.findAllByIdIn(ids, pagination).stream()
                    .map(UserMapper::toDtoUser)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        return UserMapper.toDtoUser(repository.save(user));
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("User deleting with id {}", userId);
        repository.delete(repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not deleted - failed")));
    }


    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = repository.findUserByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found - failed"));

        return UserDetailsImpl.build(user);
    }
}
