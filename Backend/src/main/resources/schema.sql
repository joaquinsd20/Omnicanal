-- Esquema Omnicanal (idempotente). Spring Boot lo ejecuta al arrancar (spring.sql.init).
-- Usa la base de datos de la conexión (no crea ni cambia de base).
-- ========================================
-- TABLA 1: USUARIOS
-- ========================================
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre_usuario VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    telefono VARCHAR(20),
    razon_social VARCHAR(150),
    ruc VARCHAR(11) UNIQUE,
    pais VARCHAR(50) DEFAULT 'Perú',
    ciudad VARCHAR(100),
    direccion TEXT,
    tipo_usuario ENUM('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR') DEFAULT 'EMPRENDEDOR',
    plan_suscripcion ENUM('PLAN_EMPRENDEDOR', 'PLAN_PRO') DEFAULT 'PLAN_EMPRENDEDOR',
    estado ENUM('ACTIVO', 'INACTIVO', 'SUSPENDIDO', 'BLOQUEADO') DEFAULT 'ACTIVO',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    id_usuario_creador BIGINT NULL,
    INDEX idx_email (email),
    INDEX idx_ruc (ruc),
    INDEX idx_tipo_usuario (tipo_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- TABLA 2: CANALES (Ventas Multi-canal)
-- ========================================
CREATE TABLE IF NOT EXISTS canales (
    id_canal BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_usuario BIGINT NOT NULL,
    nombre_canal VARCHAR(100) NOT NULL,
    tipo_canal ENUM('tienda_web', 'whatsapp', 'tiktok_shop', 'instagram', 'fisico', 'otro') NOT NULL,
    url_tienda VARCHAR(500),
    estado ENUM('activo', 'inactivo') DEFAULT 'activo',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    INDEX idx_usuario (id_usuario),
    INDEX idx_tipo_canal (tipo_canal)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- TABLA 3: PRODUCTOS
-- ========================================
CREATE TABLE IF NOT EXISTS productos (
    id_producto BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_usuario BIGINT NOT NULL,
    nombre_producto VARCHAR(150) NOT NULL,
    sku VARCHAR(50) NOT NULL,
    descripcion TEXT,
    categoria VARCHAR(100),
    precio_costo DECIMAL(10,2) NOT NULL,
    precio_venta DECIMAL(10,2) NOT NULL,
    margen_ganancia_pct DECIMAL(5,2),
    stock_total INT NOT NULL DEFAULT 0,
    stock_minimo INT DEFAULT 5,
    peso_kg DECIMAL(8,3),
    volumen_m3 DECIMAL(8,4),
    imagen_url VARCHAR(500),
    proveedor VARCHAR(150),
    pais_origen VARCHAR(50),
    estado ENUM('activo', 'inactivo', 'discontinuado') DEFAULT 'activo',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    INDEX idx_usuario (id_usuario),
    INDEX idx_sku (sku),
    INDEX idx_categoria (categoria),
    INDEX idx_stock (stock_total)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- TABLA 4: IMPORTACIONES (Smart Sourcing)
-- ========================================
CREATE TABLE IF NOT EXISTS importaciones (
    id_importacion BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_usuario BIGINT NOT NULL,
    numero_factura VARCHAR(100) NOT NULL,
    fecha_factura DATE NOT NULL,
    proveedor_internacional VARCHAR(150) NOT NULL,
    pais_origen VARCHAR(50),
    monto_original DECIMAL(12,2) NOT NULL,
    moneda_original VARCHAR(3) DEFAULT 'USD',
    tipo_cambio DECIMAL(8,4),
    monto_en_soles DECIMAL(12,2),
    
    -- Costos desglosados
    costo_flete DECIMAL(12,2) DEFAULT 0,
    costo_aduanas DECIMAL(12,2) DEFAULT 0,
    costo_aranceles DECIMAL(12,2) DEFAULT 0,
    costo_transporte_local DECIMAL(12,2) DEFAULT 0,
    otros_costos DECIMAL(12,2) DEFAULT 0,
    costo_total DECIMAL(12,2),
    
    cantidad_items INT NOT NULL,
    peso_total_kg DECIMAL(10,3),
    volumen_total_m3 DECIMAL(10,4),
    
    -- Documento IA
    documento_ruta VARCHAR(500),
    estado_procesamiento ENUM('pendiente', 'procesado', 'verificado', 'rechazado') DEFAULT 'pendiente',
    
    fecha_llegada_estimada DATE,
    fecha_llegada_real DATE,
    estado_importacion ENUM('en_transito', 'entregado', 'en_aduana', 'cancelado') DEFAULT 'en_transito',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    INDEX idx_usuario (id_usuario),
    INDEX idx_numero_factura (numero_factura),
    INDEX idx_estado (estado_importacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- TABLA 5: PRORRATEO IMPORTACIONES (Detalles por Producto)
-- ========================================
CREATE TABLE IF NOT EXISTS prorrateo_importaciones (
    id_prorrateo BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_importacion BIGINT NOT NULL,
    id_producto BIGINT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario_original DECIMAL(10,2),
    costo_flete_asignado DECIMAL(10,2),
    costo_aduanas_asignado DECIMAL(10,2),
    costo_aranceles_asignado DECIMAL(10,2),
    costo_unitario_final DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_importacion) REFERENCES importaciones(id_importacion) ON DELETE CASCADE,
    FOREIGN KEY (id_producto) REFERENCES productos(id_producto) ON DELETE CASCADE,
    INDEX idx_importacion (id_importacion),
    INDEX idx_producto (id_producto)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- TABLA 6: VENTAS (Transacciones)
-- ========================================
CREATE TABLE IF NOT EXISTS ventas (
    id_venta BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_usuario BIGINT NOT NULL,
    id_canal BIGINT NOT NULL,
    numero_venta VARCHAR(50) NOT NULL UNIQUE,
    fecha_venta TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Cliente
    nombre_cliente VARCHAR(100) NOT NULL,
    email_cliente VARCHAR(100),
    telefono_cliente VARCHAR(20),
    
    -- Envío
    provincia_destino VARCHAR(100),
    distrito_destino VARCHAR(100),
    direccion_entrega TEXT,
    
    -- Montos
    subtotal DECIMAL(12,2) NOT NULL,
    costo_envio DECIMAL(10,2) DEFAULT 0,
    descuento DECIMAL(10,2) DEFAULT 0,
    total DECIMAL(12,2) NOT NULL,
    
    -- Estado
    estado_venta ENUM('pendiente', 'confirmada', 'enviada', 'entregada', 'cancelada') DEFAULT 'pendiente',
    estado_pago ENUM('pendiente', 'pagado', 'reembolso') DEFAULT 'pendiente',
    
    -- Logística
    agencia_envio VARCHAR(100),
    numero_tracking VARCHAR(100),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    FOREIGN KEY (id_canal) REFERENCES canales(id_canal),
    INDEX idx_usuario (id_usuario),
    INDEX idx_canal (id_canal),
    INDEX idx_numero_venta (numero_venta),
    INDEX idx_estado (estado_venta),
    INDEX idx_fecha (fecha_venta)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- TABLA 7: DETALLES VENTAS (Items por venta)
-- ========================================
CREATE TABLE IF NOT EXISTS detalles_ventas (
    id_detalle BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_venta BIGINT NOT NULL,
    id_producto BIGINT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(12,2),
    descuento_unitario DECIMAL(10,2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_venta) REFERENCES ventas(id_venta) ON DELETE CASCADE,
    FOREIGN KEY (id_producto) REFERENCES productos(id_producto),
    INDEX idx_venta (id_venta),
    INDEX idx_producto (id_producto)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- TABLA 8: MOVIMIENTO INVENTARIO (Auditoría)
-- ========================================
CREATE TABLE IF NOT EXISTS movimiento_inventario (
    id_movimiento BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_usuario BIGINT NOT NULL,
    id_producto BIGINT NOT NULL,
    tipo_movimiento ENUM('entrada', 'salida', 'ajuste', 'devolución') NOT NULL,
    cantidad INT NOT NULL,
    referencia VARCHAR(100),
    descripcion TEXT,
    stock_anterior INT,
    stock_posterior INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    FOREIGN KEY (id_producto) REFERENCES productos(id_producto),
    INDEX idx_usuario (id_usuario),
    INDEX idx_producto (id_producto),
    INDEX idx_tipo (tipo_movimiento)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- TABLA 9: ALERTAS Y NOTIFICACIONES
-- ========================================
CREATE TABLE IF NOT EXISTS alertas (
    id_alerta BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_usuario BIGINT NOT NULL,
    id_producto BIGINT,
    tipo_alerta ENUM('stock_bajo', 'quiebre_stock', 'venta_importante', 'retraso_envio', 'flete_disponible', 'alerta_financiera') NOT NULL,
    mensaje TEXT NOT NULL,
    enviado_whatsapp BOOLEAN DEFAULT FALSE,
    fecha_envio TIMESTAMP,
    estado ENUM('no_leida', 'leida') DEFAULT 'no_leida',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    FOREIGN KEY (id_producto) REFERENCES productos(id_producto),
    INDEX idx_usuario (id_usuario),
    INDEX idx_tipo (tipo_alerta),
    INDEX idx_estado (estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- TABLA 10: DASHBOARD FINANCIERO (Resumen diario)
-- ========================================
CREATE TABLE IF NOT EXISTS dashboard_financiero (
    id_dashboard BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_usuario BIGINT NOT NULL,
    fecha_reporte DATE NOT NULL,
    
    -- Ingresos
    total_ingresos DECIMAL(12,2) DEFAULT 0,
    total_ventas INT DEFAULT 0,
    ticket_promedio DECIMAL(10,2) DEFAULT 0,
    
    -- Costos
    total_costos DECIMAL(12,2) DEFAULT 0,
    costo_importacion DECIMAL(12,2) DEFAULT 0,
    costo_envios DECIMAL(12,2) DEFAULT 0,
    
    -- Rentabilidad
    ganancia_neta DECIMAL(12,2) DEFAULT 0,
    margen_neto_pct DECIMAL(5,2) DEFAULT 0,
    
    -- Inventario
    valor_inventario DECIMAL(12,2) DEFAULT 0,
    producto_mas_vendido VARCHAR(150),
    cantidad_productos_vendidos INT DEFAULT 0,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    UNIQUE KEY unique_usuario_fecha (id_usuario, fecha_reporte),
    INDEX idx_usuario (id_usuario),
    INDEX idx_fecha (fecha_reporte)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- TABLA 11: AGENCIAS DE TRANSPORTE
-- ========================================
CREATE TABLE IF NOT EXISTS agencias_transporte (
    id_agencia BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre_agencia VARCHAR(150) NOT NULL,
    codigo_agencia VARCHAR(50) UNIQUE,
    tipo_agencia ENUM('masiva', 'express', 'internacional') DEFAULT 'masiva',
    api_key VARCHAR(255),
    api_url VARCHAR(500),
    estado ENUM('activo', 'inactivo') DEFAULT 'activo',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_nombre (nombre_agencia)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- TABLA 12: TARIFAS ENVÍO POR AGENCIA
-- ========================================
CREATE TABLE IF NOT EXISTS tarifas_envio (
    id_tarifa BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_agencia BIGINT NOT NULL,
    provincia VARCHAR(100) NOT NULL,
    distrito VARCHAR(100),
    precio_base DECIMAL(10,2) NOT NULL,
    precio_por_kg DECIMAL(10,2) DEFAULT 0,
    dias_entrega_estimado INT,
    estado ENUM('activo', 'inactivo') DEFAULT 'activo',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_agencia) REFERENCES agencias_transporte(id_agencia) ON DELETE CASCADE,
    INDEX idx_agencia (id_agencia),
    INDEX idx_provincia (provincia),
    INDEX idx_distrito (distrito)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- TABLA 13: RÓTULOS/ETIQUETAS ENVÍO
-- ========================================
CREATE TABLE IF NOT EXISTS rotulos_envio (
    id_rotulo BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_venta BIGINT NOT NULL,
    id_agencia BIGINT NOT NULL,
    numero_rotulo VARCHAR(100) NOT NULL UNIQUE,
    codigo_qr VARCHAR(500),
    estado ENUM('generado', 'impreso', 'entregado', 'cancelado') DEFAULT 'generado',
    fecha_generacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_impresion TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_venta) REFERENCES ventas(id_venta) ON DELETE CASCADE,
    FOREIGN KEY (id_agencia) REFERENCES agencias_transporte(id_agencia),
    INDEX idx_venta (id_venta),
    INDEX idx_numero (numero_rotulo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================

-- Tabla: configuracion_sunat
-- Almacena datos fiscales del usuario para emisión de comprobantes
CREATE TABLE IF NOT EXISTS configuracion_sunat (
    id_configuracion BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT NOT NULL UNIQUE,
    ruc VARCHAR(11) NOT NULL,
    razon_social VARCHAR(200) NOT NULL,
    nombre_comercial VARCHAR(200),
    domicilio_fiscal VARCHAR(100),
    numero_serie_factura VARCHAR(10) NOT NULL DEFAULT 'F001',
    ultimo_numero_factura INT NOT NULL DEFAULT 0,
    numero_serie_boleta VARCHAR(10) NOT NULL DEFAULT 'B001',
    ultimo_numero_boleta INT NOT NULL DEFAULT 0,
    certificado_digital LONGBLOB,
    password_certificado VARCHAR(100),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    pruebas_mode BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    INDEX idx_usuario (id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: comprobantes_sunat
-- Registro de comprobantes emitidos a SUNAT
CREATE TABLE IF NOT EXISTS comprobantes_sunat (
    id_comprobante BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_venta BIGINT NOT NULL,
    tipo_comprobante VARCHAR(20) NOT NULL,
    serie VARCHAR(10) NOT NULL,
    numero INT NOT NULL,
    numero_comprobante VARCHAR(30) NOT NULL,
    ticket_sunat VARCHAR(50),
    respuesta_sunat LONGTEXT,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    mensaje_error VARCHAR(500),
    xml_generado LONGTEXT,
    xml_firmado LONGTEXT,
    url_pdf VARCHAR(500),
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_envio DATETIME,
    fecha_respuesta DATETIME,
    FOREIGN KEY (id_venta) REFERENCES ventas(id_venta) ON DELETE CASCADE,
    INDEX idx_venta (id_venta),
    INDEX idx_numero (numero_comprobante),
    INDEX idx_estado (estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

