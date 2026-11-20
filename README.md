# 🚀 Omnicanal - Sistema Web de Gestión de Importaciones y Ventas Omnicanal

## 📋 Descripción General

**Omnicanal** es una plataforma SaaS completa diseñada para democratizar la tecnología empresarial, ofreciendo a micro y pequeños importadores y vendedores de redes sociales en Perú y Latinoamérica una solución de **bajo costo, intuitiva y móvil**.

### Módulos Principales

1. **🔹 Smart Sourcing**: Cálculo automático de costos de importación con escaneo de facturas (IA)
2. **📦 Inventario Omnicanal**: Sincronización centralizada de stock en múltiples canales
3. **🚚 Logística Automatizada**: Integración con agencias masivas (Shalom, Olva, Marvisur)
4. **💰 Panel Financiero**: Dashboard de ingresos, costos y ganancias en tiempo real
5. **🔔 Alertas Inteligentes**: Notificaciones por WhatsApp y sistema de alertas

---

## 🛠️ Stack Tecnológico

- **Frontend**: Angular 21 (Standalone Components, lazy loading)
- **Backend**: Spring Boot 4.0.6
- **Base de Datos**: MySQL 8.0
- **Java**: JDK 17 (LTS)
- **Diseño**: Mobile-First, Responsive

---

## 📦 Estructura del Proyecto

```
Proyecto/
├── Backend/                           # Spring Boot Backend
│   ├── src/main/java/Backendcom/.../
│   │   ├── entity/                   # Entidades JPA (Usuario, Producto, Venta, etc.)
│   │   ├── repository/               # Interfaces JpaRepository
│   │   ├── service/                  # Servicios (lógica de negocio)
│   │   ├── controller/               # REST Controllers
│   │   └── BackendApplication.java   # Clase principal
│   ├── src/main/resources/
│   │   ├── application.properties    # Configuración MySQL
│   │   ├── schema.sql / data.sql     # Esquema y datos demo (se ejecutan al arrancar)
│   │   └── DER_OMNICANAL.md         # Diagrama Entidad-Relación
│   ├── pom.xml                      # Dependencias Maven
│   └── INSTRUCCIONES_BD.md          # Guía de instalación BD
│
└── Frontend/                          # Angular Frontend
    ├── src/app/
    │   ├── components/
    │   │   ├── login/               # Login & Registro
    │   │   ├── dashboard/           # Dashboard principal
    │   │   ├── productos/           # Gestión de inventario
    │   │   ├── ventas/              # Registro de ventas
    │   │   └── financiero/          # Panel financiero
    │   ├── services/
    │   │   ├── auth.service.ts      # Autenticación
    │   │   ├── producto.service.ts
    │   │   └── venta.service.ts
    │   ├── models/
    │   │   ├── usuario.model.ts
    │   │   ├── producto.model.ts
    │   │   ├── venta.model.ts
    │   │   ├── canal.model.ts
    │   │   └── importacion.model.ts
    │   ├── app.routes.ts            # Routing
    │   └── app.ts                   # Root component
    ├── package.json
    └── angular.json
```

---

## 🌐 Demo en vivo

- **App:** https://omnicanal.vercel.app
- **API:** https://omnicanal-api.onrender.com/api/health
- **Cuenta demo:** `juan@omnicanal.pe` / `Demo1234` (o el botón *Probar con la cuenta demo* del login)

> El backend está en el plan gratuito de Render y se duerme tras 15 min sin uso: la primera petición puede tardar hasta un minuto.

### Arquitectura de despliegue

| Capa | Servicio | Detalle |
|---|---|---|
| Frontend | Vercel | Angular 21, rutas lazy, headers de seguridad en `Frontend/vercel.json` |
| Backend | Render (Docker) | Spring Boot 4 + Java 17, `Backend/Dockerfile`, health check en `/api/health` |
| Base de datos | Aiven for MySQL | MySQL 8 gestionado con SSL; el esquema y los datos demo se crean solos al arrancar |

---

## 🚀 Cómo correrlo en local

### Requisitos

- Node.js 20+ y npm
- Java JDK 17+
- MySQL 8.0+

### 1. Base de datos

Crea una base vacía; las tablas y los datos demo se crean solos al arrancar el backend (`schema.sql` + `data.sql`, idempotentes).

```sql
CREATE DATABASE omnicanal_db;
```

### 2. Backend (Spring Boot)

Copia `Backend/.env.example`, completa los valores y expórtalos como variables de entorno (o configúralos en tu IDE):

```bash
cd Backend
export DB_URL="jdbc:mysql://localhost:3306/omnicanal_db?serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false"
export DB_USERNAME=root
export DB_PASSWORD=tu_password
export JWT_SECRET=$(openssl rand -base64 64)
./mvnw spring-boot:run
```

API disponible en `http://localhost:8080/api`.

### 3. Frontend (Angular)

```bash
cd Frontend
npm install
npm start
```

App disponible en `http://localhost:4200`. La URL del API se define en `src/environments/environment.ts` (local) y `environment.prod.ts` (producción).

---

## ☁️ Cómo desplegar

### Variables de entorno del backend (Render)

| Variable | Ejemplo / descripción |
|---|---|
| `DB_URL` | `jdbc:mysql://HOST:PUERTO/defaultdb?sslMode=REQUIRED&serverTimezone=UTC` |
| `DB_USERNAME` / `DB_PASSWORD` | Credenciales de Aiven |
| `JWT_SECRET` | `openssl rand -base64 64` (nunca la subas a Git) |
| `CORS_ALLOWED_ORIGINS` | `https://omnicanal.vercel.app` (separa varias con coma) |
| `SPRING_SQL_INIT_MODE` | `always` crea lo que falte al arrancar; `never` para no tocar la BD |

Render: *New Web Service* → repo → *Root Directory* `Backend` → *Runtime* Docker → *Health Check Path* `/api/health`.

### Frontend (Vercel)

*Add New Project* → repo → *Root Directory* `Frontend`. `vercel.json` ya define build, salida, rutas SPA y headers. Si cambia la URL del backend, actualízala en `environment.prod.ts` y en el `connect-src` del CSP de `vercel.json`.

---

## 📊 Datos de prueba

| Rol | Email | Contraseña |
|---|---|---|
| Emprendedor (demo pública) | `juan@omnicanal.pe` | `Demo1234` |
| Administrador global | `admin@omnicanal.pe` | Privada (no se publica) |

El registro público siempre crea cuentas de tipo **Emprendedor**; los administradores solo se crean desde el panel de un administrador global.

---

## 🔌 Endpoints API (Backend)

### Usuarios
- `GET /api/usuarios` - Obtener todos
- `POST /api/usuarios` - Crear usuario
- `GET /api/usuarios/{id}` - Obtener por ID
- `PUT /api/usuarios/{id}` - Actualizar

### Productos
- `GET /api/productos` - Listar todos
- `POST /api/productos` - Crear producto
- `GET /api/productos/usuario/{idUsuario}` - Obtener por usuario

### Ventas
- `GET /api/ventas` - Listar todas
- `POST /api/ventas` - Registrar venta
- `GET /api/ventas/usuario/{idUsuario}` - Obtener por usuario

---

## 🎨 Características de Diseño (UI/UX)

✅ **Mobile-First Responsive Design**
- Optimizado para celulares
- Diseño fluido para tablets y desktop
- Botones grandes y fáciles de tocar

✅ **Interfaz Moderna (Estilo Yape/Apps Bancarias)**
- Gradientes atractivos (Morado -> Azul)
- Tarjetas limpias y organizadas
- Tipografía clara y legible

✅ **Componentes Funcionales**
- Formularios intuitivos con validación
- Modales para crear/editar registros
- Listas dinámicas con filtros
- Gráficos de barras para análisis

✅ **Navegación Intuitiva**
- Menú hamburguesa en móvil
- Navbar sticky
- Enlaces de navegación rápida

---

## 📋 Base de Datos - Tablas Principales

| Tabla | Descripción |
|-------|-------------|
| **usuarios** | Datos de comerciantes y suscriptores |
| **productos** | Catálogo con precios costo/venta |
| **canales** | Puntos de venta (Web, WhatsApp, TikTok, Físico) |
| **ventas** | Transacciones de clientes |
| **detalles_ventas** | Ítems por venta |
| **importaciones** | Registro de compras internacionales |
| **prorrateo_importaciones** | Distribución de costos por producto |
| **movimiento_inventario** | Auditoría de entrada/salida de stock |
| **alertas** | Notificaciones y alertas de sistema |
| **dashboard_financiero** | Resumen diario de ingresos/costos |
| **agencias_transporte** | Proveedores logísticos (Shalom, Olva, etc.) |
| **tarifas_envio** | Precios de envío por provincia |
| **rotulos_envio** | Etiquetas de envío con QR |

---

## 🔒 Seguridad

- Secretos (BD, JWT) solo en variables de entorno; nada sensible en el código.
- Contraseñas con BCrypt; la contraseña nunca se devuelve en las respuestas.
- JWT stateless; el id de usuario (`X-User-Id`) siempre se toma del token, no del cliente, así cada usuario solo ve sus datos.
- Roles validados en el backend con `@PreAuthorize`; el registro público no permite elegir rol.
- CORS limitado al dominio del frontend; headers CSP, HSTS, X-Frame-Options y Referrer-Policy.
- Límite de intentos en login y registro (5 cada 15 min por IP).

---

## 🚧 Próximos Pasos (Fase 2)

1. ✅ **Base de datos** - Completada
2. ✅ **Backend básico** - Completado (CRUD APIs)
3. ✅ **Frontend básico** - Completado (UI presentable)
4. ⏳ **Autenticación JWT** - Por implementar
5. ⏳ **Validaciones avanzadas** - Por implementar
6. ⏳ **Escaneo OCR de facturas** - Por integrar
7. ⏳ **Integración APIs de agencias** - Por conectar
8. ⏳ **Generador de QR** - Por implementar
9. ⏳ **Dashboard financiero avanzado** - Por extender
10. ⏳ **Notificaciones WhatsApp** - Por integrar

---

## 📞 Soporte

Para reportar bugs o sugerencias, contacta al equipo de desarrollo.

---

**Omnicanal v0.1.0 - MVP (Minimum Viable Product)**
*Desarrollado para presentación rápida - Funcionalidad completa en desarrollo*
