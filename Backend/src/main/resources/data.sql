-- Datos demo de Omnicanal. INSERT IGNORE: solo inserta lo que falta, nunca duplica.
-- Se ejecuta al arrancar cuando SPRING_SQL_INIT_MODE=always.

-- ============================================================
-- USUARIOS DE PRUEBA
-- ============================================================
-- Admin global: admin@omnicanal.pe (contraseña privada, no se publica)
-- Cuenta demo:  juan@omnicanal.pe  / Demo1234

INSERT IGNORE INTO usuarios (
  id_usuario, nombre_usuario, email, password, telefono, razon_social, ruc,
  pais, ciudad, direccion, tipo_usuario, plan_suscripcion, estado,
  id_usuario_creador, created_at, updated_at
) VALUES
(1, 'Admin Global', 'admin@omnicanal.pe', '$2a$10$29ko8fQRb2JJ.r2PjbuUdOS9gyKyd3qwuH/cH9DL86KmcU0sCJs.i', '999888777', 'Omnicanal Admin SAC', '20000000001', 'Perú', 'Lima', 'Jr. Admin 123', 'ADMINISTRADOR_GLOBAL', 'PLAN_PRO', 'ACTIVO', NULL, NOW(), NOW()),
(2, 'Juan Comerciante', 'juan@omnicanal.pe', '$2a$10$EMAw.lCbRtEruALs5wq3NOsT96MHV4NPxmwLgLlIs1QXInbvyFg.a', '999111222', 'JC Importaciones SAC', '20123456789', 'Perú', 'Lima', 'Av. Comercio 456', 'EMPRENDEDOR', 'PLAN_EMPRENDEDOR', 'ACTIVO', 1, NOW(), NOW());

-- ============================================================
-- CANALES
-- ============================================================
INSERT IGNORE INTO canales (id_canal, id_usuario, nombre_canal, tipo_canal, url_tienda, estado, created_at, updated_at) VALUES
(1, 1, 'Tienda Web Admin', 'tienda_web', 'https://admin.demo', 'activo', NOW(), NOW()),
(2, 1, 'WhatsApp Admin', 'whatsapp', NULL, 'activo', NOW(), NOW()),
(3, 2, 'Tienda Web Principal', 'tienda_web', 'https://juan.demo', 'activo', NOW(), NOW()),
(4, 2, 'WhatsApp Business', 'whatsapp', NULL, 'activo', NOW(), NOW()),
(5, 2, 'TikTok Shop', 'tiktok_shop', NULL, 'activo', NOW(), NOW());

-- ============================================================
-- PRODUCTOS
-- ============================================================
INSERT IGNORE INTO productos (
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
INSERT IGNORE INTO ventas (
  id_venta, id_usuario, id_canal, numero_venta, fecha_venta,
  nombre_cliente, email_cliente, telefono_cliente, provincia_destino,
  distrito_destino, direccion_entrega, subtotal, costo_envio, descuento,
  total, estado_venta, estado_pago, agencia_envio, numero_tracking,
  created_at, updated_at
) VALUES
(1, 1, 1, 'V-ADMIN-001', NOW(), 'Cliente Admin Demo', 'cliente.admin@test.com', '999000111', 'Lima', 'Lima', 'Av. Demo 123', 300.00, 15.00, 0.00, 315.00, 'entregada', 'pagado', 'Shalom', 'SHALOM-ADM-001', NOW(), NOW()),
(2, 2, 3, 'V-JUAN-001', NOW(), 'María García', 'maria@test.com', '999222333', 'Lima', 'Miraflores', 'Calle Real 123', 600.00, 18.00, 0.00, 618.00, 'entregada', 'pagado', 'Olva Courier', 'OLVA-JUAN-001', NOW(), NOW()),
(3, 2, 4, 'V-JUAN-002', NOW(), 'Carlos López', 'carlos@test.com', '999333444', 'La Libertad', 'Trujillo', 'Av. España 100', 140.00, 12.00, 0.00, 152.00, 'confirmada', 'pagado', 'Shalom', 'SHALOM-JUAN-002', NOW(), NOW());

INSERT IGNORE INTO detalles_ventas (id_detalle, id_venta, id_producto, cantidad, precio_unitario, subtotal, descuento_unitario, created_at) VALUES
(1, 1, 1, 1, 300.00, 300.00, 0.00, NOW()),
(2, 2, 3, 2, 300.00, 600.00, 0.00, NOW()),
(3, 3, 4, 2, 70.00, 140.00, 0.00, NOW());

INSERT IGNORE INTO movimiento_inventario (
  id_movimiento, id_usuario, id_producto, tipo_movimiento, cantidad,
  referencia, descripcion, stock_anterior, stock_posterior, created_at
) VALUES
(1, 1, 1, 'salida', 1, 'VENTA-V-ADMIN-001', 'Venta de prueba admin', 21, 20, NOW()),
(2, 2, 3, 'salida', 2, 'VENTA-V-JUAN-001', 'Venta a María García', 27, 25, NOW()),
(3, 2, 4, 'salida', 2, 'VENTA-V-JUAN-002', 'Venta a Carlos López', 52, 50, NOW());

-- ============================================================
-- IMPORTACIÓN + PRORRATEO
-- ============================================================
INSERT IGNORE INTO importaciones (
  id_importacion, id_usuario, numero_factura, fecha_factura, proveedor_internacional,
  pais_origen, monto_original, moneda_original, tipo_cambio, monto_en_soles,
  costo_flete, costo_aduanas, costo_aranceles, costo_transporte_local,
  otros_costos, costo_total, cantidad_items, peso_total_kg, volumen_total_m3,
  estado_procesamiento, fecha_llegada_estimada, estado_importacion,
  created_at, updated_at
) VALUES
(1, 2, 'INV-2026-001', CURDATE(), 'AliExpress / Shenzhen', 'China', 500.00, 'USD', 3.85, 1925.00, 120.00, 80.00, 60.00, 40.00, 20.00, 320.00, 20, 8.500, 0.1000, 'procesado', DATE_ADD(CURDATE(), INTERVAL 15 DAY), 'en_transito', NOW(), NOW());

INSERT IGNORE INTO prorrateo_importaciones (
  id_prorrateo, id_importacion, id_producto, cantidad, precio_unitario_original,
  costo_flete_asignado, costo_aduanas_asignado, costo_aranceles_asignado,
  costo_unitario_final, created_at
) VALUES
(1, 1, 3, 10, 150.00, 60.00, 40.00, 30.00, 163.00, NOW()),
(2, 1, 4, 10, 25.00, 60.00, 40.00, 30.00, 38.00, NOW());

-- ============================================================
-- AGENCIAS DE TRANSPORTE
-- ============================================================
INSERT IGNORE INTO agencias_transporte (id_agencia, nombre_agencia, codigo_agencia, tipo_agencia, estado, created_at) VALUES
(1, 'Shalom', 'SHALOM', 'masiva', 'activo', NOW()),
(2, 'Olva Courier', 'OLVA', 'masiva', 'activo', NOW()),
(3, 'Marvisur', 'MARVISUR', 'masiva', 'activo', NOW());

