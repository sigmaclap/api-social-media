package com.example.user.controller;

import com.example.user.UserService;
import com.example.user.dto.UserDto;
import com.example.utills.UserHolder;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/secured/users")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Пользователи")
public class UsersController {
    private final UserService userService;
    private final UserHolder userHolder;

    @GetMapping
    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Получение выбранных пользователей по ID"
    )
    public List<UserDto> getUsers(@RequestParam(required = false) List<Long> ids,
                                  @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                  @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Getting user with ids {} and pagination {} {}", ids, from, size);
        return userService.getUsers(ids, from, size);
    }

    @DeleteMapping
    @SecurityRequirement(name = "JWT")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Удаление пользователя по ID"
    )
    public void deleteUser() {
        log.info("Deleting user with id {}", userHolder.getUserId());
        userService.deleteUser(userHolder.getUserId());
    }
}
