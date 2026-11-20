-- ============================================================
-- ACTUALIZACIÓN DE CONTRASEÑAS A BCRYPT
-- ============================================================
-- El script omnicanal_db.sql sembró el usuario de prueba con la
-- contraseña en texto plano 'hashed_password_123', que NO funciona
-- con el login JWT (Spring usa BCryptPasswordEncoder).
--
-- Ejecuta este script UNA vez sobre la BD omnicanal_db para poder
-- iniciar sesión con el usuario de prueba.
--
--   Email:      juan@omnicanal.pe
--   Contraseña: password123
-- ============================================================

USE omnicanal_db;

UPDATE usuarios
SET password = '$2b$10$ye0cgnFsbdymAMkTOXxx1OILECl0AIi1astwXl68EGR7SJkaBY2h6'
WHERE email = 'juan@omnicanal.pe';

-- Cualquier usuario nuevo creado por POST /api/auth/register
-- ya se guarda automáticamente con su hash BCrypt.
