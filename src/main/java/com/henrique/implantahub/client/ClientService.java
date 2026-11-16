
package com.henrique.implantahub.client;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClientService {

    static final List<String> SORTABLE_PROPERTIES = List.of(
            "id",
            "corporateName",
            "tradeName",
            "createdAt",
            "updatedAt"
    );

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

    @Transactional(readOnly = true)
    public ClientResponse findById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));

        return toResponse(client);
    }

    @Transactional(readOnly = true)
    public ClientPageResponse findAll(ClientStatus status, Pageable pageable) {
        validateSort(pageable.getSort());

        Pageable stablePageable = withIdTiebreaker(pageable);

        // Sem status informado, a listagem retorna clientes de todos os status.
        Page<Client> clients = status == null
                ? clientRepository.findAll(stablePageable)
                : clientRepository.findAllByStatus(status, stablePageable);

        return ClientPageResponse.from(clients.map(this::toResponse));
    }

    private void validateSort(Sort sort) {
        for (Sort.Order order : sort) {
            if (!SORTABLE_PROPERTIES.contains(order.getProperty())) {
                throw new InvalidClientSortException(order.getProperty());
            }
        }
    }

    // Garante uma ordem determinística entre páginas quando o campo
    // ordenado possui valores repetidos (ex.: mesma razão social).
    private Pageable withIdTiebreaker(Pageable pageable) {
        Sort sort = pageable.getSort();

        if (sort.getOrderFor("id") != null) {
            return pageable;
        }

        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort.and(Sort.by("id"))
        );
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