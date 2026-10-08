ALTER TABLE horario_atendimento ADD COLUMN usuario_id BIGINT NULL;

ALTER TABLE horario_atendimento
    ADD CONSTRAINT fk_horario_atendimento_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id);
