package com.henrique.implantahub.client;

import java.time.Instant;

public record ClientResponse(
        Long id,
        String corporateName,
        String tradeName,
        String cnpj,
        String email,
        String phone,
        ClientStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}