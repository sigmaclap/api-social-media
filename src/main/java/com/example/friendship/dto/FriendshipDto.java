package com.example.friendship.dto;

import com.example.friendship.states.ChatState;
import com.example.friendship.states.FriendshipState;
import com.example.user.dto.UserDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Сущность дружбы")
public class FriendshipDto {
    private Long id;
    private Long followerId;
    private UserDto friend;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private FriendshipState state;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private ChatState chat = ChatState.CLOSED;
}
