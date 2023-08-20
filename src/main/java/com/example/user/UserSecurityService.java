package com.example.user;

import com.example.security.TokenDto;
import com.example.user.dto.SigninRequest;
import com.example.user.dto.SignupRequest;

public interface UserSecurityService {

    void signup(SignupRequest signupRequest);

    TokenDto signin(SigninRequest signinRequest);
}
