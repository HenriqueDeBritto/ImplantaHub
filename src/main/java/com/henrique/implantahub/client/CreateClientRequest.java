package com.henrique.implantahub.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateClientRequest(

        @NotBlank
        @Size(max = 255)
        String corporateName,

        @Size(max = 255)
        String tradeName,

        @NotBlank
        @Pattern(regexp = "^[0-9]{14}$")
        String cnpj,

        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @Size(max = 20)
        String phone

) {
}