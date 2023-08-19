package com.example.security;

import com.example.exceptions.BadRequestException;
import com.example.user.UserRepository;
import com.example.user.dto.SigninRequest;
import com.example.user.dto.SignupRequest;
import com.example.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Transactional
@Tag(name = "Контроллер регистрации и авторизации")
public class SecurityController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtCore jwtCore;

    @PostMapping("/signup")
    @Operation(
            summary = "Регистрация пользователя",
            description = "Позволяет зарегистрировать пользователя"
    )
    ResponseEntity<?> signup(@RequestBody SignupRequest signupRequest) {
        isCheckExistsUserDetails(signupRequest);
        createAndSaveUser(signupRequest);
        return ResponseEntity.ok("Successful registration!");
    }

    @PostMapping("/signin")
    @Operation(
            summary = "Аутентификация пользователя",
            description = "Позволяет получить доступ пользователю"
    )
    ResponseEntity<TokenDto> signin(@RequestBody SigninRequest signinRequest) {
        Authentication authentication = null;
        try {
            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(signinRequest.getUsername(),
                            signinRequest.getPassword()));
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtCore.generateToken(authentication);

        return ResponseEntity
                .ok(TokenDto.builder()
                        .token(jwt)
                        .build());
    }

    private void createAndSaveUser(SignupRequest signupRequest) {
        String hashed = passwordEncoder.encode(signupRequest.getPassword());
        User user = new User();
        user.setEmail(signupRequest.getUsername());
        user.setPassword(hashed);
        user.setUsername(signupRequest.getUsername());
        userRepository.save(user);
    }

    private void isCheckExistsUserDetails(SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new BadRequestException("Choose different username");
        }
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new BadRequestException("Choose different email");
        }
    }
}
