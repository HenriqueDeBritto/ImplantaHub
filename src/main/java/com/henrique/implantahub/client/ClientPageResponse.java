package com.henrique.implantahub.client;

import org.springframework.data.domain.Page;

import java.util.List;

public record ClientPageResponse(
        List<ClientResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    static ClientPageResponse from(Page<ClientResponse> page) {
        return new ClientPageResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
