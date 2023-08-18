package com.example.friendship.mapper;

import com.example.friendship.dto.FriendshipDto;
import com.example.friendship.entity.Friendship;
import com.example.user.mapper.UserMapper;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FriendshipMapper {
    public FriendshipDto toDto(Friendship friendship) {
        return FriendshipDto.builder()
                .id(friendship.getId())
                .friend(UserMapper.toDtoUser(friendship.getFriend()))
                .followerId(friendship.getFollower().getId())
                .state(friendship.getState())
                .chat(friendship.getChat())
                .build();
    }

    public Friendship toEntity(FriendshipDto dto) {
        return Friendship.builder()
                .id(dto.getId())
                .state(dto.getState())
                .friend(UserMapper.toUser(dto.getFriend()))
                .chat(dto.getChat())
                .build();
    }
}
