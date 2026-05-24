# 🦷 DentalPlus Back-end Repository

Backend REST para **DentalPlus**, una aplicación de gestión para una clínica dental. El proyecto está desarrollado con **Java + Spring Boot** y cubre autenticación JWT, roles de usuario, pacientes, citas, odontogramas, documentos PDF, imágenes de perfil, datos seed, colección Postman, despliegue con Docker/Render y conexión con servicios externos.

Este README está pensado para dos tipos de lector:

- una persona con experiencia que necesita entender rápido la arquitectura y los puntos de integración;
- una persona que empieza con Java/Spring y necesita saber qué tocar, dónde tocarlo y qué puede romperse.

> **Aviso importante de seguridad**  
> En el proyecto original se han detectado valores sensibles en `application.properties` como credenciales de base de datos, claves JWT y claves de servicios externos. No deben publicarse ni compartirse. Mueve esos valores a variables de entorno y rota las credenciales si el ZIP o repositorio se ha compartido.

---

## Índice

1. [Qué es DentalPlus](#qué-es-dentalplus)
2. [Arquitectura general](#arquitectura-general)
3. [Tecnologías usadas](#tecnologías-usadas)
4. [Estructura de carpetas](#estructura-de-carpetas)
5. [Configuración y variables de entorno](#configuración-y-variables-de-entorno)
6. [Cómo levantar el proyecto en local](#cómo-levantar-el-proyecto-en-local)
7. [Render y despliegue](#render-y-despliegue)
8. [Base de datos](#base-de-datos)
9. [Autenticación JWT](#autenticación-jwt)
10. [Roles y permisos](#roles-y-permisos)
11. [Servicios externos: Cloudinary y Supabase](#servicios-externos-cloudinary-y-supabase)
12. [Endpoints disponibles](#endpoints-disponibles)
13. [Ejemplos de requests y responses](#ejemplos-de-requests-y-responses)
14. [DTOs principales](#dtos-principales)
15. [Odontograma](#odontograma)
16. [Seed de datos](#seed-de-datos)
17. [Postman](#postman)
18. [Tests](#tests)
19. [Cómo modificar partes importantes](#cómo-modificar-partes-importantes)
20. [Errores comunes y depuración](#errores-comunes-y-depuración)
21. [Diagramas Mermaid](#diagramas-mermaid)
22. [Checklist antes de subir cambios](#checklist-antes-de-subir-cambios)
23. [Notas pendientes de confirmar](#notas-pendientes-de-confirmar)

---

## Qué es DentalPlus

DentalPlus Backend es una API REST para gestionar una clínica dental. Permite manejar usuarios internos, pacientes, citas, odontogramas, documentos clínicos y fotos de perfil.

Funcionalidades principales detectadas en el código:

- Login con JWT.
- Perfil del usuario autenticado.
- Roles: admin, dentista, recepcionista y paciente.
- Gestión de pacientes.
- Gestión de citas y disponibilidad.
- Gestión de odontogramas.
- Estados de piezas dentales.
- Marcas por superficie dental.
- Puentes dentales.
- Documentos PDF de pacientes.
- Imágenes de perfil con Cloudinary.
- Documentos con Supabase Storage.
- Seed destructivo con modos demo, rendimiento y estrés.
- Colección Postman lista para probar la API.
- Dockerfile para despliegue.

---

## Arquitectura general

El proyecto usa una arquitectura por capas:

```text
Controller -> Service -> DAO -> DAO Impl Hibernate -> Model/Entity -> Database
                    |
                    +-> DTOs
                    +-> Servicios externos
```

### Capas principales

| Capa | Carpeta | Responsabilidad |
|---|---|---|
| Controller | `controller/` | Expone endpoints REST. Recibe requests HTTP y devuelve responses. |
| Service | `service/` | Contiene la lógica de negocio, validaciones y permisos. |
| DAO | `dao/` | Define operaciones de acceso a datos. |
| DAO Impl Hibernate | `daoImplHibernate/` | Implementa los DAOs usando `EntityManager`/Hibernate. |
| DTO | `dto/` | Define los objetos que entran y salen por la API. |
| Model | `model/` | Entidades JPA y validaciones de dominio. |
| Config | `config/` | Seguridad, JWT, Cloudinary y Supabase. |
| Seed | `seed/` | Carga de datos demo, rendimiento, estrés y limpieza destructiva. |

### Importante

Este backend **no usa únicamente Spring Data Repository**. Tiene interfaces DAO propias y una implementación manual con Hibernate en `daoImplHibernate/`. Si modificas una entidad o consulta, revisa tanto el DAO como su implementación.

---

## Tecnologías usadas

| Tecnología | Uso |
|---|---|
| Java 21 | Versión usada actualmente en ejecución local y Docker. |
| Spring Boot 4.0.3 | Framework principal del backend. |
| Spring Web / WebMVC | API REST. |
| Spring Security | Protección de endpoints y filtro JWT. |
| JPA / Hibernate | Persistencia. |
| MySQL | Base de datos principal. |
| H2 | Dependencia usada para contexto/test. |
| JJWT | Creación y validación de JWT con claves RSA. |
| Cloudinary | Almacenamiento de imágenes de perfil. |
| Supabase Storage | Almacenamiento de documentos PDF. |
| Maven | Build y gestión de dependencias. |
| Docker | Contenedor de despliegue. |
| Render | URL base detectada en Postman/seed. |
| Postman | Pruebas manuales de endpoints. |

> **Nota sobre Java**  
> El proyecto se está ejecutando actualmente con Java 21. Conviene mantener alineados `pom.xml`, entorno local y `Dockerfile` para evitar diferencias entre desarrollo y despliegue.

---

## Estructura de carpetas

```text
DentalPlus_Backend/
├── Dockerfile
├── pom.xml
├── mvnw
├── mvnw.cmd
├── postman/
│   └── DentalPlus_Postman.json
├── src/
│   ├── main/
│   │   ├── java/com/example/DentalPlus_Backend/
│   │   │   ├── DentalPlusApplication.java
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dao/
│   │   │   ├── daoImplHibernate/
│   │   │   ├── dto/
│   │   │   ├── model/
│   │   │   ├── seed/
│   │   │   └── service/
│   │   └── resources/
│   │       ├── application.properties
│   │       └── seed/
│   │           ├── general-consent.pdf
│   │           ├── treatment-plan.pdf
│   │           └── profile-images/
│   │               ├── profile-01.jpg
│   │               ├── profile-02.jpg
│   │               ├── ...
│   │               └── profile-50.jpg
│   └── test/
│       ├── java/com/example/DentalPlus_Backend/ApplicationTest.java
│       └── resources/application-test.properties
└── target/                 # Generado por Maven. No debería versionarse.
```

### Clases importantes

| Clase | Función |
|---|---|
| `DentalPlusApplication` | Punto de entrada de Spring Boot. |
| `SecurityConfig` | Configura seguridad HTTP, filtro JWT y endpoints públicos/protegidos. |
| `JwtConfig` | Carga claves JWT y expiración. |
| `JwtService` | Genera, valida y lee tokens JWT. |
| `UserController` | Login y perfil del usuario autenticado. |
| `PatientController` | CRUD principal de pacientes. |
| `AppointmentController` | Citas y disponibilidad. |
| `DocumentController` | Subida/listado/eliminación de documentos PDF. |
| `OdontogramController` | Odontogramas, piezas, superficies, marcas y puentes. |
| `CloudinaryService` | Subida y eliminación de imágenes de perfil. |
| `SupabaseStorageService` | Subida y eliminación de PDFs. |
| `ApplicationSeed` | Seed destructivo y menú principal de carga de datos. |
| `StressSeed` | Generación masiva de pacientes, odontogramas y citas para prueba de estrés. |

---

## Configuración y variables de entorno

La configuración principal está en:

```text
src/main/resources/application.properties
```

Variables o propiedades detectadas:

| Variable/propiedad | Uso | Recomendación |
|---|---|---|
| `SPRING_DATASOURCE_URL` | URL JDBC de MySQL. | Obligatoria por entorno. |
| `SPRING_DATASOURCE_USERNAME` | Usuario de base de datos. | No versionar. |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de base de datos. | No versionar. |
| `PORT` | Puerto de ejecución. | Render suele inyectarlo. |
| `CLOUDINARY_URL` | Credenciales Cloudinary. | No versionar. |
| `SUPABASE_URL` | URL del proyecto Supabase. | Usar variable de entorno. |
| `SUPABASE_KEY` | API key de Supabase. | No versionar. |
| `SUPABASE_BUCKET_DOCUMENTS` | Bucket de documentos. | Valor por defecto: `documents`. |
| `AUTH_TOKEN_PRIVATE_KEY` | Clave privada RSA para firmar JWT. | No versionar. |
| `AUTH_TOKEN_PUBLIC_KEY` | Clave pública RSA para validar JWT. | No versionar. |
| `AUTH_TOKEN_EXPIRATION_MS` | Duración del token en ms. | Valor por defecto: `604800000`. |

Ejemplo seguro de configuración local con variables de entorno:

```bash
export SPRING_DATASOURCE_URL="jdbc:mysql://host:3306/database"
export SPRING_DATASOURCE_USERNAME="usuario"
export SPRING_DATASOURCE_PASSWORD="contraseña"
export CLOUDINARY_URL="cloudinary://..."
export SUPABASE_URL="https://xxxxx.supabase.co"
export SUPABASE_KEY="..."
export SUPABASE_BUCKET_DOCUMENTS="documents"
export AUTH_TOKEN_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----..."
export AUTH_TOKEN_PUBLIC_KEY="-----BEGIN PUBLIC KEY-----..."
export AUTH_TOKEN_EXPIRATION_MS="604800000"
```

En Windows PowerShell:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:mysql://host:3306/database"
$env:SPRING_DATASOURCE_USERNAME="usuario"
$env:SPRING_DATASOURCE_PASSWORD="contraseña"
$env:CLOUDINARY_URL="cloudinary://..."
$env:SUPABASE_URL="https://xxxxx.supabase.co"
$env:SUPABASE_KEY="..."
$env:SUPABASE_BUCKET_DOCUMENTS="documents"
$env:AUTH_TOKEN_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----..."
$env:AUTH_TOKEN_PUBLIC_KEY="-----BEGIN PUBLIC KEY-----..."
$env:AUTH_TOKEN_EXPIRATION_MS="604800000"
```

> **No subas credenciales reales a GitHub.** Usa variables de entorno en local, Render o el proveedor donde despliegues.

---

## Cómo levantar el proyecto en local

### Requisitos

- Java compatible con el proyecto. Recomendado: Java 21.
- Maven, o usar el Maven Wrapper incluido (`mvnw` / `mvnw.cmd`).
- Acceso a una base de datos MySQL.
- Variables de entorno configuradas para DB, JWT, Cloudinary y Supabase si vas a probar funcionalidades completas.

### Pasos

1. Entra en la raíz del proyecto, donde está `pom.xml`.

```bash
cd DentalPlus_Backend
```

2. Configura variables de entorno.

3. Ejecuta la aplicación.

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```bash
mvnw.cmd spring-boot:run
```

4. La API queda disponible por defecto en:

```text
http://localhost:8080
```

### Build manual

```bash
./mvnw clean package
```

Ejecutar el `.jar` generado:

```bash
java -jar target/DentalPlus_Backend-0.0.1-SNAPSHOT.jar
```

---

## Render y despliegue

La colección Postman y el seed muestran esta URL base de Render:

```text
https://dentalplus-backend.onrender.com
```

El proyecto incluye un `Dockerfile`:

```dockerfile
FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/DentalPlus_Backend-0.0.1-SNAPSHOT.jar"]
```

### Consideraciones para Render

- Render suele inyectar la variable `PORT`.
- `application.properties` usa `server.port=${PORT:8080}`.
- Configura credenciales en el panel de Render como variables de entorno.
- No dependas de valores sensibles por defecto en `application.properties`.
- Si cambias la URL de Render, actualiza:
  - `postman/DentalPlus_Postman.json`
  - este README
  - frontend, si lo consume
  - seed, si imprime URLs por consola

---

## Base de datos

El proyecto usa MySQL mediante JPA/Hibernate.

Configuración recomendada para desarrollo normal:

```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
```

Durante pruebas de rendimiento o estrés, `spring.jpa.show-sql` debe estar en `false`. Si está en `true`, Hibernate imprime cada consulta SQL en consola y ralentiza mucho la carga masiva de datos.

### Qué significa `ddl-auto=update`

Hibernate intenta actualizar el esquema automáticamente según las entidades Java. Es cómodo en desarrollo, pero puede ser peligroso en producción.

Riesgos:

- cambios automáticos no revisados;
- columnas o relaciones modificadas sin migración formal;
- diferencias entre local, Render y DB externa;
- errores al arrancar si la estructura real no encaja con las entidades.

Para producción convendría valorar:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

o usar migraciones controladas con Flyway/Liquibase.

### Desincronización entre entidades Java y esquema MySQL

Si el proyecto usa una base de datos externa que ya existía antes, puede aparecer una diferencia entre las entidades Java actuales y las columnas reales de MySQL. Un caso real detectado al ejecutar el seed fue:

```text
Field 'city' doesn't have a default value
```

El error ocurrió al insertar un `Dentist` porque la tabla `dentist` tenía una columna antigua `city` marcada como `NOT NULL`, pero la entidad `Dentist` actual no tiene ese campo. En el diseño actual, datos como ciudad, teléfono o email pertenecen a `Person`, no a `Dentist`.

Para diagnosticarlo:

```sql
SHOW COLUMNS FROM dentist;
```

O de forma más detallada:

```sql
SELECT COLUMN_NAME, IS_NULLABLE, COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'dentist';
```

Si aparece una columna antigua que ya no existe en la entidad Java y además es obligatoria, el seed puede fallar aunque las tablas estén vacías. `TRUNCATE` borra datos, pero no corrige la estructura.

Solución recomendada para el caso anterior:

```sql
ALTER TABLE dentist DROP COLUMN city;
```

Alternativa temporal, menos limpia:

```sql
ALTER TABLE dentist MODIFY city VARCHAR(100) NULL;
```

Después de corregir el esquema, vuelve a ejecutar el seed desde el principio, porque el proceso pudo haber dejado la base parcialmente truncada.

---

## Autenticación JWT

El login se hace con:

```http
POST /user/login
```

Body:

```json
{
  "identifier": "admin@dentalplus.demo",
  "password": "Password123"
}
```

Respuesta esperada:

```json
{
  "token": "eyJ...",
  "userId": 1,
  "profile": {
    "id": 1,
    "username": "admin",
    "active": true,
    "person": {
      "name": "Admin",
      "email": "admin@dentalplus.demo"
    },
    "roles": [
      {
        "roleType": "ADMIN",
        "roleId": 1,
        "clinicId": 1,
        "clinicName": "...",
        "active": true
      }
    ]
  }
}
```

Después del login, cada request protegida debe enviar:

```http
Authorization: Bearer <token>
```

### Cómo usar el token en frontend

Ejemplo con `fetch`:

```js
const token = localStorage.getItem("authToken");

const response = await fetch(`${baseUrl}/patient`, {
  method: "GET",
  headers: {
    "Authorization": `Bearer ${token}`,
    "Content-Type": "application/json"
  }
});
```

### Cómo usar el token en Postman

1. Ejecuta `UserController / Login`.
2. Si la respuesta contiene el token, la colección lo guarda automáticamente en `authToken`.
3. Ejecuta el resto de endpoints protegidos.

También puedes usar autorización tipo `Bearer Token` o header manual:

```http
Authorization: Bearer {{authToken}}
```

### Detalles técnicos detectados

- El token se genera con claves RSA.
- El `subject` del token es el `userId`.
- La expiración se configura con `AUTH_TOKEN_EXPIRATION_MS`.
- `/user/login` es público.
- El resto de endpoints requiere autenticación salvo `OPTIONS /**` y `/error`.

---

## Roles y permisos

Roles detectados:

| Rol lógico | Authority Spring Security |
|---|---|
| `ADMIN` | `ROLE_ADMIN` |
| `DENTIST` | `ROLE_DENTIST` |
| `RECEPTIONIST` | `ROLE_RECEPTIONIST` |
| `PATIENT` | `ROLE_PATIENT` |

Las tablas/modelos relacionados son:

- `Admin`
- `Dentist`
- `Receptionist`
- `Patient`
- `User`
- `Person`
- `Clinic`
- `Organization`

### Importante sobre permisos

No se han detectado reglas de permisos centralizadas con `@PreAuthorize` en los controladores. La mayoría de restricciones se aplican dentro de los services, por ejemplo comprobando:

- usuario autenticado;
- clínica del usuario;
- clínica del paciente;
- si el recurso pertenece a la misma clínica;
- si el usuario puede modificar odontogramas, citas o documentos.

Por eso, cuando cambies permisos, revisa los servicios implicados, no solo `SecurityConfig`.

### Estado actual de pacientes con login

El código contempla `ROLE_PATIENT`, pero el seed indica que los pacientes demo **no tienen login habilitado**. Esto puede ser una funcionalidad futura o parcial. Si se habilita login de pacientes, revisa seguridad, DTOs, seed, Postman y frontend.

---

## Servicios externos: Cloudinary y Supabase

### Cloudinary: imágenes de perfil

Clases principales:

- `CloudinaryConfig`
- `CloudinaryService`
- `UserController`
- `UserService`
- `Person`
- `ProfileDto`

Uso detectado:

- Subir imagen de perfil al actualizar `/user/me` con `multipart/form-data`.
- Eliminar imagen con `removeProfileImage=true`.
- Carpeta usada: `dentalplus/profile-images`.
- Tipos permitidos:
  - `image/jpeg`
  - `image/png`
  - `image/webp`
- Tamaño máximo: `10 MB`.

### Supabase Storage: documentos PDF

Clases principales:

- `SupabaseConfig`
- `SupabaseStorageService`
- `DocumentController`
- `DocumentService`
- `Document`
- `DocumentDto`

Uso detectado:

- Subida de PDFs de pacientes.
- Eliminación de PDFs.
- URL pública del documento en `DocumentDto.url`.
- Bucket por defecto: `documents`.

Tipos de documento válidos detectados:

```text
CONSENT
XRAY
REPORT
PRESCRIPTION
OTHER
```

> **Privacidad**  
> Los documentos clínicos son información sensible. Revisa si el bucket de Supabase debe ser público o privado. Si el bucket es público, cualquiera con la URL puede acceder al documento.

---

## Endpoints disponibles

Base URL local:

```text
http://localhost:8080
```

Base URL Render detectada:

```text
https://dentalplus-backend.onrender.com
```

Todos los endpoints, excepto `/user/login`, requieren:

```http
Authorization: Bearer <token>
```

### UserController

| Método | Endpoint | Body | Respuesta | Descripción |
|---|---|---|---|---|
| `POST` | `/user/login` | `LoginRequest` | `LoginResponse` | Inicia sesión y devuelve JWT. |
| `GET` | `/user/me` | No | `ProfileDto` | Devuelve el perfil autenticado. |
| `PUT` | `/user/me` | `ProfileDto` JSON | `ProfileDto` | Actualiza perfil sin imagen. |
| `PUT` | `/user/me` | multipart | `ProfileDto` | Actualiza perfil con imagen o elimina imagen. |

### PatientController

| Método | Endpoint | Query/body | Respuesta | Descripción |
|---|---|---|---|---|
| `GET` | `/patient` | `search` opcional | `List<PatientDto>` | Lista pacientes visibles. |
| `GET` | `/patient/{id}` | No | `PatientDto` | Obtiene paciente por ID. |
| `POST` | `/patient` | `PatientDto` | `PatientDto` | Crea paciente. |
| `PUT` | `/patient/{id}` | `PatientDto` | `PatientDto` | Actualiza paciente. |

No se ha detectado endpoint `DELETE /patient/{id}`.

### AppointmentController

| Método | Endpoint | Query/body | Respuesta | Descripción |
|---|---|---|---|---|
| `GET` | `/appointment` | `date`, `patientId`, `dentistId`, `boxId` opcionales | `List<AppointmentDto>` | Lista citas filtrables. |
| `GET` | `/appointment/{id}` | No | `AppointmentDto` | Obtiene cita por ID. |
| `POST` | `/appointment` | `AppointmentDto` | `AppointmentDto` | Crea cita. |
| `PUT` | `/appointment/{id}` | `AppointmentDto` | `AppointmentDto` | Actualiza cita. |
| `DELETE` | `/appointment/{id}` | No | texto | Elimina/desactiva cita. |
| `GET` | `/appointment/availability` | `date`, `time` opcional | `AvailabilityDto` | Consulta disponibilidad. |

Estados de cita válidos detectados:

```text
SCHEDULED
COMPLETED
CANCELLED
```

### DocumentController

| Método | Endpoint | Body | Respuesta | Descripción |
|---|---|---|---|---|
| `GET` | `/document/patient/{patientId}` | No | `List<DocumentDto>` | Lista documentos del paciente. |
| `POST` | `/document/patient/{patientId}` | multipart | `DocumentDto` | Sube PDF. |
| `DELETE` | `/document/{id}` | No | texto | Elimina documento. |

Multipart para subida de documento:

| Campo | Tipo | Obligatorio |
|---|---|---|
| `file` | archivo PDF | Sí |
| `name` | texto | Sí |
| `documentType` | texto | Sí |
| `notes` | texto | No |

### OdontogramController

El controller base es `/patient`.

| Método | Endpoint | Body | Descripción |
|---|---|---|---|
| `POST` | `/patient/{patientId}/odontogram` | No | Crea odontograma para paciente. |
| `GET` | `/patient/{patientId}/odontogram` | No | Obtiene odontograma por paciente. |
| `GET` | `/patient/odontogram/{odontogramId}` | No | Obtiene odontograma por ID. |
| `PUT` | `/patient/{patientId}/odontogram/view-mode` | `{ "viewMode": "MIXED" }` | Cambia modo por paciente. |
| `PUT` | `/patient/odontogram/{odontogramId}/view-mode` | `{ "viewMode": "MIXED" }` | Cambia modo por odontograma. |
| `GET` | `/patient/{patientId}/odontogram/piece/{pieceNumber}` | No | Obtiene pieza por paciente. |
| `GET` | `/patient/odontogram/{odontogramId}/piece/{pieceNumber}` | No | Obtiene pieza por odontograma. |
| `GET` | `/patient/{patientId}/odontogram/piece/{pieceNumber}/state` | No | Lista estados de pieza. |
| `GET` | `/patient/odontogram/{odontogramId}/piece/{pieceNumber}/state` | No | Lista estados de pieza por odontograma. |
| `POST` | `/patient/{patientId}/odontogram/piece/{pieceNumber}/state` | `DentalPieceStateDto` | Crea estado de pieza. |
| `POST` | `/patient/odontogram/{odontogramId}/piece/{pieceNumber}/state` | `DentalPieceStateDto` | Crea estado por odontograma. |
| `GET` | `/patient/{patientId}/odontogram/piece/{pieceNumber}/surface/{surfaceType}` | No | Obtiene superficie. |
| `GET` | `/patient/odontogram/{odontogramId}/piece/{pieceNumber}/surface/{surfaceType}` | No | Obtiene superficie por odontograma. |
| `GET` | `/patient/{patientId}/odontogram/piece/{pieceNumber}/surface/{surfaceType}/mark` | No | Lista marcas de superficie. |
| `GET` | `/patient/odontogram/{odontogramId}/piece/{pieceNumber}/surface/{surfaceType}/mark` | No | Lista marcas por odontograma. |
| `POST` | `/patient/{patientId}/odontogram/piece/{pieceNumber}/surface/{surfaceType}/mark` | `DentalSurfaceMarkDto` | Crea marca. |
| `POST` | `/patient/odontogram/{odontogramId}/piece/{pieceNumber}/surface/{surfaceType}/mark` | `DentalSurfaceMarkDto` | Crea marca por odontograma. |
| `PUT` | `/patient/odontogram/mark/{markId}` | `DentalSurfaceMarkDto` | Actualiza marca. |
| `DELETE` | `/patient/odontogram/mark/{markId}` | No | Desactiva marca. |
| `GET` | `/patient/{patientId}/odontogram/bridge` | No | Lista puentes por paciente. |
| `GET` | `/patient/odontogram/{odontogramId}/bridge` | No | Lista puentes por odontograma. |
| `POST` | `/patient/{patientId}/odontogram/bridge` | `DentalBridgeDto` | Crea puente. |
| `POST` | `/patient/odontogram/{odontogramId}/bridge` | `DentalBridgeDto` | Crea puente por odontograma. |
| `PUT` | `/patient/odontogram/bridge/{bridgeId}` | `DentalBridgeDto` | Actualiza puente. |
| `DELETE` | `/patient/odontogram/bridge/{bridgeId}` | No | Desactiva puente. |

---

## Ejemplos de requests y responses

### Login

Request:

```http
POST /user/login
Content-Type: application/json
```

```json
{
  "identifier": "admin@dentalplus.demo",
  "password": "Password123"
}
```

Response orientativa:

```json
{
  "token": "eyJ...",
  "userId": 1,
  "profile": {
    "id": 1,
    "username": "admin",
    "active": true,
    "themePreference": "SYSTEM",
    "languagePreference": "EN",
    "person": {
      "id": 1,
      "name": "Admin",
      "firstSurname": "User",
      "email": "admin@dentalplus.demo"
    },
    "roles": [
      {
        "roleType": "ADMIN",
        "roleId": 1,
        "clinicId": 1,
        "clinicName": "DentalPlus Clinic",
        "active": true
      }
    ]
  }
}
```

### Crear paciente

```http
POST /patient
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "active": true,
  "notes": "Created from Postman",
  "person": {
    "name": "Test",
    "firstSurname": "Patient",
    "secondSurname": "Sample",
    "birthDate": "1990-01-01",
    "gender": "OTHER",
    "email": "test.patient@example.com",
    "phonePrefix": "+1",
    "phoneNumber": "0000000000",
    "address": "Test Address",
    "city": "Test City",
    "notes": "Patient created from Postman"
  }
}
```

Response orientativa:

```json
{
  "patientId": 1,
  "userId": null,
  "clinicId": 1,
  "clinicName": "DentalPlus Clinic",
  "registrationDate": "2026-05-02",
  "active": true,
  "notes": "Created from Postman",
  "person": {
    "id": 10,
    "name": "Test",
    "firstSurname": "Patient",
    "secondSurname": "Sample",
    "birthDate": "1990-01-01",
    "gender": "OTHER",
    "email": "test.patient@example.com"
  },
  "documents": []
}
```

### Crear cita

```http
POST /appointment
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "boxId": 1,
  "dentistId": 1,
  "patientId": 1,
  "treatment": "Dental cleaning",
  "startDateTime": "2026-06-01T10:00:00",
  "endDateTime": "2026-06-01T10:30:00",
  "status": "SCHEDULED",
  "notes": "Routine appointment created from Postman",
  "active": true
}
```

### Consultar disponibilidad

```http
GET /appointment/availability?date=2026-06-01&time=10:00
Authorization: Bearer <token>
```

Response orientativa según `AvailabilityDto`:

```json
{
  "dentists": [
    {
      "id": 1,
      "fullName": "Dentist Name",
      "speciality": "General dentistry"
    }
  ],
  "boxes": [
    {
      "id": 1,
      "name": "Box 1"
    }
  ]
}
```

### Subir documento PDF

```http
POST /document/patient/1
Authorization: Bearer <token>
Content-Type: multipart/form-data
```

Campos:

```text
file: archivo PDF
name: General consent document
documentType: CONSENT
notes: Uploaded from Postman
```

Response orientativa:

```json
{
  "id": 1,
  "patientId": 1,
  "name": "General consent document",
  "storagePath": "...",
  "url": "https://...",
  "mimeType": "application/pdf",
  "documentType": "CONSENT",
  "active": true,
  "notes": "Uploaded from Postman"
}
```

### Actualizar perfil con multipart

```http
PUT /user/me
Authorization: Bearer <token>
Content-Type: multipart/form-data
```

Campos:

```text
profile
profileImage
removeProfileImage
```

Ejemplo de `profile`:

```json
{
  "username": "admin",
  "themePreference": "SYSTEM",
  "languagePreference": "EN",
  "person": {
    "name": "Test",
    "firstSurname": "User",
    "secondSurname": "Sample",
    "email": "admin@dentalplus.demo",
    "phonePrefix": "+1",
    "phoneNumber": "0000000000",
    "address": "Test Address",
    "city": "Test City",
    "notes": "Updated from Postman"
  }
}
```

### Crear estado de pieza dental

```http
POST /patient/1/odontogram/piece/11/state
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "stateType": "HEALTHY",
  "notes": "Created from Postman"
}
```

### Crear marca en superficie dental

```http
POST /patient/1/odontogram/piece/11/surface/MESIAL/mark
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "markType": "CARIES",
  "markState": "PENDING",
  "notes": "Created from Postman"
}
```

### Crear puente dental

```http
POST /patient/1/odontogram/bridge
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "bridgeState": "PENDING",
  "notes": "Created from Postman",
  "pieces": [
    {
      "pieceNumber": 14,
      "pieceRole": "ABUTMENT"
    },
    {
      "pieceNumber": 15,
      "pieceRole": "PONTIC"
    },
    {
      "pieceNumber": 16,
      "pieceRole": "ABUTMENT"
    }
  ]
}
```

---

## DTOs principales

### `LoginRequest`

| Campo | Tipo |
|---|---|
| `identifier` | `String` |
| `password` | `String` |

### `LoginResponse`

| Campo | Tipo |
|---|---|
| `token` | `String` |
| `userId` | `Long` |
| `profile` | `ProfileDto` |

### `ProfileDto`

| Campo | Tipo |
|---|---|
| `id` | `Long` |
| `username` | `String` |
| `active` | `Boolean` |
| `themePreference` | `String` |
| `languagePreference` | `String` |
| `notes` | `String` |
| `person` | `PersonDto` |
| `roles` | `List<RoleDto>` |
| `weeklyCalendar` | `WeeklyCalendarDto` |

### `PersonDto`

| Campo | Tipo |
|---|---|
| `id` | `Long` |
| `name` | `String` |
| `firstSurname` | `String` |
| `secondSurname` | `String` |
| `birthDate` | `LocalDate` |
| `gender` | `String` |
| `email` | `String` |
| `phonePrefix` | `String` |
| `phoneNumber` | `String` |
| `address` | `String` |
| `city` | `String` |
| `profileImage` | `String` |
| `notes` | `String` |

### `PatientDto`

| Campo | Tipo |
|---|---|
| `patientId` | `Long` |
| `userId` | `Long` |
| `clinicId` | `Long` |
| `clinicName` | `String` |
| `registrationDate` | `LocalDate` |
| `active` | `Boolean` |
| `medicalAlert` | `String` |
| `notes` | `String` |
| `person` | `PersonDto` |
| `documents` | `List<DocumentDto>` |

### `AppointmentDto`

| Campo | Tipo |
|---|---|
| `id` | `Long` |
| `boxId` | `Long` |
| `boxName` | `String` |
| `dentistId` | `Long` |
| `dentistName` | `String` |
| `patientId` | `Long` |
| `patientName` | `String` |
| `treatment` | `String` |
| `startDateTime` | `LocalDateTime` |
| `endDateTime` | `LocalDateTime` |
| `status` | `String` |
| `notes` | `String` |
| `active` | `Boolean` |

### `DocumentDto`

| Campo | Tipo |
|---|---|
| `id` | `Long` |
| `patientId` | `Long` |
| `name` | `String` |
| `storagePath` | `String` |
| `url` | `String` |
| `mimeType` | `String` |
| `documentType` | `String` |
| `active` | `Boolean` |
| `notes` | `String` |

---

## Odontograma

El odontograma es el módulo más específico del dominio dental.

Clases principales:

- `Odontogram`
- `DentalPiece`
- `DentalPieceState`
- `DentalSurface`
- `DentalSurfaceMark`
- `DentalBridge`
- `DentalBridgePiece`

Services principales:

- `OdontogramService`
- `DentalPieceService`
- `DentalSurfaceService`
- `DentalBridgeService`

DTOs principales:

- `OdontogramDto`
- `DentalPieceDto`
- `DentalPieceStateDto`
- `DentalSurfaceDto`
- `DentalSurfaceMarkDto`
- `DentalBridgeDto`
- `DentalBridgePieceDto`

### Valores válidos detectados

#### `viewMode`

```text
TEMPORARY
PERMANENT
MIXED
```

#### `surfaceType`

```text
MESIAL
DISTAL
VESTIBULAR
LINGUAL
OCCLUSAL
```

Nota: `OCCLUSAL` solo aplica a piezas posteriores según la validación de `DentalSurface`. El modelo actual no usa `PALATAL`; para esa zona se utiliza `LINGUAL`.

#### `stateType`

```text
HEALTHY
NATURAL_ABSENCE
EXTRACTION_PENDING
EXTRACTION_DONE
CROWN_PENDING
CROWN_DONE
ENDODONTICS_PENDING
ENDODONTICS_DONE
BRIDGE_PENDING
BRIDGE_DONE
FILLING_PENDING
FILLING_DONE
PERIODONTAL_TREATMENT_PENDING
PERIODONTAL_TREATMENT_DONE
UNKNOWN
```

#### `markType`

```text
CARIES
FILLING
RADIOGRAPH_CARIES
FISSURE_SEALANT
EXTRACTION
CROWN
ENDODONTICS
BRIDGE
NATURAL_ABSENCE
```

#### `markState`

```text
PENDING
DONE
NATURAL
```

#### `bridgeState`

```text
PENDING
DONE
```

#### `pieceRole`

```text
ABUTMENT
PONTIC
```

### Funcionamiento general

1. Se crea un odontograma para un paciente.
2. El odontograma contiene piezas dentales.
3. Cada pieza puede tener un estado activo y un historial de estados.
4. Cada pieza tiene superficies.
5. Cada superficie puede tener marcas activas o históricas.
6. Un puente agrupa varias piezas, normalmente con pilares (`ABUTMENT`) y pónticos (`PONTIC`).
7. Las operaciones comprueban que el usuario autenticado tenga acceso a la clínica/paciente correspondiente.

---

## Seed de datos

El seed está en:

```text
src/main/java/com/example/DentalPlus_Backend/seed/ApplicationSeed.java
```

También participa:

```text
src/main/java/com/example/DentalPlus_Backend/seed/StressSeed.java
```

Recursos usados:

```text
src/main/resources/seed/general-consent.pdf
src/main/resources/seed/treatment-plan.pdf
src/main/resources/seed/profile-images/
```

### Advertencia

> **El seed es destructivo.**  
> Borra datos de base de datos y también intenta eliminar archivos externos registrados en Cloudinary/Supabase.

Antes de ejecutarlo, revisa que estás conectado a una base de datos de desarrollo o demo, nunca a producción.

### Ejecución

`ApplicationSeed` tiene método `main`, por lo que puede ejecutarse desde el IDE como clase Java.

La forma recomendada actualmente es ejecutar `ApplicationSeed` desde el IDE usando su método `main`, ya que el seed pide confirmación interactiva por consola.

### Confirmación requerida

El seed pide escribir exactamente:

```text
SEED
```

Después permite elegir el modo de carga:

```text
1. DEMO - small realistic dataset for Postman/frontend
2. PERFORMANCE_LIGHT - demo + controlled performance data
3. STRESS - demo + performance + massive stress data
```

Al final pregunta si quieres mantener los datos generados o limpiar la base.

### Modos disponibles

| Modo | Descripción |
|---|---|
| `DEMO` | Carga un dataset pequeño y realista para probar frontend/Postman. |
| `PERFORMANCE_LIGHT` | Carga demo + datos controlados para pruebas de rendimiento. |
| `STRESS` | Carga demo + rendimiento + datos masivos para prueba de estrés. |

Todos los modos pueden subir imágenes de perfil y documentos PDF si los recursos existen en `src/main/resources/seed`.

### Datos generados por modo

#### DEMO

Incluye datos base:

- organización;
- clínica;
- boxes;
- administradores;
- recepcionistas;
- dentistas;
- pacientes demo;
- citas demo;
- odontogramas demo;
- marcas dentales;
- documentos PDF;
- imágenes de perfil.

#### PERFORMANCE_LIGHT

Añade al modo demo:

| Tipo | Cantidad extra |
|---|---:|
| Admins | 2 |
| Recepcionistas | 3 |
| Dentistas | 6 |
| Pacientes | 30 |
| Odontogramas | 4 |
| Citas | 80 |

#### STRESS

Añade al modo performance:

| Tipo | Cantidad extra |
|---|---:|
| Pacientes stress | 1000 |
| Odontogramas stress | 50 |
| Citas stress | 5000 |

Además, en la prueba actual se suben:

| Archivo externo | Cantidad esperada |
|---|---:|
| Imágenes de perfil | 1049 |
| PDFs de pacientes | 2068 |

Cada paciente recibe dos documentos PDF:

- `General Consent Document`
- `Treatment Plan Document`

### Usuarios demo

| Usuario | Contraseña |
|---|---|
| `admin@dentalplus.demo` | `Password123` |
| `reception@dentalplus.demo` | `Password123` |
| `dentist.general@dentalplus.demo` | `Password123` |
| `dentist.surgery@dentalplus.demo` | `Password123` |

Usuarios de rendimiento disponibles:

| Usuario | Contraseña |
|---|---|
| `admin.perf.01@example.com` | `Password123` |
| `receptionist.perf.01@example.com` | `Password123` |
| `dentist.perf.01@example.com` | `Password123` |

El seed indica que los pacientes demo no tienen login habilitado.

### IDs por defecto para Postman

La colección Postman usa directamente los IDs base generados por el seed:

| Valor | ID usado |
|---|---:|
| Paciente base | `1` |
| Odontograma base | `1` |
| Dentista base | `1` |
| Box base | `1` |
| Cita base | `1` |
| Documento base | `1` |
| Pieza dental de ejemplo | `11` |
| Superficie de ejemplo | `MESIAL` |

No se usan variables de colección para estos IDs porque el objetivo es que la colección funcione rápido después de ejecutar el seed.

### Resultado real de la prueba de estrés

En la última ejecución completa del modo `STRESS`, el sistema generó correctamente:

| Métrica | Resultado |
|---|---:|
| Usuarios | 15 |
| Personas | 1049 |
| Admins | 3 |
| Recepcionistas | 4 |
| Dentistas | 8 |
| Pacientes | 1034 |
| Citas | 5083 |
| Odontogramas | 58 |
| Piezas dentales | 3016 |
| Estados de piezas | 3069 |
| Superficies dentales | 13688 |
| Marcas dentales | 112 |
| Documentos PDF registrados | 2067 |
| Filas controladas aproximadas | 29206 |

También se completaron los diagnósticos funcionales y de rendimiento:

| Diagnóstico | Resultado aproximado |
|---|---:|
| Login admin | 749 ms |
| Login recepcionista | 855 ms |
| Login dentista | 713 ms |
| Listado de citas admin | 591 ms |
| Disponibilidad admin | 1534 ms |
| Odontograma base admin | 3180 ms |
| Odontograma performance admin | 3029 ms |
| Odontograma stress inicial | 3390 ms |
| Odontograma stress intermedio | 3162 ms |
| Odontograma stress final | 3121 ms |
| Listado de citas stress por fecha | 407 ms |

La prueba se considera satisfactoria porque el seed masivo finalizó, los datos principales fueron persistidos, las imágenes de perfil se subieron correctamente y los diagnósticos de acceso siguieron funcionando con el volumen generado.

Observación: de 2068 PDFs esperados, se registraron 2067. Hubo un fallo puntual de subida a Supabase para un documento, pero el proceso continuó correctamente. Esto debe mencionarse como incidencia menor de almacenamiento externo, no como fallo general de la base de datos ni de la prueba de estrés.

---

## Postman

La colección está en:

```text
postman/DentalPlus_Postman.json
```

Variables detectadas:

| Variable | Valor/uso |
|---|---|
| `baseUrl` | URL activa usada por requests. |
| `baseUrlLocal` | `http://localhost:8080` |
| `baseUrlLan` | `http://YOUR_LOCAL_IP:8080` |
| `baseUrlRender` | `https://dentalplus-backend.onrender.com` |
| `authToken` | JWT obtenido en login. |

### Flujo recomendado

1. Importa `postman/DentalPlus_Postman.json`.
2. Ajusta `baseUrl`:
   - local: `http://localhost:8080`
   - Render: `https://dentalplus-backend.onrender.com`
3. Ejecuta `UserController / Login`.
4. Si la respuesta contiene el token, la colección lo guarda automáticamente en `authToken`.
5. Ejecuta el resto de endpoints protegidos.

La colección no usa variables para IDs fijos como `patientId`, `odontogramId`, `dentistId` o `boxId`. Los endpoints de ejemplo usan directamente los IDs generados por el seed, normalmente `1`, para facilitar pruebas rápidas.

Si cambias endpoints, DTOs o ejemplos, actualiza también esta colección.

---

## Tests

Test principal detectado:

```text
src/test/java/com/example/DentalPlus_Backend/ApplicationTest.java
```

Configuración de test:

```text
src/test/resources/application-test.properties
```

El test cubre diagnósticos como:

- carga de contexto Spring;
- existencia de controladores y servicios críticos;
- endpoints protegidos sin token;
- login inválido;
- validaciones de usuario/persona;
- validaciones de citas/calendario;
- validaciones de odontograma;
- validaciones de documentos;
- validaciones de entidades principales.

Ejecutar tests:

```bash
./mvnw test
```

> Si los tests fallan por base de datos o configuración externa, revisa `application-test.properties` y las variables necesarias. No asumas que un fallo de test es solo del código: puede ser configuración.

---

## Cómo modificar partes importantes

### Usuario, login y perfil

Revisar:

```text
controller/UserController.java
service/UserService.java
service/JwtService.java
config/SecurityConfig.java
config/JwtConfig.java
dto/LoginRequest.java
dto/LoginResponse.java
dto/ProfileDto.java
dto/PersonDto.java
model/User.java
model/Person.java
```

Si cambias login o perfil:

- actualiza Postman;
- actualiza ejemplos del README;
- revisa frontend;
- revisa tests de autenticación;
- revisa JWT si cambia el identificador del usuario.

### Roles y permisos

Revisar:

```text
model/Admin.java
model/Dentist.java
model/Receptionist.java
model/Patient.java
service/AdminService.java
service/DentistService.java
service/ReceptionistService.java
service/PatientService.java
service/UserService.java
config/SecurityConfig.java
```

Antes de cambiar permisos, busca validaciones por `callerUserId`, clínica y rol dentro de los services.

### Pacientes

Revisar:

```text
controller/PatientController.java
service/PatientService.java
dao/PatientDao.java
daoImplHibernate/PatientDaoImplHibernate.java
dto/PatientDto.java
dto/PersonDto.java
model/Patient.java
model/Person.java
```

Si cambia `PatientDto`, actualiza Postman y frontend.

### Citas y disponibilidad

Revisar:

```text
controller/AppointmentController.java
service/AppointmentService.java
service/CalendarService.java
dto/AppointmentDto.java
dto/AvailabilityDto.java
model/Appointment.java
model/Box.java
model/CalendarRule.java
model/CalendarBreak.java
model/CalendarException.java
model/CalendarHoliday.java
```

Cuida especialmente:

- solapamientos de horarios;
- disponibilidad de box;
- disponibilidad de dentista;
- citas fuera de horario;
- estado `SCHEDULED`, `COMPLETED`, `CANCELLED`.

### Odontograma, piezas, superficies, marcas y puentes

Revisar:

```text
controller/OdontogramController.java
service/OdontogramService.java
service/DentalPieceService.java
service/DentalSurfaceService.java
service/DentalBridgeService.java
model/Odontogram.java
model/DentalPiece.java
model/DentalPieceState.java
model/DentalSurface.java
model/DentalSurfaceMark.java
model/DentalBridge.java
model/DentalBridgePiece.java
dto/OdontogramDto.java
dto/DentalPieceDto.java
dto/DentalPieceStateDto.java
dto/DentalSurfaceDto.java
dto/DentalSurfaceMarkDto.java
dto/DentalBridgeDto.java
dto/DentalBridgePieceDto.java
```

Si se cambia odontograma:

- revisar seed;
- revisar Postman;
- revisar tests;
- revisar diagramas;
- revisar validaciones de piezas y superficies.

### Documentos

Revisar:

```text
controller/DocumentController.java
service/DocumentService.java
service/SupabaseStorageService.java
dto/DocumentDto.java
model/Document.java
config/SupabaseConfig.java
```

Si cambias subida de documentos:

- mantener multipart compatible;
- revisar tipos permitidos;
- revisar Supabase;
- revisar privacidad de URLs;
- actualizar Postman.

### Imágenes de perfil

Revisar:

```text
service/CloudinaryService.java
config/CloudinaryConfig.java
controller/UserController.java
service/UserService.java
model/Person.java
dto/ProfileDto.java
```

Si cambias imágenes:

- revisar tipos permitidos;
- revisar tamaño máximo;
- revisar eliminación anterior;
- revisar `removeProfileImage`;
- actualizar ejemplos multipart.

### Seed

Revisar:

```text
seed/ApplicationSeed.java
seed/StressSeed.java
src/main/resources/seed/
postman/DentalPlus_Postman.json
```

Si cambia el seed:

- actualizar usuarios demo;
- actualizar IDs esperados;
- actualizar ejemplos Postman;
- actualizar README;
- revisar limpieza de Cloudinary/Supabase;
- revisar métricas de rendimiento y estrés.

---

## Errores comunes y depuración

### `401 Unauthorized`

Causas probables:

- No se envió header `Authorization`.
- El token no empieza por `Bearer `.
- Token caducado.
- Token firmado con otra clave.
- Clave pública/privada mal configurada.

Solución:

1. Rehacer login.
2. Copiar token completo o dejar que Postman lo guarde automáticamente.
3. Enviar:

```http
Authorization: Bearer <token>
```

### `403 Forbidden`

Causas probables:

- Usuario autenticado pero sin permisos funcionales.
- Recurso de otra clínica.
- Usuario inactivo.
- Rol no esperado.

Solución:

- Revisar `UserService` y services del dominio afectado.
- Comprobar roles devueltos por `/user/me`.

### Error del seed: `Field 'city' doesn't have a default value`

Este error indica que la base de datos tiene una columna obligatoria que no existe en la entidad Java actual.

Ejemplo real:

```text
Field 'city' doesn't have a default value
insert into dentist (...)
```

Causa probable:

- la tabla `dentist` conserva una columna antigua `city`;
- esa columna está como `NOT NULL`;
- la entidad `Dentist` actual no la envía al insertar;
- `ddl-auto=update` no siempre elimina columnas obsoletas.

Comprobación:

```sql
SHOW COLUMNS FROM dentist;
```

Solución recomendada si `city` es una columna antigua:

```sql
ALTER TABLE dentist DROP COLUMN city;
```

Luego vuelve a ejecutar el seed completo.

> Antes de borrar columnas, confirma que estás trabajando en una base de desarrollo o demo y no en producción.

### Error del seed: `Seed dental surface not found`

Ejemplo:

```text
Seed dental surface not found: 12 PALATAL
```

Causa:

- Se intentó crear una marca dental sobre una superficie que no existe para esa pieza.
- En el modelo actual las superficies válidas son `MESIAL`, `DISTAL`, `VESTIBULAR`, `LINGUAL` y `OCCLUSAL`.
- Para piezas anteriores no se debe usar `OCCLUSAL`.
- `PALATAL` no está contemplado en el modelo actual; se usa `LINGUAL`.

Solución:

- Cambiar `PALATAL` por `LINGUAL`.
- Verificar que la pieza tenga esa superficie.
- Volver a ejecutar el seed completo.

### Error de JSON mal construido

Causas probables:

- Falta una coma.
- Fecha con formato incorrecto.
- Campo mal escrito.
- Se envía multipart cuando el endpoint espera JSON, o al revés.

Formatos usados:

```text
LocalDate: 2026-06-01
LocalTime: 10:00
LocalDateTime: 2026-06-01T10:00:00
```

### Error multipart en perfil

Para `PUT /user/me` multipart, el campo `profile` debe ser JSON y el archivo debe ir en `profileImage`.

Campos:

```text
profile
profileImage
removeProfileImage
```

### Error multipart en documentos

Para subir PDF:

```text
file
name
documentType
notes
```

El archivo debe ser PDF.

### Error Cloudinary

Causas probables:

- `CLOUDINARY_URL` no configurada.
- Archivo demasiado grande.
- Tipo no permitido.
- Credenciales inválidas.

### Error Supabase

Causas probables:

- `SUPABASE_URL` incorrecta.
- `SUPABASE_KEY` inválida.
- Bucket inexistente.
- Permisos insuficientes.
- Archivo no PDF.
- Fallo puntual de red o almacenamiento externo durante cargas masivas.

En la prueba de estrés se registró una incidencia menor: 2067 PDFs subidos de 2068 esperados. El proceso continuó correctamente y no afectó al resto del seed.

### Error de base de datos

Causas probables:

- URL JDBC incorrecta.
- Credenciales incorrectas.
- DB apagada o inaccesible.
- Esquema incompatible.
- `ddl-auto=update` produjo cambios inesperados.

### Seed borra datos inesperados

El seed usa la misma configuración que la app normal. Si apunta a una DB externa real, puede borrar datos. Revisa variables antes de ejecutarlo.

### La prueba de estrés tarda demasiado

Causas probables:

- Base de datos remota lenta.
- `spring.jpa.show-sql=true`.
- Subida masiva de imágenes a Cloudinary.
- Subida masiva de PDFs a Supabase.
- Muchos odontogramas, piezas y superficies creados individualmente.
- Pool de conexiones demasiado pequeño.

Recomendaciones:

- Mantener `spring.jpa.show-sql=false`.
- Ejecutar contra base de datos de desarrollo.
- No cortar el proceso si ya está subiendo archivos y no hay error.
- Revisar los tiempos `[PERF]` que imprime el seed para identificar el cuello de botella.

---

## Diagramas Mermaid

### Arquitectura general

```mermaid
flowchart LR
    Client[Frontend / Postman] --> Controller[Controllers REST]
    Controller --> Service[Services]
    Service --> DAO[DAO interfaces]
    DAO --> Impl[DAO Impl Hibernate]
    Impl --> DB[(MySQL)]
    Service --> DTO[DTOs]
    Service --> Cloudinary[Cloudinary]
    Service --> Supabase[Supabase Storage]
    Controller --> Security[Spring Security + JWT]
```

### Flujo de login JWT

```mermaid
sequenceDiagram
    participant C as Cliente
    participant U as UserController
    participant S as UserService
    participant J as JwtService
    participant DB as MySQL

    C->>U: POST /user/login
    U->>S: login(LoginRequest)
    S->>DB: Buscar usuario por identifier
    DB-->>S: User + Person + Roles
    S->>S: Validar password BCrypt
    S->>J: Generar token con userId
    J-->>S: JWT
    S-->>U: LoginResponse
    U-->>C: 200 OK + token + profile
```

### Flujo de request autenticado

```mermaid
sequenceDiagram
    participant C as Cliente
    participant F as JWT Filter / SecurityConfig
    participant J as JwtService
    participant CT as Controller
    participant S as Service
    participant DB as MySQL

    C->>F: Request con Authorization Bearer token
    F->>J: Validar token
    J-->>F: userId válido
    F->>CT: Continúa request
    CT->>S: Lógica con callerUserId
    S->>DB: Consulta/modificación
    DB-->>S: Datos
    S-->>CT: DTO
    CT-->>C: Response
```

### Roles, actores y permisos

```mermaid
flowchart TD
    User[User] --> Person[Person]
    User --> Admin[Admin]
    User --> Dentist[Dentist]
    User --> Receptionist[Receptionist]
    User --> Patient[Patient]

    Admin --> Clinic[Clinic]
    Dentist --> Clinic
    Receptionist --> Clinic
    Patient --> Clinic
    Clinic --> Organization[Organization]

    Admin --> P1[Gestión amplia de clínica]
    Dentist --> P2[Pacientes, citas y odontograma según permisos]
    Receptionist --> P3[Pacientes, citas y documentación según permisos]
    Patient --> P4[Rol contemplado, login no habilitado en seed]
```

### Diagrama conceptual de base de datos

```mermaid
erDiagram
    ORGANIZATION ||--o{ CLINIC : has
    CLINIC ||--o{ BOX : has
    CLINIC ||--o{ ADMIN : has
    CLINIC ||--o{ DENTIST : has
    CLINIC ||--o{ RECEPTIONIST : has
    CLINIC ||--o{ PATIENT : has
    USER ||--|| PERSON : owns
    USER ||--o| ADMIN : role
    USER ||--o| DENTIST : role
    USER ||--o| RECEPTIONIST : role
    USER ||--o| PATIENT : role_optional
    PATIENT ||--o{ DOCUMENT : has
    PATIENT ||--o| ODONTOGRAM : has
    PATIENT ||--o{ APPOINTMENT : books
    DENTIST ||--o{ APPOINTMENT : attends
    BOX ||--o{ APPOINTMENT : hosts
    ODONTOGRAM ||--o{ DENTAL_PIECE : contains
    DENTAL_PIECE ||--o{ DENTAL_SURFACE : has
    DENTAL_PIECE ||--o{ DENTAL_PIECE_STATE : has
    DENTAL_SURFACE ||--o{ DENTAL_SURFACE_MARK : has
    ODONTOGRAM ||--o{ DENTAL_BRIDGE : has
    DENTAL_BRIDGE ||--o{ DENTAL_BRIDGE_PIECE : includes
```

### Diagrama lógico/ER con atributos principales

```mermaid
erDiagram
    USER {
        Long id
        String username
        String password
        Boolean active
        String themePreference
        String languagePreference
    }

    PERSON {
        Long id
        String name
        String firstSurname
        String secondSurname
        Date birthDate
        String gender
        String email
        String phonePrefix
        String phoneNumber
        String profileImage
    }

    PATIENT {
        Long id
        Date registrationDate
        Boolean active
        String medicalAlert
        String notes
    }

    APPOINTMENT {
        Long id
        LocalDateTime startDateTime
        LocalDateTime endDateTime
        String treatment
        String status
        Boolean active
        String notes
    }

    DOCUMENT {
        Long id
        String name
        String storagePath
        String url
        String mimeType
        String documentType
        Boolean active
    }

    ODONTOGRAM {
        Long id
        String viewMode
        LocalDateTime createdAt
        LocalDateTime updatedAt
    }

    DENTAL_PIECE {
        Long id
        Integer pieceNumber
        String pieceKind
    }

    DENTAL_PIECE_STATE {
        Long id
        String stateType
        Boolean active
        String notes
    }

    DENTAL_SURFACE {
        Long id
        String surfaceType
        String notes
    }

    DENTAL_SURFACE_MARK {
        Long id
        String markType
        String markState
        Boolean active
        String notes
    }

    DENTAL_BRIDGE {
        Long id
        String bridgeState
        Boolean active
        String notes
    }

    USER ||--|| PERSON : owns
    PATIENT ||--|| PERSON : references
    PATIENT ||--o{ DOCUMENT : has
    PATIENT ||--o{ APPOINTMENT : has
    PATIENT ||--o| ODONTOGRAM : has
    ODONTOGRAM ||--o{ DENTAL_PIECE : contains
    DENTAL_PIECE ||--o{ DENTAL_PIECE_STATE : has
    DENTAL_PIECE ||--o{ DENTAL_SURFACE : has
    DENTAL_SURFACE ||--o{ DENTAL_SURFACE_MARK : has
    ODONTOGRAM ||--o{ DENTAL_BRIDGE : has
```

### Flujo de odontograma

```mermaid
flowchart TD
    A[Crear u obtener paciente] --> B[Crear odontograma]
    B --> C[Generar piezas dentales]
    C --> D[Generar superficies por pieza]
    D --> E[Consultar pieza]
    E --> F[Crear estado de pieza]
    E --> G[Consultar superficie]
    G --> H[Crear marca de superficie]
    B --> I[Crear puente]
    I --> J[Asignar piezas ABUTMENT/PONTIC]
    F --> K[Actualizar DTO de odontograma]
    H --> K
    J --> K
```

### Flujo de subida de documento

```mermaid
sequenceDiagram
    participant C as Cliente
    participant DC as DocumentController
    participant DS as DocumentService
    participant SS as SupabaseStorageService
    participant DB as MySQL

    C->>DC: POST /document/patient/{patientId} multipart
    DC->>DS: uploadDocument(patientId, file, name, type, notes, callerUserId)
    DS->>DS: Validar paciente, permisos y PDF
    DS->>SS: Subir PDF
    SS-->>DS: storagePath + url
    DS->>DB: Guardar Document
    DB-->>DS: Document persistido
    DS-->>DC: DocumentDto
    DC-->>C: 201 Created
```

### Flujo de subida/eliminación de foto de perfil

```mermaid
sequenceDiagram
    participant C as Cliente
    participant UC as UserController
    participant US as UserService
    participant CS as CloudinaryService
    participant DB as MySQL

    C->>UC: PUT /user/me multipart
    UC->>US: updateMyProfile(profile, image, removeProfileImage)
    US->>DB: Buscar User + Person
    alt removeProfileImage = true
        US->>CS: Eliminar imagen anterior
        US->>DB: Guardar profileImage null
    else profileImage enviada
        US->>CS: Subir imagen nueva
        CS-->>US: URL Cloudinary
        US->>DB: Guardar URL en Person
    else solo JSON
        US->>DB: Actualizar datos de perfil
    end
    US-->>UC: ProfileDto
    UC-->>C: 200 OK
```

### Flujo de seed

```mermaid
flowchart TD
    A[Ejecutar ApplicationSeed] --> B[Mostrar advertencia]
    B --> C{Usuario escribe SEED?}
    C -- No --> X[Cancelar]
    C -- Sí --> D[Elegir modo DEMO / PERFORMANCE_LIGHT / STRESS]
    D --> E[Limpiar archivos externos registrados]
    E --> F[Truncar tablas]
    F --> G[Crear organización y clínica]
    G --> H[Crear boxes, staff y pacientes base]
    H --> I[Crear odontogramas, piezas, marcas y puentes]
    I --> J{Modo PERFORMANCE o STRESS?}
    J -- Sí --> K[Crear datos de rendimiento]
    J -- No --> M[Subir archivos externos]
    K --> L{Modo STRESS?}
    L -- Sí --> N[Crear pacientes, odontogramas y citas masivas]
    L -- No --> M
    N --> M[Subir imágenes y PDFs]
    M --> O[Ejecutar diagnósticos]
    O --> P{Mantener datos?}
    P -- Sí --> Q[Datos disponibles para Postman]
    P -- No --> R[Limpiar entorno otra vez]
```

### Flujo de despliegue local/Render

```mermaid
flowchart LR
    Dev[Desarrollador] --> Local[Localhost 8080]
    Dev --> Docker[Dockerfile]
    Docker --> Build[Maven clean package -DskipTests]
    Build --> Jar[target/*.jar]
    Jar --> Render[Render]
    Render --> Env[Variables de entorno]
    Env --> DB[(MySQL externa)]
    Env --> Cloudinary[Cloudinary]
    Env --> Supabase[Supabase]
    Postman[Postman] --> Local
    Postman --> Render
```

---

## Checklist antes de subir cambios

Antes de dar un cambio por terminado:

- [ ] El proyecto compila con `./mvnw clean package`.
- [ ] Los tests relevantes pasan o se documenta por qué no.
- [ ] No se han añadido secretos al código.
- [ ] Si cambió un endpoint, se actualizó Postman.
- [ ] Si cambió un endpoint, se actualizó este README.
- [ ] Si cambió un DTO, se revisaron ejemplos JSON.
- [ ] Si cambió una entidad, se revisaron DAO, service, DTO, seed y tests.
- [ ] Si cambió seguridad/JWT, se revisaron `SecurityConfig`, `JwtService`, Postman y frontend.
- [ ] Si cambió odontograma, se revisaron piezas, superficies, marcas, puentes, seed y ejemplos.
- [ ] Si cambió subida de archivos, se revisaron Cloudinary/Supabase, multipart y permisos.
- [ ] Si cambió seed, se revisaron usuarios demo, IDs por defecto, Postman y resultados esperados.
- [ ] Si cambió despliegue, se revisaron `Dockerfile`, variables de entorno y URL base.

---

## Notas pendientes de confirmar

Estas partes no se pueden saber con total seguridad solo mirando el código o la ejecución local:

- Configuración exacta de Render en el panel.
- Si la base de datos externa configurada es de desarrollo, demo o producción.
- Política final de permisos por rol aprobada por negocio.
- Si `ROLE_PATIENT` será usado para login real de pacientes.
- Si Supabase debe usar bucket público o privado.