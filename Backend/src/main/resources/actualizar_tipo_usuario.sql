USE omnicanal_db;

-- Corrección de tipo_usuario para que coincida con los enums Java.
ALTER TABLE usuarios MODIFY tipo_usuario ENUM('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR') DEFAULT 'EMPRENDEDOR';
UPDATE usuarios SET tipo_usuario = 'EMPRENDEDOR' WHERE email = 'juan@omnicanal.pe';
SELECT id_usuario, email, tipo_usuario FROM usuarios;
