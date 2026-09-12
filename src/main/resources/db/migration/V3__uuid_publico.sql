-- Identificador público (UUID) para leilão, lote e animal.
-- O id BIGINT continua sendo a chave interna/FK; o UUID é o que trafega na API e nas URLs,
-- evitando que ids sequenciais sejam enumerados por quem acessa o catálogo.

ALTER TABLE leilao ADD COLUMN uuid UUID;
ALTER TABLE lote   ADD COLUMN uuid UUID;
ALTER TABLE animal ADD COLUMN uuid UUID;

-- gen_random_uuid() é nativo do PostgreSQL 13+.
UPDATE leilao SET uuid = gen_random_uuid() WHERE uuid IS NULL;
UPDATE lote   SET uuid = gen_random_uuid() WHERE uuid IS NULL;
UPDATE animal SET uuid = gen_random_uuid() WHERE uuid IS NULL;

ALTER TABLE leilao ALTER COLUMN uuid SET NOT NULL;
ALTER TABLE lote   ALTER COLUMN uuid SET NOT NULL;
ALTER TABLE animal ALTER COLUMN uuid SET NOT NULL;

ALTER TABLE leilao ADD CONSTRAINT leilao_uuid_key UNIQUE (uuid);
ALTER TABLE lote   ADD CONSTRAINT lote_uuid_key   UNIQUE (uuid);
ALTER TABLE animal ADD CONSTRAINT animal_uuid_key UNIQUE (uuid);
