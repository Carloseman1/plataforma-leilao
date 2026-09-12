DELETE FROM lance;
DELETE FROM lote;
DELETE FROM leilao;

ALTER TABLE lote DROP COLUMN nome_animal;
ALTER TABLE lote DROP COLUMN sexo;
ALTER TABLE lote DROP COLUMN raca;
ALTER TABLE lote DROP COLUMN idade_anos;
ALTER TABLE lote DROP COLUMN pelagem;
ALTER TABLE lote DROP COLUMN registro;
ALTER TABLE lote DROP COLUMN descricao_curta;
ALTER TABLE lote DROP COLUMN lance_inicial;

ALTER TABLE lote ADD COLUMN animal_id BIGINT NOT NULL REFERENCES animal (id);
ALTER TABLE lote ADD COLUMN tipo_oferta VARCHAR(255) NOT NULL;
ALTER TABLE lote ADD COLUMN valor_inicial NUMERIC(12, 2) NOT NULL;
ALTER TABLE lote ADD COLUMN incremento_minimo NUMERIC(12, 2) NOT NULL;
ALTER TABLE lote ADD COLUMN status VARCHAR(255) NOT NULL;

ALTER TABLE leilao DROP CONSTRAINT IF EXISTS leilao_status_check;
ALTER TABLE lance DROP CONSTRAINT IF EXISTS lance_status_check;
ALTER TABLE lance DROP CONSTRAINT IF EXISTS lance_tipo_check;
ALTER TABLE usuario DROP CONSTRAINT IF EXISTS usuario_permissao_check;
ALTER TABLE grupo_permissao_item DROP CONSTRAINT IF EXISTS grupo_permissao_item_permissao_check;
