package com.wexec.zinde_server.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class CreatePollPostRequest {

    /** Anket üstüne isteğe bağlı açıklama metni */
    @Size(max = 500)
    private String caption;

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotNull
    @Size(min = 2, max = 10, message = "En az 2, en fazla 10 seçenek girilebilir.")
    private List<@NotBlank @Size(max = 100) String> options;

    @NotNull
    @Future(message = "Bitiş tarihi gelecekte olmalıdır.")
    private LocalDateTime expiresAt;
}
