USE omnicanal_db;

-- ============================================================
-- RESET DEMO ACTUALIZADO - SISTEMA OMNICANAL
-- Limpia datos, conserva estructura y vuelve a insertar usuarios,
-- productos, canales, ventas, kardex e importaciones de prueba.
-- Compatible con los enums Java en MAYÚSCULAS de Usuario.
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

-- Truncar tablas si existen sin romper el script.
DELIMITER //
DROP PROCEDURE IF EXISTS truncar_si_existe//
CREATE PROCEDURE truncar_si_existe(IN nombre_tabla VARCHAR(80))
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = nombre_tabla
  ) THEN
    SET @sql_stmt = CONCAT('TRUNCATE TABLE ', nombre_tabla);
    PREPARE stmt FROM @sql_stmt;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//
DELIMITER ;

CALL truncar_si_existe('comprobantes_sunat');
CALL truncar_si_existe('configuracion_sunat');
CALL truncar_si_existe('rotulos_envio');
CALL truncar_si_existe('detalles_ventas');
CALL truncar_si_existe('ventas');
CALL truncar_si_existe('movimiento_inventario');
CALL truncar_si_existe('prorrateo_importaciones');
CALL truncar_si_existe('importaciones');
CALL truncar_si_existe('productos');
CALL truncar_si_existe('canales');
CALL truncar_si_existe('alertas');
CALL truncar_si_existe('dashboard_financiero');
CALL truncar_si_existe('tarifas_envio');
CALL truncar_si_existe('agencias_transporte');
CALL truncar_si_existe('usuarios');

DROP PROCEDURE IF EXISTS truncar_si_existe;

SET FOREIGN_KEY_CHECKS = 1;

-- Ajustar columnas de usuarios para que coincidan con los enums Java.
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS id_usuario_creador BIGINT NULL;
ALTER TABLE usuarios MODIFY tipo_usuario ENUM('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR') DEFAULT 'EMPRENDEDOR';
ALTER TABLE usuarios MODIFY plan_suscripcion ENUM('PLAN_EMPRENDEDOR', 'PLAN_PRO') DEFAULT 'PLAN_EMPRENDEDOR';
ALTER TABLE usuarios MODIFY estado ENUM('ACTIVO', 'INACTIVO', 'SUSPENDIDO', 'BLOQUEADO') DEFAULT 'ACTIVO';

-- ============================================================
-- USUARIOS DE PRUEBA
-- ============================================================
-- Admin global: admin@omnicanal.pe / 12345678
-- Emprendedor:  juan@omnicanal.pe  / password123

INSERT INTO usuarios (
  id_usuario, nombre_usuario, email, password, telefono, razon_social, ruc,
  pais, ciudad, direccion, tipo_usuario, plan_suscripcion, estado,
  id_usuario_creador, created_at, updated_at
) VALUES
(1, 'Admin Global', 'admin@omnicanal.pe', '$2a$10$slYQmyNdGzin7olVN3p5Be4DZwIvI6eX7YZLx4CRq8I3rK3W2KYOK', '999888777', 'Omnicanal Admin SAC', '20000000001', 'Perú', 'Lima', 'Jr. Admin 123', 'ADMINISTRADOR_GLOBAL', 'PLAN_PRO', 'ACTIVO', NULL, NOW(), NOW()),
(2, 'Juan Comerciante', 'juan@omnicanal.pe', '$2b$10$ye0cgnFsbdymAMkTOXxx1OILECl0AIi1astwXl68EGR7SJkaBY2h6', '999111222', 'JC Importaciones SAC', '20123456789', 'Perú', 'Lima', 'Av. Comercio 456', 'EMPRENDEDOR', 'PLAN_EMPRENDEDOR', 'ACTIVO', 1, NOW(), NOW());

-- ============================================================
-- CANALES
-- ============================================================
INSERT INTO canales (id_canal, id_usuario, nombre_canal, tipo_canal, url_tienda, estado, created_at, updated_at) VALUES
(1, 1, 'Tienda Web Admin', 'tienda_web', 'https://admin.demo', 'activo', NOW(), NOW()),
(2, 1, 'WhatsApp Admin', 'whatsapp', NULL, 'activo', NOW(), NOW()),
(3, 2, 'Tienda Web Principal', 'tienda_web', 'https://juan.demo', 'activo', NOW(), NOW()),
(4, 2, 'WhatsApp Business', 'whatsapp', NULL, 'activo', NOW(), NOW()),
(5, 2, 'TikTok Shop', 'tiktok_shop', NULL, 'activo', NOW(), NOW());

-- ============================================================
-- PRODUCTOS
-- ============================================================
INSERT INTO productos (
  id_producto, id_usuario, nombre_producto, sku, descripcion, categoria,
  precio_costo, precio_venta, margen_ganancia_pct, stock_total, stock_minimo,
  peso_kg, volumen_m3, proveedor, pais_origen, estado, created_at, updated_at
) VALUES
(1, 1, 'Smartphone X200 Admin', 'ADM-PHONE-001', 'Teléfono inteligente 6.5 pulgadas', 'Tecnología', 150.00, 300.00, 100.00, 20, 5, 0.200, 0.0010, 'Proveedor China ABC', 'China', 'activo', NOW(), NOW()),
(2, 1, 'Auriculares Admin', 'ADM-AUDIO-001', 'Auriculares Bluetooth premium', 'Tecnología', 25.00, 70.00, 180.00, 8, 10, 0.080, 0.0005, 'Proveedor Shenzhen', 'China', 'activo', NOW(), NOW()),
(3, 2, 'Smartphone X200', 'SKU-PHONE-001', 'Teléfono inteligente 6.5 pulgadas', 'Tecnología', 150.00, 300.00, 100.00, 25, 5, 0.200, 0.0010, 'Proveedor China ABC', 'China', 'activo', NOW(), NOW()),
(4, 2, 'Auriculares Inalámbricos', 'SKU-AUDIO-001', 'Auriculares Bluetooth premium', 'Tecnología', 25.00, 70.00, 180.00, 50, 10, 0.080, 0.0005, 'Proveedor Shenzhen', 'China', 'activo', NOW(), NOW()),
(5, 2, 'Mochila Impermeable', 'SKU-BAG-001', 'Mochila de viaje 40L', 'Accesorios', 20.00, 60.00, 200.00, 30, 5, 0.600, 0.0100, 'Proveedor Textil', 'Vietnam', 'activo', NOW(), NOW());

-- ============================================================
-- VENTAS + DETALLES + KARDEX
-- ============================================================
INSERT INTO ventas (
  id_venta, id_usuario, id_canal, numero_venta, fecha_venta,
  nombre_cliente, email_cliente, telefono_cliente, provincia_destino,
  distrito_destino, direccion_entrega, subtotal, costo_envio, descuento,
  total, estado_venta, estado_pago, agencia_envio, numero_tracking,
  created_at, updated_at
) VALUES
(1, 1, 1, 'V-ADMIN-001', NOW(), 'Cliente Admin Demo', 'cliente.admin@test.com', '999000111', 'Lima', 'Lima', 'Av. Demo 123', 300.00, 15.00, 0.00, 315.00, 'entregada', 'pagado', 'Shalom', 'SHALOM-ADM-001', NOW(), NOW()),
(2, 2, 3, 'V-JUAN-001', NOW(), 'María García', 'maria@test.com', '999222333', 'Lima', 'Miraflores', 'Calle Real 123', 600.00, 18.00, 0.00, 618.00, 'entregada', 'pagado', 'Olva Courier', 'OLVA-JUAN-001', NOW(), NOW()),
(3, 2, 4, 'V-JUAN-002', NOW(), 'Carlos López', 'carlos@test.com', '999333444', 'La Libertad', 'Trujillo', 'Av. España 100', 140.00, 12.00, 0.00, 152.00, 'confirmada', 'pagado', 'Shalom', 'SHALOM-JUAN-002', NOW(), NOW());

INSERT INTO detalles_ventas (id_detalle, id_venta, id_producto, cantidad, precio_unitario, subtotal, descuento_unitario, created_at) VALUES
(1, 1, 1, 1, 300.00, 300.00, 0.00, NOW()),
(2, 2, 3, 2, 300.00, 600.00, 0.00, NOW()),
(3, 3, 4, 2, 70.00, 140.00, 0.00, NOW());

INSERT INTO movimiento_inventario (
  id_movimiento, id_usuario, id_producto, tipo_movimiento, cantidad,
  referencia, descripcion, stock_anterior, stock_posterior, created_at
) VALUES
(1, 1, 1, 'salida', 1, 'VENTA-V-ADMIN-001', 'Venta de prueba admin', 21, 20, NOW()),
(2, 2, 3, 'salida', 2, 'VENTA-V-JUAN-001', 'Venta a María García', 27, 25, NOW()),
(3, 2, 4, 'salida', 2, 'VENTA-V-JUAN-002', 'Venta a Carlos López', 52, 50, NOW());

-- ============================================================
-- IMPORTACIÓN + PRORRATEO
-- ============================================================
INSERT INTO importaciones (
  id_importacion, id_usuario, numero_factura, fecha_factura, proveedor_internacional,
  pais_origen, monto_original, moneda_original, tipo_cambio, monto_en_soles,
  costo_flete, costo_aduanas, costo_aranceles, costo_transporte_local,
  otros_costos, costo_total, cantidad_items, peso_total_kg, volumen_total_m3,
  estado_procesamiento, fecha_llegada_estimada, estado_importacion,
  created_at, updated_at
) VALUES
(1, 2, 'INV-2026-001', CURDATE(), 'AliExpress / Shenzhen', 'China', 500.00, 'USD', 3.85, 1925.00, 120.00, 80.00, 60.00, 40.00, 20.00, 320.00, 20, 8.500, 0.1000, 'procesado', DATE_ADD(CURDATE(), INTERVAL 15 DAY), 'en_transito', NOW(), NOW());

INSERT INTO prorrateo_importaciones (
  id_prorrateo, id_importacion, id_producto, cantidad, precio_unitario_original,
  costo_flete_asignado, costo_aduanas_asignado, costo_aranceles_asignado,
  costo_unitario_final, created_at
) VALUES
(1, 1, 3, 10, 150.00, 60.00, 40.00, 30.00, 163.00, NOW()),
(2, 1, 4, 10, 25.00, 60.00, 40.00, 30.00, 38.00, NOW());

-- ============================================================
-- AGENCIAS DE TRANSPORTE
-- ============================================================
INSERT INTO agencias_transporte (id_agencia, nombre_agencia, codigo_agencia, tipo_agencia, estado, created_at) VALUES
(1, 'Shalom', 'SHALOM', 'masiva', 'activo', NOW()),
(2, 'Olva Courier', 'OLVA', 'masiva', 'activo', NOW()),
(3, 'Marvisur', 'MARVISUR', 'masiva', 'activo', NOW());

-- Verificación final.
SELECT 'usuarios' AS tabla, COUNT(*) AS registros FROM usuarios
UNION ALL SELECT 'productos', COUNT(*) FROM productos
UNION ALL SELECT 'canales', COUNT(*) FROM canales
UNION ALL SELECT 'ventas', COUNT(*) FROM ventas
UNION ALL SELECT 'detalles_ventas', COUNT(*) FROM detalles_ventas
UNION ALL SELECT 'importaciones', COUNT(*) FROM importaciones
UNION ALL SELECT 'movimiento_inventario', COUNT(*) FROM movimiento_inventario;

SELECT id_usuario, email, tipo_usuario, estado, plan_suscripcion FROM usuarios ORDER BY id_usuario;
