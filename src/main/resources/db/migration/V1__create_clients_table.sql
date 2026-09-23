CREATE TABLE clients (
                         id              BIGINT GENERATED ALWAYS AS IDENTITY,
                         corporate_name  VARCHAR(255) NOT NULL,
                         trade_name      VARCHAR(255),
                         cnpj            VARCHAR(14) NOT NULL,
                         email           VARCHAR(255) NOT NULL,
                         phone           VARCHAR(20),
                         status          VARCHAR(20) NOT NULL,
                         created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT pk_clients
                             PRIMARY KEY (id),

                         CONSTRAINT uq_clients_cnpj
                             UNIQUE (cnpj),

                         CONSTRAINT ck_clients_cnpj_format
                             CHECK (cnpj ~ '^[0-9]{14}$'),

    CONSTRAINT ck_clients_status
        CHECK (status IN ('ACTIVE', 'INACTIVE'))
);