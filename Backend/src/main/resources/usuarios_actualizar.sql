USE omnicanal_db;

-- Usuarios de prueba compatibles con los enums Java.
-- admin@omnicanal.pe / 12345678
-- juan@omnicanal.pe  / password123

ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS id_usuario_creador BIGINT NULL;
ALTER TABLE usuarios MODIFY tipo_usuario ENUM('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR') DEFAULT 'EMPRENDEDOR';
ALTER TABLE usuarios MODIFY plan_suscripcion ENUM('PLAN_EMPRENDEDOR', 'PLAN_PRO') DEFAULT 'PLAN_EMPRENDEDOR';
ALTER TABLE usuarios MODIFY estado ENUM('ACTIVO', 'INACTIVO', 'SUSPENDIDO', 'BLOQUEADO') DEFAULT 'ACTIVO';

DELETE FROM usuarios WHERE email IN ('admin@omnicanal.pe', 'juan@omnicanal.pe');

INSERT INTO usuarios (nombre_usuario, email, password, razon_social, ruc, ciudad, tipo_usuario, plan_suscripcion, estado, id_usuario_creador, created_at, updated_at)
VALUES
('Admin Global', 'admin@omnicanal.pe', '$2a$10$slYQmyNdGzin7olVN3p5Be4DZwIvI6eX7YZLx4CRq8I3rK3W2KYOK', 'Omnicanal Admin SAC', '20000000001', 'Lima', 'ADMINISTRADOR_GLOBAL', 'PLAN_PRO', 'ACTIVO', NULL, NOW(), NOW()),
('Juan Comerciante', 'juan@omnicanal.pe', '$2b$10$ye0cgnFsbdymAMkTOXxx1OILECl0AIi1astwXl68EGR7SJkaBY2h6', 'JC Importaciones SAC', '20123456789', 'Lima', 'EMPRENDEDOR', 'PLAN_EMPRENDEDOR', 'ACTIVO', 1, NOW(), NOW());

SELECT id_usuario, nombre_usuario, email, tipo_usuario, estado FROM usuarios ORDER BY id_usuario;
