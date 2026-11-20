USE omnicanal_db;

-- ============================================================
-- CAMBIAR DEFINICIÓN DEL ENUM EN LA TABLA USUARIOS
-- De minúsculas a MAYÚSCULAS
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

-- Cambiar el tipo de la columna estado de enum minúsculas a MAYÚSCULAS
ALTER TABLE usuarios MODIFY estado ENUM('ACTIVO', 'INACTIVO', 'SUSPENDIDO', 'BLOQUEADO') DEFAULT 'ACTIVO';

-- Ahora sí actualizar el valor
UPDATE usuarios SET estado = 'ACTIVO' WHERE estado = 'activo';

-- Verificar
SELECT id_usuario, nombre_usuario, email, estado, tipo_usuario FROM usuarios;

SET FOREIGN_KEY_CHECKS = 1;
