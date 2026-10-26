# 📊 DIAGRAMA ENTIDAD-RELACIÓN (DER) - OMNICANAL

## Modelo de Datos Completo

```
┌─────────────────────────────────────────────────────────────────┐
│                    DIAGRAMA ENTIDAD-RELACIÓN                     │
│                    Sistema Omnicanal SaaS                         │
└─────────────────────────────────────────────────────────────────┘

                              USUARIOS
                          ┌───────────────┐
                          │ id_usuario(PK)│◄─────────────┐
                          │ email(UNIQUE) │              │
                          │ password      │              │ 1:N
                          │ plan_suscr.   │              │
                          │ estado        │              │
                          └───────────────┘              │
                                 │                       │
                    ┌────────────┼────────────┐          │
                    │            │            │          │
                    │ 1:N        │ 1:N       │ 1:N      │
                    │            │            │          │
              ┌──────────────┐ ┌─────────────┐ ┌──────────────┐
              │  CANALES     │ │ PRODUCTOS   │ │ IMPORTACIONES│
              ├──────────────┤ ├─────────────┤ ├──────────────┤
              │id_canal(PK)  │ │id_prod(PK)  │ │id_import(PK) │
              │id_usuario(FK)│ │id_usuario(FK) │id_usuario(FK)│
              │tipo_canal    │ │sku(UNIQUE)  │ │num_factura   │
              │url_tienda    │ │precio_costo │ │fecha         │
              └──────────────┘ │precio_venta │ │costo_total   │
                    │          │stock_total  │ │estado        │
                    │          │margen       │ └──────────────┘
                    │ 1:N       │peso_kg      │        │
                    │           │proveedor    │        │ 1:N
                    │           └─────────────┘        │
                    │                 ▲                 │
                    │                 │                 │
                    │ 1:N             │ 1:N             │
                    │                 │                 │
              ┌──────────────┐ ┌──────────────────┐    │
              │   VENTAS     │ │ PRORRATEO        │    │
              ├──────────────┤ │ IMPORTACIONES    │    │
              │id_venta(PK)  │ ├──────────────────┤    │
              │id_usuario(FK)│ │id_prorrateo(PK)  │    │
              │id_canal(FK)  │ │id_importacion(FK)│────┘
              │num_venta     │ │id_producto(FK)   │
              │total         │ │cantidad          │
              │estado_venta  │ │costo_unitario    │
              │estado_pago   │ └──────────────────┘
              │agencia_envio │
              │num_tracking  │
              └──────────────┘
                    │
                    │ 1:N
                    │
              ┌──────────────────┐
              │ DETALLES_VENTAS  │
              ├──────────────────┤
              │id_detalle(PK)    │
              │id_venta(FK)      │
              │id_producto(FK)   │
              │cantidad          │
              │precio_unitario   │
              └──────────────────┘

              ┌──────────────────────┐
              │ MOVIMIENTO_INVENTARIO│
              ├──────────────────────┤
              │id_movimiento(PK)     │
              │id_usuario(FK)        │
              │id_producto(FK)       │
              │tipo_movimiento       │
              │cantidad              │
              │stock_anterior        │
              │stock_posterior       │
              └──────────────────────┘

              ┌──────────────────────┐
              │      ALERTAS         │
              ├──────────────────────┤
              │id_alerta(PK)         │
              │id_usuario(FK)        │
              │id_producto(FK)       │
              │tipo_alerta           │
              │estado                │
              │enviado_whatsapp      │
              └──────────────────────┘

         ┌────────────────────────────────┐
         │ DASHBOARD_FINANCIERO (Reportes)│
         ├────────────────────────────────┤
         │id_dashboard(PK)                │
         │id_usuario(FK)                  │
         │fecha_reporte                   │
         │total_ingresos                  │
         │total_costos                    │
         │ganancia_neta                   │
         │margen_neto_pct                 │
         └────────────────────────────────┘

    ┌──────────────────────┐    ┌──────────────────────┐
    │AGENCIAS_TRANSPORTE   │    │  TARIFAS_ENVIO       │
    ├──────────────────────┤    ├──────────────────────┤
    │id_agencia(PK)        │◄──►│id_tarifa(PK)         │
    │nombre_agencia        │ 1:N│id_agencia(FK)        │
    │tipo_agencia          │    │provincia             │
    │api_key               │    │precio_base           │
    │estado                │    │precio_por_kg         │
    └──────────────────────┘    └──────────────────────┘

         ┌──────────────────────┐
         │   ROTULOS_ENVIO      │
         ├──────────────────────┤
         │id_rotulo(PK)         │
         │id_venta(FK)          │
         │id_agencia(FK)        │
         │numero_rotulo(UNIQUE) │
         │codigo_qr             │
         │estado                │
         └──────────────────────┘
```

---

## 📋 Tabla de Relaciones (Cardinalidad)

| Tabla 1 | Relación | Tabla 2 | Descripción |
|---------|----------|---------|------------|
| USUARIOS | 1:N | CANALES | Un usuario tiene múltiples canales de venta |
| USUARIOS | 1:N | PRODUCTOS | Un usuario puede gestionar múltiples productos |
| USUARIOS | 1:N | IMPORTACIONES | Un usuario registra múltiples importaciones |
| USUARIOS | 1:N | VENTAS | Un usuario realiza múltiples ventas |
| USUARIOS | 1:N | MOVIMIENTO_INVENTARIO | Un usuario genera múltiples movimientos |
| USUARIOS | 1:N | ALERTAS | Un usuario recibe múltiples alertas |
| USUARIOS | 1:N | DASHBOARD_FINANCIERO | Un usuario tiene múltiples reportes diarios |
| CANALES | 1:N | VENTAS | Un canal genera múltiples ventas |
| PRODUCTOS | 1:N | VENTAS (vía DETALLES) | Un producto se vende múltiples veces |
| PRODUCTOS | 1:N | PRORRATEO_IMPORTACIONES | Un producto está en múltiples importaciones |
| PRODUCTOS | 1:N | MOVIMIENTO_INVENTARIO | Un producto tiene múltiples movimientos |
| PRODUCTOS | 1:N | ALERTAS | Un producto genera múltiples alertas |
| IMPORTACIONES | 1:N | PRORRATEO_IMPORTACIONES | Una importación tiene múltiples ítems |
| VENTAS | 1:N | DETALLES_VENTAS | Una venta tiene múltiples productos |
| VENTAS | 1:N | ROTULOS_ENVIO | Una venta genera un rótulo de envío |
| AGENCIAS_TRANSPORTE | 1:N | TARIFAS_ENVIO | Una agencia tiene múltiples tarifas por provincia |
| AGENCIAS_TRANSPORTE | 1:N | ROTULOS_ENVIO | Una agencia genera múltiples rótulos |

---

## 🔑 Claves Primarias y Foráneas

### Claves Primarias (PK - Primary Key)
- Todas las tablas tienen una columna `id_*` como PK con AUTO_INCREMENT

### Claves Foráneas (FK - Foreign Key)
- Todas las FK tienen ON DELETE CASCADE para integridad referencial
- Se crearon índices en todas las FK para optimizar búsquedas

---

## 📝 Campos Especiales por Módulo

### 🔹 Módulo: Smart Sourcing (Importaciones)
- **Tabla: IMPORTACIONES**
  - `numero_factura`: Identificador único de la factura
  - `tipo_cambio`: Tipo de cambio del día USD a SOL
  - `costo_flete`, `costo_aduanas`, `costo_aranceles`: Desglose de costos
  - `documento_ruta`: Ruta del documento escaneado por IA
  - `estado_procesamiento`: Seguimiento de procesamiento IA

- **Tabla: PRORRATEO_IMPORTACIONES**
  - Distribuye costos de importación por producto
  - Cálculo: `costo_unitario_final = precio_original + (costos_totales / cantidad)`

### 🔹 Módulo: Inventario Omnicanal
- **Tabla: PRODUCTOS**
  - `stock_total`: Stock centralizado (sincronizado en todos los canales)
  - `stock_minimo`: Alerta cuando stock < stock_minimo
  - `margen_ganancia_pct`: Rentabilidad deseada

- **Tabla: MOVIMIENTO_INVENTARIO**
  - Auditoría completa de entrada/salida de stock
  - `tipo_movimiento`: entrada, salida, ajuste, devolución
  - Histórico de `stock_anterior` y `stock_posterior`

### 🔹 Módulo: Logística Automatizada
- **Tabla: AGENCIAS_TRANSPORTE**
  - Integración con APIs de agencias: Shalom, Olva, Marvisur

- **Tabla: TARIFAS_ENVIO**
  - Cotización automática por provincia/distrito
  - `precio_base + (peso_kg * precio_por_kg)`

- **Tabla: ROTULOS_ENVIO**
  - Generación de rótulos en 1 clic
  - `codigo_qr`: QR generado para impresión

### 🔹 Módulo: Panel Financiero
- **Tabla: DASHBOARD_FINANCIERO**
  - Resumen diario: ingresos, costos, ganancias netas
  - `margen_neto_pct`: Rentabilidad porcentual
  - `producto_mas_vendido`: Ranking en tiempo real

### 🔹 Módulo: Notificaciones
- **Tabla: ALERTAS**
  - Tipos: stock_bajo, quiebre_stock, venta_importante, retraso_envio, alerta_financiera
  - `enviado_whatsapp`: Flag para integración WhatsApp

---

## ✅ Pasos de Instalación en MySQL

### Opción 1: Línea de Comando
```bash
mysql -u root -p < omnicanal_db.sql
```

### Opción 2: MySQL Workbench
1. Abre MySQL Workbench
2. File → Open SQL Script → Selecciona `omnicanal_db.sql`
3. Ejecuta (Ctrl + Enter)

### Opción 3: PhpMyAdmin
1. Ve a phpMyAdmin
2. Click en "Importar"
3. Selecciona `omnicanal_db.sql`
4. Click en "Importar"

### Opción 4: MySQL Shell
```bash
mysql> SOURCE /ruta/a/omnicanal_db.sql;
```

---

## 🔍 Verificar Instalación

```sql
USE omnicanal_db;
SHOW TABLES;
-- Debería mostrar 13 tablas

DESCRIBE usuarios;
-- Verifica estructura de usuarios
```

---

## 📊 Estadísticas del Modelo

- **Total de Tablas:** 13
- **Total de Relaciones:** 20+
- **Total de Índices:** 30+ (PK + FK + índices de búsqueda)
- **Campos con Timestamps:** 11 tablas (created_at, updated_at)
- **ENUM Types:** 8 tipos diferentes para estados y categorías
- **Motor:** InnoDB (transacciones + integridad referencial)
- **Charset:** UTF-8 (Español, caracteres especiales)

---

## 🎯 Próximos Pasos

1. ✅ Ejecutar script SQL en MySQL
2. ⏳ Crear entidades JPA en Spring Boot (ORM)
3. ⏳ Generar repositorios automáticos (JpaRepository)
4. ⏳ Implementar servicios (lógica de negocio)
5. ⏳ Crear controladores REST (APIs)
6. ⏳ Integrar con Angular frontend
