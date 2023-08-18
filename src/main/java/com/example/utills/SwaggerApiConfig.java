package com.example.utills;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@OpenAPIDefinition(
        info = @Info(
                title = "Social Media API",
                description = "Cоциальная медиа платформа,\n" +
                        "позволяющая пользователям регистрироваться, входить в систему, создавать\n" +
                        "посты, переписываться, подписываться на других пользователей и получать\n" +
                        "свою ленту активности.", version = "1.0.0",
                contact = @Contact(
                        name = "Ilya Shubin"
                )
        )
)
@SecurityScheme(
        name = "JWT",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class SwaggerApiConfig {
}
