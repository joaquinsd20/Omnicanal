-- ============================================================
-- TABLAS PARA INTEGRACIÓN SUNAT
-- Ejecutar después de que JPA cree las tablas automáticamente
-- ============================================================

USE omnicanal_db;

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

-- Insertar datos iniciales de ejemplo
-- INSERT INTO configuracion_sunat (id_usuario, ruc, razon_social) VALUES (1, '20123456789', 'Mi Empresa Peruana SAC');

-- Verificar tablas creadas
SHOW TABLES LIKE '%sunat%';
