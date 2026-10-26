USE omnicanal_db;

-- ============================================================
-- LIMPIEZA COMPLETA DE TODAS LAS TABLAS (pero mantiene estructura)
-- Orden: De menor a mayor dependencia para evitar errores de FK
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Limpiar detalles_ventas (depende de ventas y productos)
TRUNCATE TABLE detalles_ventas;

-- 2. Limpiar ventas (depende de usuario, canal y productos)
TRUNCATE TABLE ventas;

-- 3. Limpiar movimiento_inventario (kardex - depende de usuario y producto)
TRUNCATE TABLE movimiento_inventario;

-- 4. Limpiar prorrateo_importaciones (depende de importaciones)
TRUNCATE TABLE prorrateo_importaciones;

-- 5. Limpiar importaciones (depende de usuario)
TRUNCATE TABLE importaciones;

-- 6. Limpiar productos (depende de usuario)
TRUNCATE TABLE productos;

-- 7. Limpiar canales (depende de usuario)
TRUNCATE TABLE canales;

-- 8. Limpiar alertas (si depende de usuario)
TRUNCATE TABLE alertas;

-- 9. Finalmente limpiar usuarios
TRUNCATE TABLE usuarios;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- VERIFICAR QUE TODAS LAS TABLAS ESTÁN VACÍAS
-- ============================================================
SELECT 
  'usuarios' as tabla, COUNT(*) as registros FROM usuarios
UNION ALL
SELECT 'productos', COUNT(*) FROM productos
UNION ALL
SELECT 'canales', COUNT(*) FROM canales
UNION ALL
SELECT 'ventas', COUNT(*) FROM ventas
UNION ALL
SELECT 'detalles_ventas', COUNT(*) FROM detalles_ventas
UNION ALL
SELECT 'importaciones', COUNT(*) FROM importaciones
UNION ALL
SELECT 'movimiento_inventario', COUNT(*) FROM movimiento_inventario
ORDER BY tabla;
