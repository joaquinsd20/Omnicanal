USE omnicanal_db;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE detalles_ventas;
TRUNCATE TABLE ventas;
TRUNCATE TABLE movimiento_inventario;
TRUNCATE TABLE prorrateo_importaciones;
TRUNCATE TABLE importaciones;
TRUNCATE TABLE productos;
TRUNCATE TABLE canales;
TRUNCATE TABLE alertas;
TRUNCATE TABLE usuarios;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO usuarios (
  id_usuario, nombre_usuario, email, password, telefono, razon_social, ruc, pais, ciudad, direccion,
  tipo_usuario, plan_suscripcion, estado, id_usuario_creador, created_at, updated_at
) VALUES
(1, 'Admin Global', 'admin@omnicanal.pe', '$2a$10$slYQmyNdGzin7olVN3p5Be4DZwIvI6eX7YZLx4CRq8I3rK3W2KYOK', '999888777', 'Omnicanal Admin SAC', '20000000001', 'Perú', 'Lima', 'Jr. Admin 123', 'ADMINISTRADOR_GLOBAL', 'PLAN_PRO', 'ACTIVO', NULL, NOW(), NOW()),
(2, 'Juan Comerciante', 'juan@omnicanal.pe', '$2b$10$ye0cgnFsbdymAMkTOXxx1OILECl0AIi1astwXl68EGR7SJkaBY2h6', '999111222', 'JC Importaciones SAC', '20123456789', 'Perú', 'Lima', 'Av. Comercio 456', 'EMPRENDEDOR', 'PLAN_EMPRENDEDOR', 'ACTIVO', 1, NOW(), NOW());

INSERT INTO canales (id_canal, id_usuario, nombre_canal, tipo_canal, url_tienda, estado) VALUES
(1, 2, 'Tienda Web', 'tienda_web', 'https://omnicanal.local', 'activo'),
(2, 2, 'WhatsApp Ventas', 'whatsapp', NULL, 'activo'),
(3, 2, 'Tienda Física', 'fisico', NULL, 'activo');

INSERT INTO productos (
 id_producto, id_usuario, nombre_producto, sku, descripcion, categoria, precio_costo, precio_venta,
 margen_ganancia_pct, stock_total, stock_minimo, peso_kg, volumen_m3, imagen_url, proveedor, pais_origen, estado
) VALUES
(1, 2, 'Smartphone X200', 'SKU-PHONE-001', 'Equipo importado gama media', 'Tecnología', 650.00, 899.00, 38.31, 25, 5, 0.350, 0.0030, NULL, 'Proveedor China', 'China', 'activo'),
(2, 2, 'Audiculares Inalámbricos', 'SKU-AUDIO-001', 'Audífonos bluetooth', 'Tecnología', 45.00, 89.00, 97.78, 40, 8, 0.120, 0.0010, NULL, 'Proveedor China', 'China', 'activo'),
(3, 2, 'Mochila Impermeable', 'SKU-BAG-001', 'Mochila urbana resistente al agua', 'Accesorios', 55.00, 120.00, 118.18, 12, 5, 0.500, 0.0100, NULL, 'Proveedor Local', 'Perú', 'activo');

INSERT INTO ventas (
 id_venta, id_usuario, id_canal, numero_venta, nombre_cliente, email_cliente, telefono_cliente,
 provincia_destino, distrito_destino, direccion_entrega, subtotal, costo_envio, descuento, total,
 estado_venta, estado_pago, agencia_envio, numero_tracking, fecha_venta
) VALUES
(1, 2, 1, 'V-DEMO-001', 'Cliente Demo 1', 'cliente1@test.com', '999000111', 'Lima', 'Miraflores', 'Av. Demo 111', 899.00, 15.00, 0.00, 914.00, 'entregada', 'pagado', 'Shalom', 'SHALOM-DEMO-001', NOW()),
(2, 2, 2, 'V-DEMO-002', 'Cliente Demo 2', 'cliente2@test.com', '999000222', 'Lima', 'San Miguel', 'Calle Demo 222', 178.00, 10.00, 0.00, 188.00, 'confirmada', 'pagado', 'Olva Courier', 'OLVA-DEMO-002', NOW());

INSERT INTO detalles_ventas (id_detalle, id_venta, id_producto, cantidad, precio_unitario, subtotal, descuento_unitario) VALUES
(1, 1, 1, 1, 899.00, 899.00, 0.00),
(2, 2, 2, 2, 89.00, 178.00, 0.00);

INSERT INTO movimiento_inventario (id_usuario, id_producto, tipo_movimiento, cantidad, referencia, descripcion, stock_anterior, stock_posterior) VALUES
(2, 1, 'salida', 1, 'VENTA-V-DEMO-001', 'Venta demo a Cliente Demo 1', 26, 25),
(2, 2, 'salida', 2, 'VENTA-V-DEMO-002', 'Venta demo a Cliente Demo 2', 42, 40);

INSERT INTO importaciones (
 id_importacion, id_usuario, numero_factura, fecha_factura, proveedor_internacional, pais_origen,
 monto_original, moneda_original, tipo_cambio, monto_en_soles, costo_flete, costo_aduanas,
 costo_aranceles, costo_transporte_local, otros_costos, costo_total, cantidad_items,
 peso_total_kg, volumen_total_m3, estado_procesamiento, fecha_llegada_estimada, estado_importacion
) VALUES
(1, 2, 'INV-DEMO-001', CURDATE(), 'Shenzhen Demo Trading', 'China', 1000.00, 'USD', 3.80, 3800.00, 300.00, 150.00, 120.00, 80.00, 50.00, 4500.00, 2, 12.5, 0.25, 'procesado', DATE_ADD(CURDATE(), INTERVAL 15 DAY), 'en_transito');

INSERT INTO prorrateo_importaciones (id_importacion, id_producto, cantidad, precio_unitario_original, costo_flete_asignado, costo_aduanas_asignado, costo_aranceles_asignado, costo_unitario_final) VALUES
(1, 1, 10, 120.00, 150.00, 75.00, 60.00, 650.00),
(1, 2, 30, 15.00, 150.00, 75.00, 60.00, 45.00);

SELECT 'OK: datos estables cargados' AS resultado;
SELECT id_usuario, nombre_usuario, email, tipo_usuario, estado FROM usuarios;
SELECT 'productos' AS tabla, COUNT(*) AS registros FROM productos
UNION ALL SELECT 'canales', COUNT(*) FROM canales
UNION ALL SELECT 'ventas', COUNT(*) FROM ventas
UNION ALL SELECT 'detalles_ventas', COUNT(*) FROM detalles_ventas
UNION ALL SELECT 'kardex', COUNT(*) FROM movimiento_inventario;
