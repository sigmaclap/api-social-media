package com.example.friendship.controller;

import com.example.friendship.FriendshipService;
import com.example.friendship.dto.FriendshipDto;
import com.example.post.dto.PostDto;
import com.example.utills.UserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/secured/users/friendships")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Взаимодействие пользователей")
public class FriendshipController {
    private final FriendshipService friendshipService;
    private final UserHolder userHolder;

    @PostMapping("/{friendId}")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Создание подписки на другого пользователя",
            description = "Позволяет подписаться на другого пользователя"
    )
    public FriendshipDto requestFriendship(@PathVariable("friendId") long friendId) {
        return friendshipService.requestFriendship(userHolder.getUserId(), friendId);
    }

    @DeleteMapping("/{subsId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Удаление подписки на другого пользователя"
    )
    public void deleteFriendshipRequest(@PathVariable("subsId") long subsId) {
        friendshipService.deleteFriendshipRequest(userHolder.getUserId(), subsId);
    }

    @DeleteMapping("/delete/{subsId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Удаление дружбы с другим пользователем"
    )
    public void deleteFriendship(@PathVariable("subsId") long subsId) {
        friendshipService.deleteFriendship(userHolder.getUserId(), subsId);
    }

    @PatchMapping("/approve")
    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Подтверждение в друзья подписчика"
    )
    public List<FriendshipDto> approveFriendship(@RequestParam(value = "subscribersIds") List<Long> subscribersIds) {
        return friendshipService.approveFriendship(userHolder.getUserId(), subscribersIds);
    }

    @PatchMapping("/chat/{toUser}")
    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Отправка запроса на создание чата с другим пользователем"
    )
    public FriendshipDto requestChat(@PathVariable long toUser) {
        return friendshipService.requestChat(userHolder.getUserId(), toUser);
    }

    @PatchMapping("/reject")
    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Отклонение заявки в друзья другим пользователем"
    )
    public List<FriendshipDto> rejectFriendship(@RequestParam(value = "subscribersIds") List<Long> subscribersIds) {
        return friendshipService.rejectFriendship(userHolder.getUserId(), subscribersIds);
    }

    @GetMapping("/events")
    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Получение всех событий подписчиков"
    )
    public List<PostDto> getSubscriptionEvents(@RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                               @RequestParam(defaultValue = "20") @Positive Integer size,
                                               @RequestParam(defaultValue = "DESC") @Parameter(description = "Сортировка времени по умолчанию - по убыванию") String sort) {
        return friendshipService.getSubscriptionEvents(userHolder.getUserId(), from, size, sort);

    }
}
