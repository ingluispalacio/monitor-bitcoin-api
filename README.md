# 📊 Crypto Monitor - Microservicio de Monitoreo de Criptomonedas

> **⚠️ Proyecto de Estudio** - Este proyecto es desarrollado con fines educativos para aprender arquitectura de microservicios con Spring Boot Reactive, seguridad con JWT y operaciones reactivas en Java.

---

## 📌 Descripción General

**Crypto Monitor** es un microservicio backend que permite a los usuarios monitorear, comprar y gestionar órdenes de criptomonedas. El sistema integra precios en tiempo real desde CoinGecko, implementa autenticación y autorización basada en roles, y utiliza WebSockets para actualizaciones en tiempo real.

---

## 🎯 Lógica de Negocio

### Funcionalidades Principales

#### 1. **Autenticación y Autorización**
- Registro y login de usuarios con cifrado de contraseñas
- Generación de tokens JWT con expiración configurable (1 hora por defecto)
- Control de acceso basado en roles:
  - **ADMIN**: Gestión completa de usuarios del sistema
  - **CLIENT**: Acceso a órdenes y operaciones de compra de criptomonedas

#### 2. **Gestión de Órdenes de Criptomonedas**
- Creación de órdenes de compra para diferentes criptomonedas
- Seguimiento del estado de las órdenes (PENDING, COMPLETED, CANCELLED)
- Consulta de órdenes personales del cliente autenticado
- Integración con precios en tiempo real desde la API de CoinGecko

#### 3. **Gestión de Usuarios**
- CRUD de usuarios (solo ADMIN)
- Almacenamiento seguro con contraseñas hasheadas
- Información de usuario: nombre, apellido, email, username, rol

#### 4. **Feature Toggles (Banderas de Características)**
- Control de activación/desactivación de módulos del sistema
- Ejemplo: Permitir/denegar la funcionalidad de compra de criptomonedas
- Útil para testing, deployments canary y feature management

#### 5. **Monitoreo y Logging**
- Registro detallado de operaciones en archivo `./logs/crypto-monitor.log`
- Niveles configurables de logging para debugging
- Actuator endpoints para monitoreo de aplicación

---

## 🏗️ Arquitectura

### Estructura de Capas

```
┌─────────────────────────────────────────────────────┐
│           Controllers (REST Endpoints)              │
│  UserController | OrderController | AuthController  │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│        Business Layer (Services)                     │
│  UserService | OrderService | FeatureToggleService  │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│     Data Layer (R2DBC Repositories)                 │
│   UserRepository | OrderRepository | etc.           │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│       SQL Server Database (Reactive)                │
│           via r2dbc-mssql driver                    │
└─────────────────────────────────────────────────────┘
```

### Componentes Clave

| Componente | Propósito |
|-----------|----------|
| **Controllers** | Exponen endpoints REST y manejan solicitudes HTTP |
| **Services** | Contienen la lógica de negocio y orquestación |
| **Repositories** | Acceso a datos reactivo con R2DBC |
| **Models/Entities** | Representación de datos en base de datos |
| **DTOs** | Objetos de transferencia de datos (request/response) |
| **Security** | Autenticación JWT y configuración de seguridad |
| **WebSocket** | Comunicación bidireccional en tiempo real |
| **Config** | Configuración de CORS, Jackson, WebClient, etc. |

---

## 🛠️ Tecnología

### Stack Tecnológico

| Tecnología | Versión | Propósito |
|-----------|---------|----------|
| **Java** | 17 | Lenguaje de programación |
| **Spring Boot** | 3.0.6 | Framework principal |
| **Spring WebFlux** | 3.0.6 | Programación reactiva y WebSocket |
| **Spring Data R2DBC** | - | Acceso reactivo a base de datos |
| **SQL Server** | - | Base de datos relacional |
| **JWT (JSON Web Token)** | - | Autenticación stateless |
| **OpenAPI/Swagger** | 2.1.0 | Documentación interactiva de APIs |
| **Liquibase** | - | Versionado de esquema de BD |
| **Lombok** | 1.18.24 | Reducción de boilerplate código |
| **Maven** | - | Gestor de dependencias y build |

### Programación Reactiva

El proyecto utiliza **Project Reactor** para operaciones no bloqueantes:

- **Mono**: Para operaciones que retornan 0 o 1 resultado
- **Flux**: Para operaciones que retornan múltiples resultados (stream)
- **WebFlux**: Framework web reactivo
- Permite mayor escalabilidad con menos threads

---

## 📦 Dependencias Principales

### Core Framework
```xml
<!-- Spring Boot Web Framework Reactivo -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

### Base de Datos
```xml
<!-- R2DBC - Reactive Database Connectivity -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-r2dbc</artifactId>
</dependency>

<!-- Driver R2DBC para SQL Server -->
<dependency>
    <groupId>io.r2dbc</groupId>
    <artifactId>r2dbc-mssql</artifactId>
    <version>1.0.0.RELEASE</version>
</dependency>

<!-- JDBC para Liquibase (necesario para migraciones) -->
<dependency>
    <groupId>com.microsoft.sqlserver</groupId>
    <artifactId>mssql-jdbc</artifactId>
</dependency>

<!-- Versionado de Base de Datos -->
<dependency>
    <groupId>org.liquibase</groupId>
    <artifactId>liquibase-core</artifactId>
</dependency>
```

### Documentación y Seguridad
```xml
<!-- OpenAPI 3 con Swagger UI -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
    <version>2.1.0</version>
</dependency>

<!-- Seguridad y JWT -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

### Utilidades
```xml
<!-- Lombok - Reduce código boilerplate -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.24</version>
</dependency>

<!-- Spring Boot DevTools - Reload automático -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
</dependency>

<!-- Actuator - Monitoreo y health checks -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

---

## 🔌 Endpoints Principales

### Autenticación
```
POST   /api/v1/auth/login              - Login y obtener JWT
```

### Usuarios (ADMIN)
```
POST   /api/v1/users                   - Crear nuevo usuario
GET    /api/v1/users                   - Listar todos los usuarios
DELETE /api/v1/users/{id}              - Eliminar usuario
```

### Órdenes (CLIENTE)
```
POST   /api/v1/orders                  - Crear orden de compra
GET    /api/v1/orders/my-orders        - Obtener mis órdenes
```

### Feature Toggles
```
GET    /api/v1/feature-toggles         - Listar toggles (Explorar documentación Swagger)
```

---

## 🗄️ Esquema de Base de Datos

### Entidades Principales

#### **User**
```sql
- id: UUID (PK)
- username: VARCHAR (UNIQUE)
- email: VARCHAR (UNIQUE)
- password: VARCHAR (hasheada)
- name: VARCHAR
- lastname: VARCHAR
- role: VARCHAR (ADMIN, CLIENT)
- created_at: TIMESTAMP
```

#### **Order**
```sql
- id: UUID (PK)
- username: VARCHAR (FK -> User.username)
- crypto_name: VARCHAR (BTC, ETH, etc.)
- amount: DECIMAL
- price: DECIMAL
- total: DECIMAL
- status: VARCHAR (PENDING, COMPLETED, CANCELLED)
- created_at: TIMESTAMP
```

#### **FeatureToggle**
```sql
- id: UUID (PK)
- module_name: VARCHAR (UNIQUE)
- is_active: BOOLEAN
- created_at: TIMESTAMP
```

---

## ⚙️ Configuración

### `application.yml` - Configuraciones Principales

```yaml
# Server
server.port: 7077

# API Mapping
application.request.mappings: /api/v1

# JWT
jwt.secret: [clave secreta configurable]
jwt.expiration: 3600000 (1 hora)

# Base de Datos Reactive (R2DBC)
spring.r2dbc.url: r2dbc:sqlserver://localhost:56244/crypto_db
spring.r2dbc.username: sa
spring.r2dbc.password: 123456

# Migraciones (Liquibase)
spring.liquibase.enabled: true
spring.liquibase.change-log: classpath:db/db.changelog-master.yaml

# APIs Externas
services.coingecko.base-url: https://api.coingecko.com/api/v3

# Logging
logging.level.com.micro.service.crypto_monitor: DEBUG
logging.file.name: ./logs/crypto-monitor.log
```

---

## 🚀 Cómo Ejecutar

### Requisitos Previos
- Java 17+
- Maven 3.6+
- SQL Server local o remoto (en puerto 56244 por defecto)

### Pasos
1. **Clonar o descargar el proyecto**
2. **Configurar variables de entorno** (si es necesario):
   - Usuario y contraseña de SQL Server
   - URL de la base de datos
   - Clave secreta JWT

3. **Compilar y ejecutar**:
   ```bash
   mvnw clean package
   mvn spring-boot:run
   ```

4. **Acceder a Swagger UI**:
   ```
   http://localhost:7077/swagger-ui.html
   ```

---

## 📚 Conceptos de Aprendizaje

Este proyecto es ideal para aprender:

### Backend & Architecture
- ✅ **Microservicios** con Spring Boot
- ✅ **Programación Reactiva** con Project Reactor
- ✅ **Autenticación JWT** stateless
- ✅ **Control de acceso** basado en roles (RBAC)
- ✅ **Patrón de Capas** (Controller → Service → Repository)

### Base de Datos
- ✅ **R2DBC** - Acceso reactivo a BD
- ✅ **Liquibase** - Versionado de esquema
- ✅ **SQL Server** - BD relacional

### DevOps & Tools
- ✅ **Docker** (opcional - para SQL Server)
- ✅ **Swagger/OpenAPI** - Documentación automática
- ✅ **Logging y Monitoreo** con Actuator
- ✅ **Maven** - Gestor de dependencias

### Seguridad
- ✅ **Hash de contraseñas** (BCrypt)
- ✅ **Tokens JWT** con expiración
- ✅ **CORS** configurado
- ✅ **Autorización basada en roles**

---

## 🧪 Testing

El proyecto incluye dependencias para testing:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

Para ejecutar pruebas:
```bash
mvn test
```

---

## 📝 Notas y Consideraciones

⚠️ **Para Producción**:
- Cambiar claves secretas JWT
- Usar variables de entorno para credenciales
- Implementar refresh tokens
- Agregar rate limiting
- Mejorar manejo de errores
- Añadir más validaciones

✅ **Características Implementadas**:
- ✓ Autenticación y autorización con JWT
- ✓ CRUD reactivo de usuarios
- ✓ Creación y consulta de órdenes
- ✓ Feature toggles para control de funcionalidades
- ✓ WebSocket para comunicación en tiempo real
- ✓ Swagger/OpenAPI documentación
- ✓ CORS habilitado para cliente frontend
- ✓ Logging estructurado

---

## 📞 Contacto y Soporte

Este es un proyecto de **estudio y aprendizaje**. Para preguntas sobre la implementación o arquitectura, consulta la documentación de:
- [Spring Boot Reactive](https://spring.io/projects/spring-boot)
- [Project Reactor](https://projectreactor.io/)
- [Spring Security](https://spring.io/projects/spring-security)

---

**Última actualización**: Marzo 2026  
**Estado**: En desarrollo (Educational)  
**Versión**: 0.0.1-SNAPSHOT
