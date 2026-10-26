-- ============================================================
-- SCRIPT SIMPLE: BORRAR UNA VENTA ESPECÍFICA POR ID
-- ============================================================
-- Cambia el número 5 por el ID de la venta que quieras borrar
-- ============================================================

USE omnicanal_db;

-- Desactivar verificación de claves foráneas
SET FOREIGN_KEY_CHECKS=0;

-- BORRAR: Reemplaza "5" con el ID de la venta que quieras eliminar
DELETE FROM detalles_ventas WHERE id_venta = 5;
DELETE FROM ventas WHERE id_venta = 5;

-- Reactivar verificación de claves foráneas
SET FOREIGN_KEY_CHECKS=1;

-- Confirmación
SELECT 'Venta eliminada' AS estado;
SELECT COUNT(*) as total_ventas FROM ventas;
