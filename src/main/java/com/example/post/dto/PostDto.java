package com.example.post.dto;

import com.example.utills.CommonPatterns;
import com.example.utills.validated.Marker;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Сущность поста")
public class PostDto {
    private Long id;
    @NotBlank(groups = Marker.OnCreate.class)
    private String description;
    @NotBlank(groups = Marker.OnCreate.class)
    private String text;
    @NotBlank(groups = Marker.OnCreate.class)
    private String image;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = CommonPatterns.DATE_FORMAT)
    private LocalDateTime created;
}
