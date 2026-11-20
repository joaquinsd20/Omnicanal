USE omnicanal_db;

-- Corrige el usuario Juan para que coincida con los enums Java.
-- Login: juan@omnicanal.pe / password123
UPDATE usuarios
SET password = '$2b$10$ye0cgnFsbdymAMkTOXxx1OILECl0AIi1astwXl68EGR7SJkaBY2h6',
    tipo_usuario = 'EMPRENDEDOR',
    plan_suscripcion = 'PLAN_EMPRENDEDOR',
    estado = 'ACTIVO'
WHERE email = 'juan@omnicanal.pe';

SELECT id_usuario, email, tipo_usuario, estado, plan_suscripcion FROM usuarios WHERE email = 'juan@omnicanal.pe';
