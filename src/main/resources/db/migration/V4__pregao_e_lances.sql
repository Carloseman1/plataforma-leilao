-- Estrutura do pregão ao vivo.
--
-- lance.uuid é a chave de idempotência: quem envia o lance gera o UUID, e o
-- índice único abaixo é o que impede o mesmo lance de entrar duas vezes quando
-- o cliente reenvia ou o Kafka reentrega a mensagem.

ALTER TABLE lance ADD COLUMN uuid UUID;
UPDATE lance SET uuid = gen_random_uuid() WHERE uuid IS NULL;
ALTER TABLE lance ALTER COLUMN uuid SET NOT NULL;
ALTER TABLE lance ADD CONSTRAINT lance_uuid_key UNIQUE (uuid);

-- Por que o lance foi recusado. Nulo quando ele foi aceito.
ALTER TABLE lance ADD COLUMN motivo_recusa VARCHAR(40);

-- O lance da casa não tem comprador por trás.
ALTER TABLE lance ALTER COLUMN usuario_id DROP NOT NULL;

-- Janela de cada lote dentro do pregão.
ALTER TABLE lote ADD COLUMN aberto_em TIMESTAMP(6);
ALTER TABLE lote ADD COLUMN fecha_em TIMESTAMP(6);

-- Quando o leiloeiro bateu o martelo de abertura e de encerramento.
ALTER TABLE leilao ADD COLUMN iniciado_em TIMESTAMP(6);
ALTER TABLE leilao ADD COLUMN encerrado_em TIMESTAMP(6);

CREATE INDEX idx_lance_lote_criado ON lance (lote_id, criado_em DESC);
CREATE INDEX idx_lance_status ON lance (status);
