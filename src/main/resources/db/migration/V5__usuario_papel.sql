-- A coluna guarda ADMIN/USER, que é papel. Permissão é o que vem do grupo.
ALTER TABLE usuario RENAME COLUMN permissao TO papel;
