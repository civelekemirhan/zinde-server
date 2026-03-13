package com.wexec.zinde_server.dto.request;

import com.wexec.zinde_server.entity.Gender;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Ad boş olamaz")
    @Size(max = 50, message = "Ad en fazla 50 karakter olabilir")
    private String firstName;

    @NotBlank(message = "Soyad boş olamaz")
    @Size(max = 50, message = "Soyad en fazla 50 karakter olabilir")
    private String lastName;

    @NotBlank(message = "Kullanıcı adı boş olamaz")
    @Size(min = 3, max = 30, message = "Kullanıcı adı 3-30 karakter arasında olmalıdır")
    @Pattern(
            regexp = "^[a-zA-Z0-9_.]+$",
            message = "Kullanıcı adı sadece harf, rakam, alt çizgi (_) ve nokta (.) içerebilir"
    )
    private String username;

    @NotBlank(message = "E-posta adresi boş olamaz")
    @Email(message = "Geçerli bir e-posta adresi girin")
    private String email;

    @NotBlank(message = "Şifre boş olamaz")
    @Size(min = 8, message = "Şifre en az 8 karakter olmalıdır")
    private String password;

    @NotNull(message = "Cinsiyet seçimi zorunludur")
    private Gender gender;
}
