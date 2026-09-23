package com.henrique.implantahub.client;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Transactional
    public ClientResponse create(CreateClientRequest request) {
        if (clientRepository.existsByCnpj(request.cnpj())) {
            throw new DuplicateCnpjException(request.cnpj());
        }

        Client client = new Client(
                request.corporateName(),
                request.tradeName(),
                request.cnpj(),
                request.email(),
                request.phone(),
                ClientStatus.ACTIVE
        );

        Client savedClient = clientRepository.save(client);

        return toResponse(savedClient);
    }

    private ClientResponse toResponse(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getCorporateName(),
                client.getTradeName(),
                client.getCnpj(),
                client.getEmail(),
                client.getPhone(),
                client.getStatus(),
                client.getCreatedAt(),
                client.getUpdatedAt()
        );
    }
}