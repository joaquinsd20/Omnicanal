# 🚀 GUÍA RÁPIDA: Crear Base de Datos Omnicanal en MySQL

## ⚡ 3 Pasos Rápidos

### Paso 1: Abre Terminal MySQL
```bash
mysql -u root -p
```
Te pedirá tu contraseña de MySQL.

---

### Paso 2: Ejecuta el Script
Una vez dentro de MySQL, copia y pega TODO el contenido de `omnicanal_db.sql` en la terminal.

O, si prefieres desde fuera de MySQL:
```bash
mysql -u root -p < omnicanal_db.sql
```

---

### Paso 3: Verifica la Instalación
```bash
mysql -u root -p
USE omnicanal_db;
SHOW TABLES;
```

Deberías ver estas **13 tablas**:
1. usuarios
2. canales
3. productos
4. importaciones
5. prorrateo_importaciones
6. ventas
7. detalles_ventas
8. movimiento_inventario
9. alertas
10. dashboard_financiero
11. agencias_transporte
12. tarifas_envio
13. rotulos_envio

---

## 📍 Ubicación del Script SQL

**Ruta:** `Backend/src/main/resources/omnicanal_db.sql`

---

## 🔧 Configuración MySQL en application.properties

Después de crear la BD, configura tu Spring Boot:

**`src/main/resources/application.properties`**
```properties
spring.application.name=Backend

# MySQL Datasource
spring.datasource.url=jdbc:mysql://localhost:3306/omnicanal_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=tu_password_mysql
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.use_sql_comments=true

# Logging
logging.level.root=INFO
logging.level.org.springframework=INFO
logging.level.org.hibernate.SQL=DEBUG
```

---

## ✅ Checklist

- [ ] Instalaste MySQL
- [ ] Ejecutaste el script `omnicanal_db.sql`
- [ ] Verificaste que existan las 13 tablas
- [ ] Actualizaste `application.properties` con tu usuario/contraseña MySQL
- [ ] Descargaste el controlador MySQL (`mysql-connector-j` - ya está en `pom.xml`)

---

## 🆘 Troubleshooting

### Error: "Access denied for user 'root'@'localhost'"
```bash
mysql -u root -ptu_password  # Sin espacio entre -p y la contraseña
```

### Error: "Can't connect to MySQL server on 'localhost:3306'"
MySQL no está corriendo. Inicia el servicio:
- **Windows (CMD Admin):** `net start MySQL80`
- **Mac:** `brew services start mysql`
- **Linux:** `sudo systemctl start mysql`

### Error: "Unknown database 'omnicanal_db'"
Verifica que el script se ejecutó correctamente y creó la BD:
```bash
mysql -u root -p -e "SHOW DATABASES;"
```

---

## 📚 Archivos Relacionados

- **Diagrama DER:** `Backend/src/main/resources/DER_OMNICANAL.md`
- **Script SQL:** `Backend/src/main/resources/omnicanal_db.sql`
- **Configuración Spring:** `Backend/src/main/resources/application.properties`

---

**Listo para continuar con las entidades JPA en Spring Boot** ✅
