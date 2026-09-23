package com.henrique.implantahub.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
}