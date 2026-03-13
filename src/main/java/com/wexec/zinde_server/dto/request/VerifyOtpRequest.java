package com.wexec.zinde_server.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpRequest {

    @NotBlank(message = "E-posta adresi boş olamaz")
    @Email(message = "Geçerli bir e-posta adresi girin")
    private String email;

    @NotBlank(message = "Doğrulama kodu boş olamaz")
    @Pattern(regexp = "\\d{6}", message = "Doğrulama kodu 6 haneli rakamlardan oluşmalıdır")
    private String code;
}
