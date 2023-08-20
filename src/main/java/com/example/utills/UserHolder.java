package com.example.utills;

import com.example.exceptions.InvalidDataException;
import com.example.user.UserDetailsImpl;
import lombok.Data;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Data
@Component
@RequestScope
public class UserHolder {
    private UserDetails userDetails;

    public Long getUserId() {
        if (!(userDetails instanceof UserDetailsImpl)) {
            throw new InvalidDataException("Unknown user type");
        }
        return ((UserDetailsImpl) userDetails).getId();
    }
}
