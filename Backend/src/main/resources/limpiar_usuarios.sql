USE omnicanal_db;

-- ============================================================
-- LIMPIAR TODOS LOS USUARIOS (para empezar desde cero)
-- ============================================================
SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM usuarios WHERE id_usuario > 0;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- VERIFICAR QUE LA TABLA ESTÁ VACÍA
-- ============================================================
SELECT COUNT(*) as total_usuarios FROM usuarios;
