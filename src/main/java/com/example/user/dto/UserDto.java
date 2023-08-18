package com.example.user.dto;

import com.example.utills.validated.Marker;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Сущность пользователя")
public class UserDto {
    private Long id;
    @NotBlank(groups = Marker.OnCreate.class)
    @Size(min = 2, max = 250)
    private String username;
    @NotBlank(groups = Marker.OnCreate.class)
    @Email
    @Size(min = 6, max = 254)
    private String email;
}
