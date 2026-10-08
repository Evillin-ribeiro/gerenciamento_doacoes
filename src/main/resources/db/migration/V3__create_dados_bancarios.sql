CREATE TABLE dados_bancarios (
    id              BIGINT PRIMARY KEY,
    banco           VARCHAR(100)    NOT NULL,
    agencia         VARCHAR(20)     NOT NULL,
    conta           VARCHAR(30)     NOT NULL,
    chave_pix       VARCHAR(150)    NOT NULL,
    titular         VARCHAR(150)    NOT NULL
);

INSERT INTO dados_bancarios (id, banco, agencia, conta, chave_pix, titular) VALUES
    (1, 'A definir', 'A definir', 'A definir', 'A definir', 'A definir');
