package com.henrique.implantahub.client;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {

    boolean existsByCnpj(String cnpj);

    Page<Client> findAllByStatus(ClientStatus status, Pageable pageable);
}