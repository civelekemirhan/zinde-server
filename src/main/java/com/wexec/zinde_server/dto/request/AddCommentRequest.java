package com.wexec.zinde_server.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AddCommentRequest {
    @NotBlank
    @Size(max = 1000)
    private String content;

    private Long parentId;
}
