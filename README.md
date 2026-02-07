# 🛒 Tienda

Aplicación **backend** desarrollada con **Spring Boot** para la gestión de una tienda, que permite administrar clientes y cotizaciones, con persistencia en base de datos y preparación para integración con **Salesforce**.

Este proyecto fue creado como práctica para aplicar buenas bases de arquitectura backend, validaciones, JPA y diseño orientado a dominio.

---

## 🚀 Tecnologías

- **Java 21**
- **Spring Boot**
- **Spring Web MVC**
- **Spring Data JPA**
- **Bean Validation**
- **MySQL**
- **Maven**
- **Lombok**

---

## 🏗️ Arquitectura del proyecto

El proyecto está organizado por capas siguiendo buenas prácticas de diseño:

- **controller**: expone los endpoints de la API.
- **service**: contiene la lógica de negocio.
- **repository**: acceso a datos mediante JPA.
- **model**: entidades del dominio.
- **dto**: objetos de transferencia de datos.
- **exception**: manejo centralizado de errores.
- **config**: configuración general.
- **integration.salesforce**: integración con Salesforce.

Estructura base:

---

## 🧩 Modelo de dominio

### Cliente
Representa un cliente de la tienda.

**Campos principales:**
- nombres
- apellidos
- email (único)
- teléfono
- documento
- salesforceAccountId

**Reglas y validaciones:**
- Email obligatorio, formato válido y único.
- Nombres y apellidos obligatorios.
- Control de longitud en los campos.

**Relaciones:**
- Un cliente puede tener múltiples cotizaciones (1:N).

---

### Cotizacion
Representa una cotización asociada a un cliente.

**Campos principales:**
- cliente
- total (mayor a 0)
- estado
- salesforceQuoteId

**Relaciones:**
- Muchas cotizaciones pertenecen a un cliente (N:1).

---

### EstadoCotizacion (Enum)
Estados posibles de una cotización:
- `CREADA`
- `ENVIADA_SF`
- `ERROR`

---

## ✅ Reglas de negocio implementadas

- Un cliente debe tener nombres, apellidos y email válidos.
- El email del cliente es único.
- Una cotización debe estar asociada a un cliente.
- El total de la cotización debe ser mayor a 0.
- El estado inicial de la cotización es `CREADA`.
- Los campos `createdAt` se asignan automáticamente al persistir.

---

## 🗄️ Persistencia / Base de datos

El proyecto utiliza **Spring Data JPA** para la persistencia.

Tablas principales:
- `cliente`
   - Restricción única sobre el campo `email`.
- `cotizacion`
   - Clave foránea `cliente_id` → `cliente.id`.

---

## 🔗 Integración con Salesforce

El proyecto está preparado para integrarse con Salesforce:

- `Cliente.salesforceAccountId`: referencia al **Account** en Salesforce.
- `Cotizacion.salesforceQuoteId`: referencia al **Quote** en Salesforce.

El estado de la sincronización se controla mediante `EstadoCotizacion`:
- `CREADA`: creada localmente.
- `ENVIADA_SF`: enviada correctamente a Salesforce.
- `ERROR`: error durante la integración.

> ⚠️ Las credenciales de Salesforce deben manejarse mediante configuración externa o variables de entorno. No se suben al repositorio.

---

## ✅ Requisitos

- **JDK 21**
- **Maven** (opcional, se incluye Maven Wrapper)
- **MySQL** (para entorno local)

---

## ▶️ Cómo ejecutar el proyecto

### 1️⃣ Clonar el repositorio
```bash
git clone https://github.com/joasalazargi568/tienda.git
cd tienda
```

### 2️⃣ Configurar la base de datos (MySQL)

Configura las credenciales en el archivo application.properties:

```
spring.datasource.url=jdbc:mysql://localhost:3306/tienda
spring.datasource.username=TU_USUARIO
spring.datasource.password=TU_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.open-in-view=false
```

### 3️⃣ Ejecutar la aplicación
Usando Maven Wrapper:

```
./mvnw spring-boot:run
```

En Windows:

```
mvnw.cmd spring-boot:run
```

La aplicación se iniciará por defecto en:
```
http://localhost:8080
```

# 🧪 Pruebas

Para ejecutar las pruebas del proyecto:
```
./mvnw test
```

# 👤 Autor
### Johnatan Andres Salazar Giraldo
Backend Developer | Salesforce Developer
📍 Medellín, Colombia