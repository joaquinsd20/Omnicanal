USE omnicanal_db;

-- Corrige la definición de ENUM de usuarios para Spring/JPA.
ALTER TABLE usuarios MODIFY tipo_usuario ENUM('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR') DEFAULT 'EMPRENDEDOR';
ALTER TABLE usuarios MODIFY plan_suscripcion ENUM('PLAN_EMPRENDEDOR', 'PLAN_PRO') DEFAULT 'PLAN_EMPRENDEDOR';
ALTER TABLE usuarios MODIFY estado ENUM('ACTIVO', 'INACTIVO', 'SUSPENDIDO', 'BLOQUEADO') DEFAULT 'ACTIVO';

UPDATE usuarios SET tipo_usuario = UPPER(tipo_usuario);
UPDATE usuarios SET plan_suscripcion = UPPER(plan_suscripcion);
UPDATE usuarios SET estado = UPPER(estado);

SELECT id_usuario, email, tipo_usuario, estado, plan_suscripcion FROM usuarios;
