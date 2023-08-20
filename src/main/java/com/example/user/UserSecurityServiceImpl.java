package com.example.user;

import com.example.exceptions.BadRequestException;
import com.example.security.JwtCore;
import com.example.security.TokenDto;
import com.example.user.dto.SigninRequest;
import com.example.user.dto.SignupRequest;
import com.example.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserSecurityServiceImpl implements UserSecurityService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtCore jwtCore;

    @Override
    public void signup(SignupRequest signupRequest) {
        isCheckExistsUserDetails(signupRequest);
        createAndSaveUser(signupRequest);
    }

    @Override
    public TokenDto signin(SigninRequest signinRequest) {
        Authentication authentication = null;
        try {
            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(signinRequest.getUsername(),
                            signinRequest.getPassword()));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Unauthenticated user");
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtCore.generateToken(authentication);

        return TokenDto.builder()
                .token(jwt)
                .build();
    }

    private void createAndSaveUser(SignupRequest signupRequest) {
        String hashed = passwordEncoder.encode(signupRequest.getPassword());
        User user = new User();
        user.setEmail(signupRequest.getUsername());
        user.setPassword(hashed);
        user.setUsername(signupRequest.getUsername());
        repository.save(user);
    }

    private void isCheckExistsUserDetails(SignupRequest signupRequest) {
        if (repository.existsByUsername(signupRequest.getUsername())) {
            throw new BadRequestException("Choose different username");
        }
        if (repository.existsByEmail(signupRequest.getEmail())) {
            throw new BadRequestException("Choose different email");
        }
    }
}
