USE omnicanal_db;

-- 1) Verifica usuarios e ids. El emprendedor Juan debe ser id_usuario = 2.
SELECT id_usuario, nombre_usuario, email, tipo_usuario, estado
FROM usuarios
ORDER BY id_usuario;

-- 2) Verifica que Juan tenga productos.
SELECT id_producto, id_usuario, nombre_producto, precio_costo, precio_venta, stock_total
FROM productos
WHERE id_usuario = 2;

-- 3) Verifica que Juan tenga ventas.
SELECT id_venta, id_usuario, id_canal, numero_venta, fecha_venta, subtotal, costo_envio, total, estado_venta, estado_pago
FROM ventas
WHERE id_usuario = 2;

-- 4) Verifica detalles de ventas de Juan.
SELECT dv.id_detalle, v.numero_venta, p.nombre_producto, dv.cantidad, dv.precio_unitario, dv.subtotal
FROM detalles_ventas dv
INNER JOIN ventas v ON dv.id_venta = v.id_venta
INNER JOIN productos p ON dv.id_producto = p.id_producto
WHERE v.id_usuario = 2;

-- 5) Verifica importaciones y kardex de Juan.
SELECT id_importacion, id_usuario, numero_factura, costo_total, estado_importacion
FROM importaciones
WHERE id_usuario = 2;

SELECT id_movimiento, id_usuario, id_producto, tipo_movimiento, cantidad, referencia, created_at
FROM movimiento_inventario
WHERE id_usuario = 2;
