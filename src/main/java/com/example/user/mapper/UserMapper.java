package com.example.user.mapper;

import com.example.user.dto.UserDto;
import com.example.user.entity.User;
import lombok.experimental.UtilityClass;


@UtilityClass
public class UserMapper {
    public User toUser(UserDto userDto) {
        return User.builder()
                .id(userDto.getId())
                .username(userDto.getUsername())
                .email(userDto.getEmail())
                .build();
    }

    public UserDto toDtoUser(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .build();
    }
}
