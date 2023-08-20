package com.example.security;

import com.example.user.UserSecurityService;
import com.example.user.dto.SigninRequest;
import com.example.user.dto.SignupRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Transactional
@Tag(name = "Контроллер регистрации и авторизации")
public class SecurityController {

    private final UserSecurityService userService;

    @PostMapping("/signup")
    @Operation(
            summary = "Регистрация пользователя",
            description = "Позволяет зарегистрировать пользователя"
    )
    ResponseEntity<?> signup(@Valid @RequestBody SignupRequest signupRequest) {
        userService.signup(signupRequest);
        return ResponseEntity.ok("Successful registration!");
    }

    @PostMapping("/signin")
    @Operation(
            summary = "Аутентификация пользователя",
            description = "Позволяет получить доступ пользователю"
    )
    ResponseEntity<TokenDto> signin(@Valid @RequestBody SigninRequest signinRequest) {
        return ResponseEntity.ok(userService.signin(signinRequest));
    }

}
