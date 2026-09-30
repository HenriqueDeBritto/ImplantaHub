
package com.henrique.implantahub.client;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientController.class)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClientService clientService;

    @Test
    void deveRetornar201QuandoCadastroForBemSucedido() throws Exception {
        String requestJson = """
                {
                    "corporateName": "Acme Sistemas Ltda",
                    "tradeName": "Acme",
                    "cnpj": "12345678000199",
                    "email": "contato@acme.com",
                    "phone": "11999999999"
                }
                """;

        Instant now = Instant.parse("2026-09-23T10:00:00Z");

        ClientResponse mockedResponse = new ClientResponse(
                1L,
                "Acme Sistemas Ltda",
                "Acme",
                "12345678000199",
                "contato@acme.com",
                "11999999999",
                ClientStatus.ACTIVE,
                now,
                now
        );

        when(clientService.create(any(CreateClientRequest.class)))
                .thenReturn(mockedResponse);

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.corporateName").value("Acme Sistemas Ltda"))
                .andExpect(jsonPath("$.tradeName").value("Acme"))
                .andExpect(jsonPath("$.cnpj").value("12345678000199"))
                .andExpect(jsonPath("$.email").value("contato@acme.com"))
                .andExpect(jsonPath("$.phone").value("11999999999"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());

        ArgumentCaptor<CreateClientRequest> requestCaptor =
                ArgumentCaptor.forClass(CreateClientRequest.class);

        verify(clientService, times(1))
                .create(requestCaptor.capture());

        CreateClientRequest sentToService = requestCaptor.getValue();

        assertThat(sentToService.corporateName())
                .isEqualTo("Acme Sistemas Ltda");

        assertThat(sentToService.tradeName())
                .isEqualTo("Acme");

        assertThat(sentToService.cnpj())
                .isEqualTo("12345678000199");

        assertThat(sentToService.email())
                .isEqualTo("contato@acme.com");

        assertThat(sentToService.phone())
                .isEqualTo("11999999999");
    }

    @Test
    void deveRetornar409QuandoCnpjJaExistir() throws Exception {
        String requestJson = """
                {
                    "corporateName": "Acme Sistemas Ltda",
                    "tradeName": "Acme",
                    "cnpj": "12345678000199",
                    "email": "contato@acme.com",
                    "phone": "11999999999"
                }
                """;

        when(clientService.create(any(CreateClientRequest.class)))
                .thenThrow(new DuplicateCnpjException("12345678000199"));

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Duplicate CNPJ"))
                .andExpect(jsonPath("$.detail").value(
                        "A client with this CNPJ already exists."
                ))
                .andExpect(content().string(
                        not(containsString("12345678000199"))
                ));

        verify(clientService, times(1))
                .create(any(CreateClientRequest.class));
    }

    @Test
    void deveRetornar400QuandoRequestForInvalido() throws Exception {
        String requestJson = """
                {
                    "corporateName": "Acme Sistemas Ltda",
                    "tradeName": "Acme",
                    "cnpj": "",
                    "email": "email-invalido",
                    "phone": "11999999999"
                }
                """;

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.cnpj").isArray())
                .andExpect(jsonPath("$.errors.cnpj.length()").value(2))
                .andExpect(jsonPath("$.errors.email").isArray())
                .andExpect(jsonPath("$.errors.email.length()").value(1))
                .andExpect(content().string(
                        not(containsString("email-invalido"))
                ));

        verifyNoInteractions(clientService);
    }

    @Test
    void deveRetornar200QuandoClienteForEncontradoPorId() throws Exception {
        Instant createdAt = Instant.parse("2026-09-23T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-09-23T11:30:00Z");

        ClientResponse mockedResponse = new ClientResponse(
                42L,
                "Acme Sistemas Ltda",
                "Acme",
                "12345678000199",
                "contato@acme.com",
                "11999999999",
                ClientStatus.ACTIVE,
                createdAt,
                updatedAt
        );

        when(clientService.findById(42L))
                .thenReturn(mockedResponse);

        mockMvc.perform(get("/api/clients/42"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.corporateName").value("Acme Sistemas Ltda"))
                .andExpect(jsonPath("$.tradeName").value("Acme"))
                .andExpect(jsonPath("$.cnpj").value("12345678000199"))
                .andExpect(jsonPath("$.email").value("contato@acme.com"))
                .andExpect(jsonPath("$.phone").value("11999999999"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());

        verify(clientService, times(1))
                .findById(42L);
    }

    @Test
    void deveRetornar404QuandoClienteNaoExistir() throws Exception {
        when(clientService.findById(999999L))
                .thenThrow(new ClientNotFoundException(999999L));

        mockMvc.perform(get("/api/clients/999999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Client Not Found"))
                .andExpect(jsonPath("$.detail").value("Client not found."));

        verify(clientService, times(1))
                .findById(999999L);
    }

    @Test
    void deveRetornar200ComPaginaDeClientes() throws Exception {
        Instant now = Instant.parse("2026-09-23T10:00:00Z");

        ClientResponse client = new ClientResponse(
                1L,
                "Acme Sistemas Ltda",
                "Acme",
                "12345678000199",
                "contato@acme.com",
                "11999999999",
                ClientStatus.ACTIVE,
                now,
                now
        );

        when(clientService.findAll(any(Pageable.class)))
                .thenReturn(new ClientPageResponse(
                        List.of(client), 0, 20, 1, 1, true, true
                ));

        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].corporateName")
                        .value("Acme Sistemas Ltda"))
                .andExpect(jsonPath("$.content[0].cnpj").value("12345678000199"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void deveAplicarPaginacaoPadraoQuandoParametrosNaoForemInformados()
            throws Exception {
        when(clientService.findAll(any(Pageable.class)))
                .thenReturn(emptyPage());

        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk());

        Pageable pageable = capturePageable();

        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(20);
        assertThat(pageable.getSort())
                .isEqualTo(Sort.by(Sort.Direction.ASC, "corporateName"));
    }

    @Test
    void deveRepassarParametrosDePaginacaoInformados() throws Exception {
        when(clientService.findAll(any(Pageable.class)))
                .thenReturn(emptyPage());

        mockMvc.perform(get("/api/clients")
                        .param("page", "2")
                        .param("size", "5")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk());

        Pageable pageable = capturePageable();

        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort())
                .isEqualTo(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Test
    void deveLimitarTamanhoDaPaginaAoMaximoConfigurado() throws Exception {
        when(clientService.findAll(any(Pageable.class)))
                .thenReturn(emptyPage());

        mockMvc.perform(get("/api/clients").param("size", "500"))
                .andExpect(status().isOk());

        assertThat(capturePageable().getPageSize()).isEqualTo(100);
    }

    @Test
    void deveRetornar400QuandoOrdenacaoNaoForPermitida() throws Exception {
        when(clientService.findAll(any(Pageable.class)))
                .thenThrow(new InvalidClientSortException("cnpj"));

        mockMvc.perform(get("/api/clients").param("sort", "cnpj"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Invalid Sort Property"))
                .andExpect(jsonPath("$.detail").value(
                        "One or more sort properties are not supported."
                ))
                .andExpect(jsonPath("$.allowedSortProperties").isArray())
                .andExpect(content().string(not(containsString("cnpj"))));
    }

    private ClientPageResponse emptyPage() {
        return new ClientPageResponse(List.of(), 0, 20, 0, 0, true, true);
    }

    private Pageable capturePageable() {
        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(clientService, times(1))
                .findAll(pageableCaptor.capture());

        return pageableCaptor.getValue();
    }
}