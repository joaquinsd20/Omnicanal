🚀 Omnicanal - Sistema Web de Gestión de Importaciones y Ventas Omnicanal
📋 Descripción General
Omnicanal es una plataforma SaaS completa diseñada para democratizar la tecnología empresarial, ofreciendo a micro y pequeños importadores y vendedores de redes sociales en Perú y Latinoamérica una solución de bajo costo, intuitiva y móvil.

Módulos Principales
🔹 Smart Sourcing: Cálculo automático de costos de importación con escaneo de facturas (IA)
📦 Inventario Omnicanal: Sincronización centralizada de stock en múltiples canales
🚚 Logística Automatizada: Integración con agencias masivas (Shalom, Olva, Marvisur)
💰 Panel Financiero: Dashboard de ingresos, costos y ganancias en tiempo real
🔔 Alertas Inteligentes: Notificaciones por WhatsApp y sistema de alertas
🛠️ Stack Tecnológico
Frontend: Angular 17+ (Standalone Components)
Backend: Spring Boot 4.0.6
Base de Datos: MySQL 8.0
Java: JDK 17 (LTS)
Diseño: Mobile-First, Responsive
📦 Estructura del Proyecto
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
│   │   ├── omnicanal_db.sql         # Script SQL (13 tablas)
│   │   └── DER_OMNICANAL.md         # Diagrama Entidad-Relación
│   ├── pom.xml                      # Dependencias Maven
│   └── INSTRUCCIONES_BD.md          # Guía de instalación BD
│
└── Omnicanal/                         # Angular Frontend
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
🚀 Guía de Instalación y Ejecución
Requisitos Previos
Node.js 18+ y npm
Java JDK 17+
Maven 3.8+
MySQL 8.0+
Paso 1: Clonar/Preparar el Proyecto
cd c:\Users\CompuMorillas\Desktop\Proyecto
Paso 2: Configurar MySQL y BD
A) Crear la base de datos ejecutando el script SQL:

mysql -u root -pF20032512@aex < Backend/src/main/resources/omnicanal_db.sql
B) Verificar que se crearon las 13 tablas:

mysql -u root -pF20032512@aex
USE omnicanal_db;
SHOW TABLES;
Paso 3: Ejecutar Backend (Spring Boot)
cd Backend

# Compilar y ejecutar
./mvnw clean spring-boot:run

# O con Maven instalado globalmente
mvn clean spring-boot:run
Backend estará disponible en: http://localhost:8080/api

Paso 4: Ejecutar Frontend (Angular)
cd Omnicanal

# Instalar dependencias
npm install

# Ejecutar servidor de desarrollo
ng serve

# O con npm
npm start
Frontend estará disponible en: http://localhost:4200

📊 Datos de Prueba
Usuario de Prueba (registrado automáticamente):

Email: juan@omnicanal.pe
Contraseña: hashed_password_123
Nombre: Juan Comerciante
O crea un nuevo usuario en la pantalla de registro.

🔌 Endpoints API (Backend)
Usuarios
GET /api/usuarios - Obtener todos
POST /api/usuarios - Crear usuario
GET /api/usuarios/{id} - Obtener por ID
PUT /api/usuarios/{id} - Actualizar
Productos
GET /api/productos - Listar todos
POST /api/productos - Crear producto
GET /api/productos/usuario/{idUsuario} - Obtener por usuario
Ventas
GET /api/ventas - Listar todas
POST /api/ventas - Registrar venta
GET /api/ventas/usuario/{idUsuario} - Obtener por usuario
🎨 Características de Diseño (UI/UX)
✅ Mobile-First Responsive Design

Optimizado para celulares
Diseño fluido para tablets y desktop
Botones grandes y fáciles de tocar
✅ Interfaz Moderna (Estilo Yape/Apps Bancarias)

Gradientes atractivos (Morado -> Azul)
Tarjetas limpias y organizadas
Tipografía clara y legible
✅ Componentes Funcionales

Formularios intuitivos con validación
Modales para crear/editar registros
Listas dinámicas con filtros
Gráficos de barras para análisis
✅ Navegación Intuitiva

Menú hamburguesa en móvil
Navbar sticky
Enlaces de navegación rápida
📋 Base de Datos - Tablas Principales
Tabla	Descripción
usuarios	Datos de comerciantes y suscriptores
productos	Catálogo con precios costo/venta
canales	Puntos de venta (Web, WhatsApp, TikTok, Físico)
ventas	Transacciones de clientes
detalles_ventas	Ítems por venta
importaciones	Registro de compras internacionales
prorrateo_importaciones	Distribución de costos por producto
movimiento_inventario	Auditoría de entrada/salida de stock
alertas	Notificaciones y alertas de sistema
dashboard_financiero	Resumen diario de ingresos/costos
agencias_transporte	Proveedores logísticos (Shalom, Olva, etc.)
tarifas_envio	Precios de envío por provincia
rotulos_envio	Etiquetas de envío con QR
🔒 Configuración Actual
Backend (application.properties):

spring.datasource.url=jdbc:mysql://localhost:3306/omnicanal_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=F20032512@aex
spring.jpa.hibernate.ddl-auto=validate
Frontend (environment.ts):

apiUrl: 'http://localhost:8080/api'
🚧 Próximos Pasos (Fase 2)
✅ Base de datos - Completada
✅ Backend básico - Completado (CRUD APIs)
✅ Frontend básico - Completado (UI presentable)
⏳ Autenticación JWT - Por implementar
⏳ Validaciones avanzadas - Por implementar
⏳ Escaneo OCR de facturas - Por integrar
⏳ Integración APIs de agencias - Por conectar
⏳ Generador de QR - Por implementar
⏳ Dashboard financiero avanzado - Por extender
⏳ Notificaciones WhatsApp - Por integrar
📞 Soporte
Para reportar bugs o sugerencias, contacta al equipo de desarrollo.

Omnicanal v0.1.0 - MVP (Minimum Viable Product) Desarrollado para presentación rápida - Funcionalidad completa en desarrollo
