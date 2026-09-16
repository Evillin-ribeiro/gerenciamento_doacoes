CREATE TABLE usuario (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome            VARCHAR(150)    NOT NULL,
    email           VARCHAR(150)    NOT NULL UNIQUE,
    senha           VARCHAR(255)    NOT NULL,
    role            VARCHAR(20)     NOT NULL
);

CREATE TABLE doador (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome            VARCHAR(150)    NOT NULL,
    email           VARCHAR(150)    NOT NULL,
    telefone        VARCHAR(20)     NOT NULL,
    endereco        VARCHAR(255)
);

CREATE TABLE estoque (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    descricao_item  VARCHAR(150)    NOT NULL,
    categoria       VARCHAR(50)     NOT NULL,
    unidade_medida  VARCHAR(20)     NOT NULL,
    quantidade_atual DECIMAL(12,3)  NOT NULL DEFAULT 0
);

CREATE TABLE horario_atendimento (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    dia_semana          VARCHAR(20) NOT NULL,
    hora_inicio         TIME        NOT NULL,
    hora_fim            TIME        NOT NULL,
    capacidade_maxima   INT         NOT NULL
);

CREATE TABLE doacao (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    doador_id       BIGINT          NOT NULL,
    tipo            VARCHAR(20)     NOT NULL,
    status          VARCHAR(20)     NOT NULL,
    data_criacao    DATETIME        NOT NULL,
    CONSTRAINT fk_doacao_doador FOREIGN KEY (doador_id) REFERENCES doador (id)
);

CREATE TABLE item_doacao (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    doacao_id       BIGINT          NOT NULL,
    estoque_id      BIGINT          NOT NULL,
    quantidade      DECIMAL(12,3)   NOT NULL,
    CONSTRAINT fk_item_doacao_doacao FOREIGN KEY (doacao_id) REFERENCES doacao (id),
    CONSTRAINT fk_item_doacao_estoque FOREIGN KEY (estoque_id) REFERENCES estoque (id)
);

CREATE TABLE doacao_financeira (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    doacao_id       BIGINT          NOT NULL UNIQUE,
    valor           DECIMAL(12,2)   NOT NULL,
    comprovante_url VARCHAR(500),
    CONSTRAINT fk_doacao_financeira_doacao FOREIGN KEY (doacao_id) REFERENCES doacao (id)
);

CREATE TABLE agendamento (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    doacao_id                   BIGINT NOT NULL UNIQUE,
    horario_atendimento_id      BIGINT NOT NULL,
    data_entrega                DATE   NOT NULL,
    CONSTRAINT fk_agendamento_doacao FOREIGN KEY (doacao_id) REFERENCES doacao (id),
    CONSTRAINT fk_agendamento_horario FOREIGN KEY (horario_atendimento_id) REFERENCES horario_atendimento (id)
);

CREATE TABLE distribuicao (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    data            DATETIME        NOT NULL,
    beneficiario    VARCHAR(150)    NOT NULL,
    observacao      VARCHAR(500)
);

CREATE TABLE movimentacao_estoque (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    estoque_id                  BIGINT          NOT NULL,
    usuario_id                  BIGINT          NOT NULL,
    tipo                        VARCHAR(20)     NOT NULL,
    quantidade                  DECIMAL(12,3)   NOT NULL,
    data                        DATETIME        NOT NULL,
    referencia_doacao_id        BIGINT,
    referencia_distribuicao_id  BIGINT,
    CONSTRAINT fk_movimentacao_estoque FOREIGN KEY (estoque_id) REFERENCES estoque (id),
    CONSTRAINT fk_movimentacao_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT fk_movimentacao_doacao FOREIGN KEY (referencia_doacao_id) REFERENCES doacao (id),
    CONSTRAINT fk_movimentacao_distribuicao FOREIGN KEY (referencia_distribuicao_id) REFERENCES distribuicao (id)
);
