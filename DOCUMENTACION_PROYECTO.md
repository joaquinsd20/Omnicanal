# 📱 OMNICANAL - Sistema de Gestión Comercial Multi-Canal

## 🎯 Descripción General

**Omnicanal** es una plataforma de gestión comercial completa diseñada para emprendedores y administradores que necesitan gestionar ventas, inventario e importaciones a través de múltiples canales de venta (tienda web, WhatsApp, TikTok Shop, Instagram, físico, etc.).

### Visión
Centralizar la operación comercial de un negocio en una única plataforma, permitiendo:
- ✅ Gestión integrada de múltiples canales de venta
- ✅ Control automático de inventario
- ✅ Cálculo automático de márgenes de ganancia
- ✅ Seguimiento de importaciones internacionales
- ✅ Dashboard ejecutivo en tiempo real
- ✅ Análisis de ventas por canal

---

## 🏗️ ARQUITECTURA GENERAL

### Stack Tecnológico

#### **Frontend (Angular 21)**
- Framework: Angular 21 (Standalone Components)
- Lenguaje: TypeScript 5.9 con strict mode
- Estado: RxJS 7.8 (Observables/Subjects)
- Formas: Reactive Forms con FormBuilder
- HTTP: HttpClient con interceptores personalizados
- Estilos: CSS3 custom (variables CSS, grid, flexbox)
- Visualización: XLSX para exportación a Excel
- Servidor: ng serve en puerto 4200

#### **Backend (Spring Boot)**
- Framework: Spring Boot 4.0.6
- Seguridad: Spring Security 7 + JWT HS256
- Base de datos: Spring Data JPA + Hibernate 7.2.12
- Gestor de dependencias: Maven 3.9.6
- Java: Versión 22
- Servidor: Tomcat 11.0.21 en puerto 8080
- Contexto: /api

#### **Base de Datos**
- DBMS: MySQL 9.5
- Host: localhost:3306
- Base de datos: omnicanal_db
- Usuario: root
- Contraseña: F20032512@aex

#### **Herramientas de Desarrollo**
- IDE: Visual Studio Code
- Versionado: Git
- Navegador: Chrome/Chromium
- Terminal: PowerShell

---

## 📊 BASE DE DATOS

### Estructura de Tablas Principales

#### 1. **usuarios** (Usuarios del sistema)
```
- id_usuario (PK, INT)
- nombre_usuario (VARCHAR)
- email (VARCHAR, UNIQUE)
- password (VARCHAR, ENCODED)
- tipo_usuario (ENUM: emprendedor|administrador)
- estado (ENUM: activo|inactivo)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

#### 2. **canales** (Canales de venta omnicanal)
```
- id_canal (PK, INT)
- id_usuario (FK → usuarios)
- nombre_canal (VARCHAR)
- tipo_canal (ENUM: tienda_web|whatsapp|tiktok_shop|instagram|fisico|otro)
- url_tienda (VARCHAR, NULLABLE)
- estado (ENUM: activo|inactivo)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

#### 3. **productos** (Inventario)
```
- id_producto (PK, INT)
- id_usuario (FK → usuarios)
- nombre_producto (VARCHAR)
- sku (VARCHAR, UNIQUE)
- categoria (VARCHAR)
- precio_costo (DECIMAL 10,2)
- precio_venta (DECIMAL 10,2)
- margen_ganancia_pct (DECIMAL 5,2) - % de margen deseado
- stock_total (INT)
- stock_minimo (INT, DEFAULT: 5) - umbral de alerta
- estado (ENUM: activo|inactivo|descontinuado)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

#### 4. **ventas** (Transacciones de ventas)
```
- id_venta (PK, INT)
- id_usuario (FK → usuarios)
- id_canal (FK → canales) - Canal desde el que se registró
- numero_venta (VARCHAR, UNIQUE) - Correlativo único
- nombre_cliente (VARCHAR)
- email_cliente (VARCHAR, NULLABLE)
- telefono_cliente (VARCHAR, NULLABLE)
- provincia_destino (VARCHAR)
- distrito_destino (VARCHAR)
- direccion_entrega (VARCHAR)
- agencia_envio (VARCHAR)
- subtotal (DECIMAL 10,2)
- costo_envio (DECIMAL 10,2)
- descuento (DECIMAL 10,2)
- total (DECIMAL 10,2)
- estado_venta (ENUM: pendiente|confirmada|enviada|entregada|cancelada)
- estado_pago (ENUM: pendiente|pagado)
- fecha_venta (TIMESTAMP)
- numero_tracking (VARCHAR, NULLABLE)
```

#### 5. **detalles_ventas** (Items de cada venta)
```
- id_detalle (PK, INT)
- id_venta (FK → ventas)
- id_producto (FK → productos)
- cantidad (INT)
- precio_unitario (DECIMAL 10,2)
- subtotal (DECIMAL 10,2)
```

#### 6. **importaciones** (Compras internacionales)
```
- id_importacion (PK, INT)
- id_usuario (FK → usuarios)
- numero_factura (VARCHAR, UNIQUE)
- proveedor_internacional (VARCHAR)
- pais_origen (VARCHAR)
- fecha_importacion (DATE)
- monto_usd (DECIMAL 10,2)
- monto_en_soles (DECIMAL 10,2)
- estado_importacion (ENUM: pendiente|en_transito|aduana|recibida|procesada)
- notas (TEXT)
```

#### 7. **movimiento_inventario** (Kardex - Auditoría de stock)
```
- id_movimiento (PK, INT)
- id_usuario (FK → usuarios)
- id_producto (FK → productos)
- tipo_movimiento (ENUM: entrada|salida|ajuste|devolucion)
- cantidad (INT)
- stock_anterior (INT)
- stock_posterior (INT)
- descripcion (VARCHAR)
- fecha (TIMESTAMP)
```

---

## 🔐 AUTENTICACIÓN Y SEGURIDAD

### JWT (JSON Web Tokens)
- **Algoritmo:** HS256
- **Token Storage:** localStorage con clave `omnicanal_token`
- **Usuario Storage:** localStorage con clave `omnicanal_usuario` (JSON stringified)
- **Formato Bearer:** `Authorization: Bearer <token>`

### Headers Requeridos
```
Authorization: Bearer <jwt_token>
X-User-Id: <id_usuario>  (En todas las solicitudes protegidas)
Content-Type: application/json
```

### Interceptores HTTP
1. **authInterceptor:** Automáticamente añade token y X-User-Id header
2. **errorInterceptor:** Maneja errores 401, 403 y excepciones genéricas

### Roles de Usuario
- **emprendedor:** Acceso a sus propios datos únicamente
- **administrador:** Acceso total al sistema

### Protección de Endpoints
- Decorador: `@PreAuthorize("hasAnyRole('emprendedor', 'administrador')")`
- Validación: X-User-Id header verifica propiedad de datos

---

## 🎨 ARQUITECTURA FRONTEND

### Estructura de Carpetas
```
Omnicanal/
├── src/
│   ├── app/
│   │   ├── components/
│   │   │   ├── dashboard/              (Panel principal)
│   │   │   ├── login/                  (Autenticación)
│   │   │   ├── productos/              (Gestión de inventario)
│   │   │   ├── ventas/                 (Registro de ventas)
│   │   │   ├── importaciones/          (Gestión de importaciones)
│   │   │   ├── kardex/                 (Auditoría de inventario)
│   │   │   └── financiero/             (Reportes)
│   │   ├── services/
│   │   │   ├── auth.service.ts         (Autenticación y usuario)
│   │   │   ├── venta.service.ts        (API de ventas)
│   │   │   ├── producto.service.ts     (API de productos)
│   │   │   ├── canal.service.ts        (API de canales omnicanal)
│   │   │   ├── importacion.service.ts  (API de importaciones)
│   │   │   └── ...
│   │   ├── models/
│   │   │   ├── usuario.model.ts
│   │   │   ├── venta.model.ts
│   │   │   ├── producto.model.ts
│   │   │   ├── canal.model.ts
│   │   │   └── ...
│   │   ├── guards/
│   │   │   └── auth.guard.ts           (Protege rutas)
│   │   ├── interceptors/
│   │   │   ├── auth.interceptor.ts
│   │   │   └── error.interceptor.ts
│   │   ├── app.routes.ts               (Routing)
│   │   ├── app.config.ts               (Providers)
│   │   └── app.ts                      (Componente raíz)
│   ├── styles.css                      (Estilos globales)
│   └── index.html
└── package.json
```

### Componentes Principales

#### **DashboardComponent**
- **Ruta:** /dashboard
- **Función:** Panel ejecutivo con KPIs y resúmenes
- **Datos:**
  - 📊 KPIs: Ingresos, ganancia, ventas del día
  - 📬 Importaciones recientes (últimas 3)
  - 💳 Ventas recientes (últimas 3)
  - 📋 Últimos movimientos Kardex (últimos 5)
  - 📦 Productos con stock bajo (últimos 5)
  - 📊 **Ventas por canal (NUEVA)** - Agregación omnicanal

#### **LoginComponent**
- **Ruta:** /login
- **Función:** Autenticación de usuarios
- **Credenciales de prueba:**
  - Email: `juan@omnicanal.pe`
  - Contraseña: `12345678`
  - Tipo: emprendedor

#### **VentasComponent**
- **Ruta:** /ventas
- **Función:** Registro y gestión de ventas
- **Características:**
  - Selector de canal omnicanal (dropdown)
  - Búsqueda y filtrado de productos
  - Cálculo automático de totales
  - Descuento y costo de envío
  - Selección de agencia de envío
  - Deducción automática de stock

#### **ProductosComponent**
- **Ruta:** /productos
- **Función:** Gestión de inventario
- **Características:**
  - CRUD completo de productos
  - Entrada de costo y cálculo automático de precio (margen automático)
  - Visualización de stock
  - Alertas de stock bajo

#### **ImportacionesComponent**
- **Ruta:** /importaciones
- **Función:** Gestión de importaciones internacionales
- **Características:**
  - Registro de facturas de compra internacional
  - Cálculo de prorrateo (distribuir costos entre productos)
  - Tracking de estado de importación

#### **KardexComponent**
- **Ruta:** /kardex
- **Función:** Auditoría de movimientos de inventario
- **Características:**
  - Filtrado por tipo de movimiento
  - **Exportación a Excel profesional** con estilos
  - Vista de entrada/salida/ajustes de stock

#### **FinancieroComponent**
- **Ruta:** /financiero
- **Función:** Reportes y análisis
- **Características:**
  - Visualización de márgenes
  - Análisis de costos
  - Reportes de rentabilidad

---

## 🔌 ARQUITECTURA BACKEND

### Estructura de Carpetas
```
Backend/src/main/java/Backendcom/example/Backend/
├── controller/
│   ├── AuthController.java              (Login/Logout)
│   ├── VentaController.java             (CRUD ventas + analytics)
│   ├── ProductoController.java          (CRUD productos)
│   ├── CanalController.java             (CRUD canales omnicanal)
│   ├── ImportacionController.java       (CRUD importaciones)
│   └── ...
├── entity/
│   ├── Usuario.java
│   ├── Venta.java
│   ├── DetalleVenta.java
│   ├── Producto.java
│   ├── Canal.java
│   ├── Importacion.java
│   ├── MovimientoInventario.java
│   └── ...
├── repository/
│   ├── UsuarioRepository.java
│   ├── VentaRepository.java
│   ├── ProductoRepository.java
│   ├── CanalRepository.java
│   ├── ImportacionRepository.java
│   └── ...
├── service/
│   ├── MovimientoInventarioService.java (Lógica de Kardex)
│   ├── VentaService.java                (Lógica de ventas)
│   ├── ProductoService.java             (Lógica de productos)
│   └── ...
├── security/
│   ├── JwtUtil.java                     (Generación y validación JWT)
│   ├── SecurityConfig.java              (Configuración de Spring Security)
│   └── JwtAuthenticationFilter.java
├── exception/
│   └── GlobalExceptionHandler.java
└── BackendApplication.java
```

### Entidades Principales (JPA)

#### **Usuario.java**
```java
@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    private String nombreUsuario;
    private String password;
    private String tipoUsuario; // emprendedor, administrador
    private String estado;
    
    @OneToMany(mappedBy = "usuario")
    private List<Venta> ventas;
    
    @OneToMany(mappedBy = "usuario")
    private List<Producto> productos;
    
    @OneToMany(mappedBy = "usuario")
    private List<Canal> canales;
}
```

#### **Producto.java**
```java
@Entity
@Table(name = "productos")
public class Producto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProducto;
    
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
    
    private String nombreProducto;
    private String sku;
    private String categoria;
    
    private BigDecimal precioCosto;
    private BigDecimal precioVenta;
    private BigDecimal margenGananciaPct;  // % deseado de margen
    
    private Integer stockTotal;
    private Integer stockMinimo; // Umbral de alerta (default: 5)
    private String estado;
    
    // HOOK: Cálculo automático de precio según margen
    @PrePersist
    @PreUpdate
    public void calcularMargen() {
        if (precioCosto != null && margenGananciaPct != null) {
            // Formula: precioVenta = precioCosto × (1 + margen/100)
            this.precioVenta = precioCosto
                .multiply(BigDecimal.ONE
                    .add(margenGananciaPct.divide(new BigDecimal(100))));
        }
    }
}
```

#### **Canal.java**
```java
@Entity
@Table(name = "canales")
public class Canal {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCanal;
    
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
    
    private String nombreCanal;
    private String tipoCanal;  // tienda_web, whatsapp, tiktok_shop, etc.
    private String urlTienda;
    private String estado;  // activo, inactivo
}
```

#### **Venta.java**
```java
@Entity
@Table(name = "ventas")
public class Venta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVenta;
    
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
    
    @ManyToOne
    @JoinColumn(name = "id_canal", nullable = false)
    private Canal canal;  // Canal desde el que se registró
    
    private String numeroVenta;  // Correlativo único
    private String nombreCliente;
    private BigDecimal subtotal;
    private BigDecimal costoEnvio;
    private BigDecimal descuento;
    private BigDecimal total;
    
    private String estadoVenta;   // pendiente, confirmada, enviada, entregada
    private String estadoPago;    // pendiente, pagado
    private String agenciaEnvio;
    private String numeroTracking;
    
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL)
    private List<DetalleVenta> detalles;
}
```

#### **MovimientoInventario.java**
```java
@Entity
@Table(name = "movimiento_inventario")
public class MovimientoInventario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMovimiento;
    
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
    
    @ManyToOne
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;
    
    private String tipoMovimiento;  // entrada, salida, ajuste, devolucion
    private Integer cantidad;
    private Integer stockAnterior;
    private Integer stockPosterior;
    private String descripcion;
    private LocalDateTime fecha;
}
```

---

## 🔌 ENDPOINTS API

### Base URL
```
http://localhost:8080/api
```

### Autenticación
```
POST /auth/login
Body: { "email": "juan@omnicanal.pe", "password": "12345678" }
Response: { "token": "eyJhbGc...", "usuario": {...} }
```

### Productos
```
GET    /productos                        Lista todos los productos del usuario
GET    /productos/{id}                   Obtener producto por ID
GET    /productos/stock-bajo             Productos con stock bajo (< stockMinimo)
POST   /productos                        Crear producto
PUT    /productos/{id}                   Actualizar producto
DELETE /productos/{id}                   Eliminar producto
```

### Ventas
```
GET    /ventas                           Lista todas las ventas del usuario
GET    /ventas/{id}                      Obtener venta por ID
GET    /ventas/usuario/{idUsuario}       Ventas de un usuario específico
POST   /ventas                           Crear nueva venta
PUT    /ventas/{id}                      Actualizar venta
DELETE /ventas/{id}                      Eliminar venta
GET    /ventas/resumen/por-canal         Agregación de ventas por canal ⭐ NUEVO
```

### Canales (Omnicanal)
```
GET    /canales/usuario/{idUsuario}      Canales del usuario
GET    /canales/{idCanal}                Obtener canal por ID
POST   /canales                          Crear nuevo canal
PUT    /canales/{idCanal}                Actualizar canal
DELETE /canales/{idCanal}                Eliminar canal
```

### Importaciones
```
GET    /importaciones                    Lista importaciones del usuario
GET    /importaciones/{id}               Obtener importación por ID
POST   /importaciones                    Crear importación
PUT    /importaciones/{id}               Actualizar importación
DELETE /importaciones/{id}               Eliminar importación
```

### Kardex (Movimientos de Inventario)
```
GET    /kardex                           Todos los movimientos del usuario
GET    /kardex/usuario/{idUsuario}       Movimientos de un usuario
GET    /kardex/producto/{idProducto}    Movimientos de un producto
```

---

## 🚀 CARACTERÍSTICAS PRINCIPALES

### ✅ FASE 1: Gestión Básica de Inventario
- [x] CRUD completo de productos
- [x] Cálculo automático de margen (fórmula: precioVenta = precioCosto × (1 + margen/100))
- [x] Stock tracking automático
- [x] Alertas de stock bajo (stockMinimo)
- [x] Kardex (auditoría de movimientos)

### ✅ FASE 2: Multi-Canal (Omnicanal)
- [x] Creación de múltiples canales de venta
- [x] Selector de canal en formulario de ventas
- [x] Filtrado de canales activos
- [x] 4 canales de prueba: Web, WhatsApp, TikTok Shop, Físico
- [x] API CanalController completo

### ✅ FASE 3: Dashboard Analytics por Canal (RECIENTE)
- [x] Endpoint `/ventas/resumen/por-canal` para agregación
- [x] Sección en dashboard "Ventas por Canal"
- [x] Tabla compacta mostrando: Canal | Tipo | Cantidad | Monto Total
- [x] Diseño reorganizado a 3 columnas
- [x] Combinación de "Alertas & Canales" en una tarjeta

### 📋 Otras Características
- ✅ Exportación a Excel profesional (XLSX con estilos)
- ✅ Autenticación JWT con roles
- ✅ Dashboard ejecutivo en tiempo real
- ✅ Gestión de importaciones internacionales
- ✅ Auto-refresh cada 30 segundos
- ✅ Diseño responsivo (mobile, tablet, desktop)
- ✅ Múltiples agencias de envío
- ✅ Cálculo de totales automático

---

## 📊 DATOS DE PRUEBA

### Usuario Principal
```
Email: juan@omnicanal.pe
Contraseña: 12345678
Tipo: emprendedor
ID: 1
```

### Canales Disponibles (4 canales activos)
1. **Tienda Web Principal** (tienda_web) - Canal principal
2. **WhatsApp Business** (whatsapp) - Ventas por WhatsApp
3. **TikTok Shop** (tiktok_shop) - Integración TikTok Shop
4. **Tienda Física - Lima Centro** (fisico) - Punto de venta físico

### Productos de Prueba
- Smartphone X200 (SKU: SKU-PHONE-001) - Stock: 3 und.
- Auriculares Inalámbricos (SKU: SKU-AUDIO-001) - Stock: 8 und.

### Ventas de Prueba
- 5 ventas registradas
- Monto total acumulado: S/. 19,935.00
- Ticket promedio: S/. 3,987.00

---

## 🛠️ INSTALACIÓN Y CONFIGURACIÓN

### Requisitos Previos
- Node.js 18+ (para Angular)
- Java 22
- Maven 3.9.6
- MySQL 9.5

### Frontend (Angular)
```bash
cd Omnicanal
npm install
ng serve
# Acceder a http://localhost:4200
```

### Backend (Spring Boot)
```bash
cd Backend
mvn clean compile
mvn spring-boot:run
# Servidor en http://localhost:8080/api
```

### Base de Datos
```sql
-- Crear base de datos
CREATE DATABASE omnicanal_db;

-- Ejecutar script (omnicanal_db.sql)
mysql -u root -p omnicanal_db < omnicanal_db.sql
```

---

## 🎯 FLUJOS PRINCIPALES

### Flujo de Venta
1. Usuario inicia sesión
2. Va a Ventas → Nueva venta
3. **Selecciona canal** (Web, WhatsApp, TikTok, Físico)
4. Ingresa datos del cliente
5. Busca y añade productos (stock se deduce automáticamente)
6. Sistema calcula automáticamente totales
7. Registra venta en BD con canal asociado
8. Stock se actualiza en Kardex

### Flujo de Importación
1. Usuario registra nueva importación
2. Ingresa datos de factura y proveedor
3. Sistema calcula prorrateo de costos
4. Actualiza stock de productos
5. Registra movimiento en Kardex

### Flujo de Análisis por Canal
1. Dashboard carga automáticamente
2. Endpoint `/ventas/resumen/por-canal` agrupa ventas
3. Muestra tabla: Canal | Tipo | Cantidad | Monto
4. Permite seguimiento de desempeño por canal

---

## 📱 RESPONSIVE DESIGN

- **Desktop (> 1024px):** 3 columnas en dashboard
- **Tablet (900px - 1024px):** 2-1 columnas con reorganización
- **Mobile (< 560px):** 1 columna, menú colapsable

---

## 🔍 NOTAS TÉCNICAS IMPORTANTES

### Variables CSS Globales (en styles.css)
```css
--primary: #4f46e5 (Índigo)
--success: #16a34a (Verde)
--warning: #ea580c (Naranja)
--danger: #dc2626 (Rojo)
--muted: #64748b (Gris)
--border: #e2e8f0 (Gris claro)
--text: #1e293b (Texto oscuro)
```

### Fórmula de Margen Automático
```
Precio de Venta = Precio de Costo × (1 + Margen% / 100)

Ejemplo:
- Costo: S/. 100
- Margen deseado: 50%
- Precio Venta = 100 × (1 + 50/100) = 100 × 1.5 = S/. 150
```

### Headers Críticos
- `Authorization: Bearer <token>` - Obligatorio
- `X-User-Id: <id>` - Necesario para filtrado de datos
- `Content-Type: application/json` - Para POST/PUT

### LocalStorage Keys
- `omnicanal_token` - JWT token
- `omnicanal_usuario` - Objeto usuario (JSON stringified)

---

## 📈 MÉTRICAS DEL SISTEMA

### Performance
- Bundle Size: ~558 KB (Angular)
- Tiempo de carga: < 5 segundos
- Auto-refresh: 30 segundos

### Escalabilidad
- Soporta múltiples usuarios (filtrado por X-User-Id)
- Múltiples canales por usuario (ilimitado)
- Miles de transacciones por usuario

---

## 🔐 SEGURIDAD

- ✅ JWT con algoritmo HS256
- ✅ Passwords hasheados
- ✅ X-User-Id header para contexto de usuario
- ✅ @PreAuthorize para control de roles
- ✅ CORS configurado
- ✅ HttpOnly cookies (opcional)

---

## 📝 CHANGELOG

### v1.0.0 - FASE 3 (Actual)
- ✅ Endpoint de agregación de ventas por canal
- ✅ Dashboard reorganizado a 3 columnas
- ✅ Tarjeta combinada "Alertas & Canales"
- ✅ Estilos compactos sin espacios en blanco
- ✅ Mejor UX visual

### v0.9.0 - FASE 2
- ✅ Multi-canal (Omnicanal)
- ✅ CanalController y CanalService
- ✅ Selector de canal en ventas
- ✅ 4 canales de prueba

### v0.8.0 - FASE 1
- ✅ Gestión de productos
- ✅ Margen automático
- ✅ Stock alerts
- ✅ CRUD ventas

---

## 📞 CONTACTO Y SOPORTE

Para dudas sobre la arquitectura o implementación, revisar:
- Logs del backend en console
- DevTools de Angular en navegador
- Queries MySQL en HeidiSQL o Workbench

---

**Documentación generada:** 2026-07-05
**Última actualización:** FASE 3 - Dashboard Omnicanal
