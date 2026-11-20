-- Limpiar completamente la base de datos
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE movimiento_inventario;
TRUNCATE TABLE prorrateo_importaciones;
TRUNCATE TABLE detalles_ventas;
TRUNCATE TABLE kardex;
TRUNCATE TABLE ventas;
TRUNCATE TABLE importaciones;
TRUNCATE TABLE productos;
TRUNCATE TABLE canales;
TRUNCATE TABLE usuarios;

SET FOREIGN_KEY_CHECKS = 1;

-- Verificar que las tablas estén vacías
SELECT 'usuarios' AS tabla, COUNT(*) AS registros FROM usuarios
UNION ALL SELECT 'productos', COUNT(*) FROM productos
UNION ALL SELECT 'canales', COUNT(*) FROM canales
UNION ALL SELECT 'importaciones', COUNT(*) FROM importaciones
UNION ALL SELECT 'ventas', COUNT(*) FROM ventas
UNION ALL SELECT 'kardex', COUNT(*) FROM kardex
UNION ALL SELECT 'prorrateo_importaciones', COUNT(*) FROM prorrateo_importaciones
UNION ALL SELECT 'movimiento_inventario', COUNT(*) FROM movimiento_inventario
UNION ALL SELECT 'detalles_ventas', COUNT(*) FROM detalles_ventas;
