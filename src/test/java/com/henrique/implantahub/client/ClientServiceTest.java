
package com.henrique.implantahub.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    private CreateClientRequest validRequest() {
        return new CreateClientRequest(
                "Acme Sistemas Ltda",
                "Acme",
                "12345678000199",
                "contato@acme.com",
                "11999999999"
        );
    }

    @Test
    void deveCriarClienteComSucessoQuandoCnpjNaoExiste() {
        CreateClientRequest request = validRequest();

        when(clientRepository.existsByCnpj(request.cnpj()))
                .thenReturn(false);

        when(clientRepository.save(any(Client.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ClientResponse response = clientService.create(request);

        verify(clientRepository).existsByCnpj(request.cnpj());

        ArgumentCaptor<Client> clientCaptor =
                ArgumentCaptor.forClass(Client.class);

        verify(clientRepository).save(clientCaptor.capture());

        Client persistedClient = clientCaptor.getValue();

        assertThat(persistedClient.getCorporateName())
                .isEqualTo(request.corporateName());

        assertThat(persistedClient.getTradeName())
                .isEqualTo(request.tradeName());

        assertThat(persistedClient.getCnpj())
                .isEqualTo(request.cnpj());

        assertThat(persistedClient.getEmail())
                .isEqualTo(request.email());

        assertThat(persistedClient.getPhone())
                .isEqualTo(request.phone());

        assertThat(persistedClient.getStatus())
                .isEqualTo(ClientStatus.ACTIVE);

        assertThat(persistedClient.getCreatedAt())
                .isNotNull();

        assertThat(persistedClient.getUpdatedAt())
                .isNotNull();

        assertThat(persistedClient.getCreatedAt())
                .isEqualTo(persistedClient.getUpdatedAt());

        assertThat(response.corporateName())
                .isEqualTo(request.corporateName());

        assertThat(response.tradeName())
                .isEqualTo(request.tradeName());

        assertThat(response.cnpj())
                .isEqualTo(request.cnpj());

        assertThat(response.email())
                .isEqualTo(request.email());

        assertThat(response.phone())
                .isEqualTo(request.phone());

        assertThat(response.status())
                .isEqualTo(ClientStatus.ACTIVE);

        assertThat(response.createdAt())
                .isEqualTo(persistedClient.getCreatedAt());

        assertThat(response.updatedAt())
                .isEqualTo(persistedClient.getUpdatedAt());
    }

    @Test
    void deveLancarDuplicateCnpjExceptionQuandoCnpjJaExiste() {
        CreateClientRequest request = validRequest();

        when(clientRepository.existsByCnpj(request.cnpj()))
                .thenReturn(true);

        assertThatThrownBy(() -> clientService.create(request))
                .isInstanceOf(DuplicateCnpjException.class)
                .hasMessageContaining(request.cnpj());

        verify(clientRepository).existsByCnpj(request.cnpj());

        verify(clientRepository, never())
                .save(any(Client.class));
    }

    @Test
    void deveRetornarClienteQuandoIdExiste() {
        Long id = 42L;

        Instant createdAt =
                Instant.parse("2026-09-23T10:00:00Z");

        Instant updatedAt =
                Instant.parse("2026-09-23T11:30:00Z");

        // Simula uma entidade recuperada do banco com ID preenchido.
        // Não precisamos adicionar setters nem modificar a entidade.
        Client foundClient = mock(Client.class);

        when(foundClient.getId()).thenReturn(id);
        when(foundClient.getCorporateName()).thenReturn("Acme Sistemas Ltda");
        when(foundClient.getTradeName()).thenReturn("Acme");
        when(foundClient.getCnpj()).thenReturn("12345678000199");
        when(foundClient.getEmail()).thenReturn("contato@acme.com");
        when(foundClient.getPhone()).thenReturn("11999999999");
        when(foundClient.getStatus()).thenReturn(ClientStatus.ACTIVE);
        when(foundClient.getCreatedAt()).thenReturn(createdAt);
        when(foundClient.getUpdatedAt()).thenReturn(updatedAt);

        when(clientRepository.findById(id))
                .thenReturn(Optional.of(foundClient));

        ClientResponse response = clientService.findById(id);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.corporateName())
                .isEqualTo("Acme Sistemas Ltda");
        assertThat(response.tradeName()).isEqualTo("Acme");
        assertThat(response.cnpj()).isEqualTo("12345678000199");
        assertThat(response.email()).isEqualTo("contato@acme.com");
        assertThat(response.phone()).isEqualTo("11999999999");
        assertThat(response.status()).isEqualTo(ClientStatus.ACTIVE);
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.updatedAt()).isEqualTo(updatedAt);

        verify(clientRepository).findById(id);

        verify(clientRepository, never())
                .save(any(Client.class));

        verify(clientRepository, never())
                .existsByCnpj(anyString());

        verifyNoMoreInteractions(clientRepository);
    }

    @Test
    void deveLancarClientNotFoundExceptionQuandoIdNaoExiste() {
        Long id = 99L;

        when(clientRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.findById(id))
                .isInstanceOf(ClientNotFoundException.class)
                .hasMessageContaining(id.toString());

        verify(clientRepository).findById(id);

        verify(clientRepository, never())
                .save(any(Client.class));

        verify(clientRepository, never())
                .existsByCnpj(anyString());

        verifyNoMoreInteractions(clientRepository);
    }

    @Test
    void deveRetornarPaginaDeClientesComMetadados() {
        Pageable pageable =
                PageRequest.of(1, 2, Sort.by("corporateName"));

        Client alpha = new Client(
                "Alpha Sistemas Ltda", "Alpha", "11111111000111",
                "contato@alpha.com", null, ClientStatus.ACTIVE
        );

        Client beta = new Client(
                "Beta Software Ltda", "Beta", "22222222000122",
                "contato@beta.com", null, ClientStatus.INACTIVE
        );

        when(clientRepository.findAll(any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(
                        List.of(alpha, beta),
                        invocation.getArgument(0),
                        5
                ));

        ClientPageResponse response = clientService.findAll(pageable);

        assertThat(response.content())
                .extracting(ClientResponse::corporateName)
                .containsExactly("Alpha Sistemas Ltda", "Beta Software Ltda");

        assertThat(response.content())
                .extracting(ClientResponse::status)
                .containsExactly(ClientStatus.ACTIVE, ClientStatus.INACTIVE);

        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.totalElements()).isEqualTo(5);
        assertThat(response.totalPages()).isEqualTo(3);
        assertThat(response.first()).isFalse();
        assertThat(response.last()).isFalse();

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(clientRepository).findAll(pageableCaptor.capture());

        Pageable sentToRepository = pageableCaptor.getValue();

        assertThat(sentToRepository.getPageNumber()).isEqualTo(1);
        assertThat(sentToRepository.getPageSize()).isEqualTo(2);

        // O id é acrescentado como critério de desempate.
        assertThat(sentToRepository.getSort())
                .isEqualTo(Sort.by("corporateName").and(Sort.by("id")));

        verifyNoMoreInteractions(clientRepository);
    }

    @Test
    void naoDeveDuplicarOrdenacaoPorIdQuandoJaInformada() {
        Pageable pageable =
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id"));

        when(clientRepository.findAll(any(Pageable.class)))
                .thenReturn(Page.empty(pageable));

        clientService.findAll(pageable);

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(clientRepository).findAll(pageableCaptor.capture());

        assertThat(pageableCaptor.getValue().getSort())
                .isEqualTo(Sort.by(Sort.Direction.DESC, "id"));
    }

    @Test
    void deveLancarInvalidClientSortExceptionQuandoCampoNaoPermitido() {
        Pageable pageable = PageRequest.of(
                0, 20, Sort.by("corporateName", "cnpj")
        );

        assertThatThrownBy(() -> clientService.findAll(pageable))
                .isInstanceOf(InvalidClientSortException.class)
                .hasMessageContaining("cnpj");

        verifyNoInteractions(clientRepository);
    }

    @Test
    void deveRetornarPaginaVaziaQuandoNaoExistemClientes() {
        Pageable pageable = PageRequest.of(0, 20);

        when(clientRepository.findAll(any(Pageable.class)))
                .thenAnswer(invocation ->
                        Page.empty(invocation.getArgument(0)));

        ClientPageResponse response = clientService.findAll(pageable);

        assertThat(response.content()).isEmpty();
        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(20);
        assertThat(response.totalElements()).isZero();
        assertThat(response.totalPages()).isZero();
        assertThat(response.first()).isTrue();
        assertThat(response.last()).isTrue();
    }
}