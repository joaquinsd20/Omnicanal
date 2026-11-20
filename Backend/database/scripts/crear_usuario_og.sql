USE omnicanal_db;

-- ============================================================
-- CREAR USUARIO ADMINISTRADOR_GLOBAL (OG)
-- Email: ogadmin@gmail.com
-- Contraseña: og123456 (hasheada con BCrypt)
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Limpiar tabla usuarios
DELETE FROM usuarios WHERE id_usuario > 0;

-- 2. Insertar usuario OG con valores en MAYÚSCULAS
INSERT INTO usuarios (
    nombre_usuario,
    email,
    password,
    telefono,
    ruc,
    razon_social,
    ciudad,
    direccion,
    tipo_usuario,
    plan_suscripcion,
    estado,
    created_at,
    updated_at
) VALUES (
    'ogadmin',
    'ogadmin@gmail.com',
    '$2b$10$vcbTIpKSxL4SiJpBV5/6fudilmspKlmWCuoWUzepMGqdb.Q3fZ9Du',  -- og123456 hasheado
    '999888777',
    '20123456789',
    'Omnicanal Admin',
    'Lima',
    'Jr. Admin 123',
    'ADMINISTRADOR_GLOBAL',
    'PLAN_EMPRENDEDOR',
    'ACTIVO',
    NOW(),
    NOW()
);

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- VERIFICAR CREACIÓN
-- ============================================================
SELECT 
    id_usuario,
    nombre_usuario,
    email,
    tipo_usuario,
    estado,
    plan_suscripcion,
    created_at
FROM usuarios;
