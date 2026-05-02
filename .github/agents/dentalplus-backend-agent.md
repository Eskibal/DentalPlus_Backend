# AGENTS.md — Instrucciones para IA en DentalPlus Backend

Este documento define cómo debe trabajar una IA, asistente de código o copiloto cuando modifique el backend de **DentalPlus**.

El objetivo principal es evitar cambios que rompan la arquitectura, la seguridad, los endpoints, el seed, la colección Postman o la compatibilidad con el frontend.

> Ruta recomendada para este proyecto: `.github/agents/dentalplus-backend-agent.md`. También puede mantenerse una copia en la raíz como `AGENTS.md` si se quiere referencia general.

---

## 1. Resumen del proyecto

DentalPlus es un backend Java/Spring Boot para una clínica dental. Expone una API REST para gestionar usuarios, autenticación JWT, roles, pacientes, citas, odontogramas, documentos PDF e imágenes de perfil.

El proyecto usa una arquitectura por capas:

```text
Controller -> Service -> DAO -> DAO Impl Hibernate -> Model/Entity -> Database
                    |
                   DTO
                    |
                 Config
```

Las carpetas principales son:

```text
src/main/java/com/example/DentalPlus_Backend/
├── config/
├── controller/
├── dao/
├── daoImplHibernate/
├── dto/
├── model/
├── seed/
└── service/
```

Recursos importantes:

```text
src/main/resources/application.properties
src/main/resources/seed/
postman/DentalPlus_Postman.json
src/test/java/com/example/DentalPlus_Backend/ApplicationTest.java
src/test/resources/application-test.properties
```

---

## 2. Regla principal para la IA

No inventes clases, endpoints, DTOs, entidades, roles, campos ni comportamientos.

Antes de modificar algo:

1. Busca si ya existe una clase equivalente.
2. Revisa el patrón usado en otros dominios.
3. Mantén consistencia con nombres, rutas y estructura existentes.
4. Si algo no se puede confirmar en el código, márcalo como pendiente de confirmar.
5. No elimines lógica de seguridad, validación o permisos sin justificarlo.

---

## 3. Arquitectura y responsabilidades

### 3.1 Controllers

Ubicación:

```text
src/main/java/com/example/DentalPlus_Backend/controller
```

Responsabilidad:

- Exponer endpoints REST.
- Leer `@RequestBody`, `@PathVariable`, `@RequestParam`, `@RequestPart` o `MultipartFile`.
- Obtener el usuario autenticado cuando corresponda.
- Delegar la lógica al service.
- Devolver DTOs o respuestas HTTP.

No deben contener lógica compleja de negocio.

Controladores detectados:

```text
UserController
PatientController
AppointmentController
DocumentController
OdontogramController
```

Si se añade o cambia un endpoint, actualizar obligatoriamente:

```text
README.md
postman/DentalPlus_Postman.json
Tests si aplica
Frontend si depende del contrato
AGENTS.md si cambia una regla importante
```

---

### 3.2 Services

Ubicación:

```text
src/main/java/com/example/DentalPlus_Backend/service
```

Responsabilidad:

- Contener la lógica de negocio.
- Aplicar permisos y validaciones funcionales.
- Coordinar DAOs y servicios externos.
- Mapear entidades a DTOs cuando el patrón existente lo haga así.

Servicios importantes detectados:

```text
UserService
JwtService
PatientService
AppointmentService
CalendarService
OdontogramService
DentalPieceService
DentalSurfaceService
DentalBridgeService
DocumentService
CloudinaryService
SupabaseStorageService
InventoryService
```

Reglas:

- No saltes de controller directamente a DAO si el patrón existente usa service.
- No pongas lógica de permisos en el controller si el dominio ya la maneja en service.
- Si cambias un permiso, revisa los métodos auxiliares de validación del dominio.
- Si un service depende de `callerUserId`, no lo elimines ni lo sustituyas sin revisar seguridad.

---

### 3.3 DAOs e implementaciones Hibernate

Ubicaciones:

```text
src/main/java/com/example/DentalPlus_Backend/dao
src/main/java/com/example/DentalPlus_Backend/daoImplHibernate
```

Responsabilidad:

- `dao/`: contratos de acceso a datos.
- `daoImplHibernate/`: implementación con Hibernate/JPA.

Reglas:

- Si añades una operación de persistencia, crea o modifica primero la interfaz DAO correspondiente.
- Mantén sincronizada la implementación Hibernate.
- No mezcles queries de persistencia dentro de controllers.
- Si cambias relaciones entre entidades, revisa todas las queries afectadas.

---

### 3.4 DTOs

Ubicación:

```text
src/main/java/com/example/DentalPlus_Backend/dto
```

Responsabilidad:

- Definir el contrato de entrada y salida de la API.
- Evitar exponer entidades JPA completas cuando no corresponda.

DTOs importantes detectados:

```text
LoginRequest
LoginResponse
ProfileDto
PersonDto
RoleDto
PatientDto
AppointmentDto
AvailabilityDto
DocumentDto
OdontogramDto
DentalPieceDto
DentalPieceStateDto
DentalSurfaceDto
DentalSurfaceMarkDto
DentalBridgeDto
DentalBridgePieceDto
```

Reglas al tocar DTOs:

- Revisa controller, service, Postman, README, tests y frontend.
- No cambies nombres de campos sin revisar compatibilidad.
- Si añades un campo obligatorio, actualiza ejemplos y seed si aplica.
- Si quitas un campo, revisa consumidores existentes.

---

### 3.5 Models / entidades

Ubicación:

```text
src/main/java/com/example/DentalPlus_Backend/model
```

Responsabilidad:

- Representar entidades de base de datos y reglas básicas de dominio.
- Definir relaciones JPA.
- Contener validaciones propias del modelo cuando ya exista ese patrón.

Entidades principales detectadas:

```text
User
Person
Admin
Dentist
Receptionist
Patient
Appointment
Box
CalendarRule
CalendarBreak
CalendarException
CalendarHoliday
Odontogram
DentalPiece
DentalPieceState
DentalSurface
DentalSurfaceMark
DentalBridge
DentalBridgePiece
Document
Inventory
Product
Treatment
```

Reglas al tocar entidades:

- Revisa DAO, DAO Impl Hibernate, service, DTOs, seed y tests.
- Revisa diagramas del README si cambia el modelo.
- Revisa `application.properties`, especialmente `spring.jpa.hibernate.ddl-auto`.
- No asumas que la base externa está vacía.
- No hagas cambios destructivos de esquema sin migración o aviso claro.

---

### 3.6 Configuración

Ubicación:

```text
src/main/java/com/example/DentalPlus_Backend/config
```

Clases importantes detectadas:

```text
SecurityConfig
JwtConfig
CloudinaryConfig
SupabaseConfig
```

Reglas:

- No hardcodees credenciales.
- No cambies seguridad global sin revisar todos los endpoints.
- No hagas públicos endpoints que antes requerían token salvo que sea una decisión explícita.
- Si cambias CORS, documenta por qué y para qué frontend.
- Si cambias Cloudinary o Supabase, actualiza variables de entorno y troubleshooting.

---

## 4. Seguridad y JWT

### 4.1 Funcionamiento actual

Autenticación:

```text
POST /user/login
```

El login devuelve un token JWT. Las requests protegidas deben enviar:

```http
Authorization: Bearer <token>
```

El token usa clave privada/pública RSA y el `subject` del token corresponde al `userId`.

Clases relacionadas:

```text
SecurityConfig
JwtConfig
JwtService
UserService
LoginRequest
LoginResponse
```

---

### 4.2 Reglas obligatorias al tocar JWT

Si modificas login, generación de token, validación de token o expiración:

1. Revisa `SecurityConfig`.
2. Revisa `JwtConfig`.
3. Revisa `JwtService`.
4. Revisa `UserService`.
5. Actualiza Postman.
6. Actualiza README.
7. Revisa frontend.
8. Añade o actualiza tests de 401/403/login inválido.

No imprimas tokens completos en logs.

---

### 4.3 Endpoints públicos y protegidos

Regla actual detectada:

- `POST /user/login` es público.
- `OPTIONS /**` es público.
- `/error` es público.
- El resto requiere token.

No añadas endpoints públicos sin justificarlo.

---

## 5. Roles y permisos

Roles detectados:

```text
ADMIN
DENTIST
RECEPTIONIST
PATIENT
```

Authorities usadas por Spring Security:

```text
ROLE_ADMIN
ROLE_DENTIST
ROLE_RECEPTIONIST
ROLE_PATIENT
```

Las authorities se derivan de la existencia del usuario en tablas/entidades de rol como:

```text
Admin
Dentist
Receptionist
Patient
```

Importante:

- No se ha detectado un uso centralizado de permisos con `@PreAuthorize`.
- Muchos permisos se validan dentro de services usando el usuario autenticado, clínica y reglas internas.

Reglas al tocar permisos:

- Revisa el service del dominio afectado.
- Revisa métodos auxiliares de validación como los que resuelven clínica o permisos del caller.
- No confíes solo en el rol si el dominio también valida clínica, paciente o dentista.
- Si cambia una regla de rol, actualiza README, Postman, tests y frontend.

---

## 6. Dominios funcionales

## 6.1 User / autenticación / perfil

Clases principales:

```text
UserController
UserService
JwtService
User
Person
LoginRequest
LoginResponse
ProfileDto
PersonDto
RoleDto
```

Endpoints detectados:

```text
POST /user/login
GET  /user/me
PUT  /user/me
```

`PUT /user/me` puede funcionar con JSON o con multipart cuando hay imagen de perfil.

Reglas:

- Mantén compatibilidad con login por `identifier` y `password` si no se decide lo contrario.
- No cambies el formato de `LoginResponse` sin actualizar frontend y Postman.
- Al tocar perfil, revisa `Person`, `ProfileDto`, `UserService` y subida de imagen.

---

## 6.2 Pacientes

Clases principales:

```text
PatientController
PatientService
PatientDao
PatientDaoImplHibernate
Patient
PatientDto
PersonDto
```

Endpoints detectados:

```text
GET  /patient
GET  /patient/{id}
POST /patient
PUT  /patient/{id}
```

Reglas:

- No inventes `DELETE /patient/{id}` si no existe o no se ha pedido.
- Revisa permisos por clínica antes de devolver o modificar pacientes.
- Si cambias `PatientDto`, actualiza Postman, README y frontend.
- El seed indica que los pacientes no tienen login habilitado actualmente, aunque existe `ROLE_PATIENT`.

---

## 6.3 Citas / appointments

Clases principales:

```text
AppointmentController
AppointmentService
CalendarService
Appointment
Box
CalendarRule
CalendarBreak
CalendarException
CalendarHoliday
AppointmentDto
AvailabilityDto
```

Endpoints detectados:

```text
GET    /appointment
GET    /appointment/{id}
POST   /appointment
PUT    /appointment/{id}
DELETE /appointment/{id}
GET    /appointment/availability
```

Filtros detectados en `GET /appointment`:

```text
date
patientId
dentistId
boxId
```

Reglas:

- Revisa disponibilidad antes de crear o mover citas.
- Revisa conflictos de box, dentista, calendario y horario.
- Si cambias filtros, actualiza Postman y README.
- Si cambias calendario, revisa tests de reglas de calendario.

---

## 6.4 Odontograma

Clases principales:

```text
OdontogramController
OdontogramService
DentalPieceService
DentalSurfaceService
DentalBridgeService
Odontogram
DentalPiece
DentalPieceState
DentalSurface
DentalSurfaceMark
DentalBridge
DentalBridgePiece
OdontogramDto
DentalPieceDto
DentalPieceStateDto
DentalSurfaceDto
DentalSurfaceMarkDto
DentalBridgeDto
DentalBridgePieceDto
```

El odontograma incluye:

- Piezas dentales.
- Superficies.
- Estados de pieza.
- Marcas en superficies.
- Puentes.
- Modo de vista.

Valores de dominio detectados:

```text
viewMode: TEMPORARY, PERMANENT, MIXED

surfaceType:
MESIAL, DISTAL, VESTIBULAR, LINGUAL, OCCLUSAL

markType:
CARIES, FILLING, RADIOGRAPH_CARIES, FISSURE_SEALANT,
EXTRACTION, CROWN, ENDODONTICS, BRIDGE, NATURAL_ABSENCE

markState:
PENDING, DONE, NATURAL

stateType:
HEALTHY, NATURAL_ABSENCE, EXTRACTION_PENDING, EXTRACTION_DONE,
CROWN_PENDING, CROWN_DONE, ENDODONTICS_PENDING, ENDODONTICS_DONE,
BRIDGE_PENDING, BRIDGE_DONE, UNKNOWN

bridgeState:
PENDING, DONE

pieceRole:
ABUTMENT, PONTIC
```

Reglas críticas:

- No cambies nombres de enums sin revisar frontend, seed, Postman y tests.
- No modifiques la lógica de piezas/superficies sin revisar inicialización del odontograma.
- Si cambias marcas, revisa validaciones de superficies.
- Si cambias puentes, revisa piezas implicadas, roles `ABUTMENT`/`PONTIC` y estados.
- Si cambias modo de vista, revisa endpoints por `patientId` y por `odontogramId`.
- Actualiza diagramas Mermaid del README si cambia el modelo.

---

## 6.5 Documentos PDF

Clases principales:

```text
DocumentController
DocumentService
SupabaseStorageService
SupabaseConfig
Document
DocumentDto
```

Endpoints detectados:

```text
GET    /document/patient/{patientId}
POST   /document/patient/{patientId}
DELETE /document/{id}
```

El upload usa multipart y campos como:

```text
file
name
documentType
notes
```

Reglas:

- Solo se deben aceptar PDFs si la lógica actual lo mantiene así.
- Revisa validación de tipo MIME y extensión.
- Revisa permisos sobre paciente antes de listar, subir o borrar documentos.
- Si cambias Supabase, actualiza variables de entorno y README.
- No guardes claves de Supabase en código.

---

## 6.6 Imágenes de perfil

Clases principales:

```text
UserController
UserService
CloudinaryService
CloudinaryConfig
Person
ProfileDto
```

Reglas actuales detectadas:

```text
folder: dentalplus/profile-images
tipos permitidos: image/jpeg, image/png, image/webp
tamaño máximo: 10 MB
campo multipart: profileImage
eliminación: removeProfileImage=true
```

Reglas:

- No permitas cualquier tipo de archivo.
- No subas imágenes sin validar tamaño y tipo.
- Si cambias el campo multipart, actualiza Postman y README.
- Si cambias borrado de imagen, revisa que también se limpie la referencia en `Person`.

---

## 6.7 Inventario, productos y tratamientos

Existen clases/modelos/servicios relacionados con:

```text
Inventory
Product
Treatment
InventoryService
InventoryDao
ProductDao
```

Pero no se detectaron controladores REST públicos para estos dominios en el análisis inicial.

Reglas:

- No documentes endpoints de inventario/productos/tratamientos si no existen.
- Si se crean endpoints, seguir patrón controller-service-dao-dto.
- Actualizar README, Postman, tests y diagramas.

---

## 7. Seed

Clase principal:

```text
src/main/java/com/example/DentalPlus_Backend/seed/ApplicationSeed.java
```

Recursos usados:

```text
src/main/resources/seed/general-consent.pdf
src/main/resources/seed/treatment-plan.pdf
src/main/resources/seed/profile-image.png
```

El seed es destructivo.

Puede:

- Borrar datos de base de datos.
- Truncar tablas.
- Borrar archivos externos registrados en Cloudinary y Supabase.
- Crear organización, clínica, boxes, usuarios, pacientes, odontogramas, citas, documentos y datos demo.

Usuarios demo detectados:

```text
admin@example.com
receptionist@example.com
dentist.primary@example.com
dentist.secondary@example.com
```

Contraseña demo detectada:

```text
Password123
```

Reglas obligatorias al tocar seed:

- No ejecutes seed contra producción.
- Mantén advertencias visibles.
- Si cambias datos demo, actualiza Postman.
- Si cambias usuarios demo, actualiza README.
- Si cambias IDs esperados por ejemplos, actualiza Postman y documentación.
- Si cambias odontograma demo, actualiza ejemplos y tests.
- Si cambias documentos o imágenes demo, revisa Supabase, Cloudinary y recursos locales.

Recomendación para futuras mejoras:

- Usar un perfil explícito de Spring como `seed` o `dev`.
- Bloquear ejecución si el entorno parece producción.

---

## 8. Postman

Archivo:

```text
postman/DentalPlus_Postman.json
```

Variables detectadas:

```text
baseUrl
baseUrlLocal
baseUrlLan
baseUrlRender
authToken
```

URL Render detectada:

```text
https://dentalplus-backend.onrender.com
```

Reglas:

- Cada endpoint nuevo o modificado debe actualizar Postman.
- Cada cambio de body debe actualizar ejemplos.
- Cada cambio de respuesta debe actualizar ejemplos.
- Cada cambio de auth debe actualizar configuración de `authToken`.
- Si se cambia seed, revisar valores por defecto usados por la colección.

---

## 9. Tests

Archivo principal detectado:

```text
src/test/java/com/example/DentalPlus_Backend/ApplicationTest.java
```

Config de test:

```text
src/test/resources/application-test.properties
```

Los tests revisan, entre otras cosas:

- Carga de contexto.
- Seguridad básica.
- Endpoints protegidos.
- Login inválido.
- Validaciones de usuarios/personas.
- Validaciones de citas/calendario.
- Validaciones de odontograma.
- Validaciones de documentos/modelos.

Reglas:

- Si cambias validaciones de modelos, actualiza tests.
- Si cambias endpoints protegidos, actualiza tests de seguridad.
- Si cambias enums de odontograma, actualiza tests.
- Si no puedes ejecutar tests, dilo explícitamente y explica qué revisar manualmente.

---

## 10. Configuración, variables de entorno y secretos

Variables reales o esperadas detectadas:

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
PORT
CLOUDINARY_URL
SUPABASE_URL
SUPABASE_KEY
SUPABASE_BUCKET_DOCUMENTS
AUTH_TOKEN_PRIVATE_KEY
AUTH_TOKEN_PUBLIC_KEY
AUTH_TOKEN_EXPIRATION_MS
```

Reglas de seguridad:

- Nunca añadas credenciales reales al repositorio.
- Nunca pegues claves completas en README, issues, commits o comentarios.
- Si detectas credenciales hardcodeadas, recomienda moverlas a variables de entorno.
- Si el ZIP/repositorio ya se compartió con credenciales, recomienda rotarlas.
- No loguees secretos.

Puntos delicados detectados:

- Base de datos externa configurada por defecto.
- Cloudinary configurado para imágenes.
- Supabase configurado para documentos.
- JWT con claves RSA.
- `spring.jpa.hibernate.ddl-auto=update` puede alterar esquema automáticamente.

---

## 11. Render y Docker

Archivos relacionados:

```text
Dockerfile
pom.xml
application.properties
postman/DentalPlus_Postman.json
```

Reglas:

- Si cambias puerto, revisar `PORT`, Render y README.
- Si cambias Java, sincronizar `pom.xml` y `Dockerfile`.
- Si cambias build/start command, documentarlo.
- No depender de secretos locales para despliegue.

Nota detectada:

- `pom.xml` define Java 17.
- `Dockerfile` usa imagen JDK 21.

Esto no tiene por qué romper, pero conviene mantenerlo documentado o unificado.

---

## 12. Checklist obligatorio antes de cerrar un cambio

Antes de dar por terminado cualquier cambio, comprobar:

```text
[ ] El proyecto compila.
[ ] Los tests pasan, o se explica claramente por qué no se pudieron ejecutar.
[ ] No se añadieron secretos ni credenciales al código.
[ ] No se inventaron endpoints, DTOs, entidades o roles.
[ ] Los endpoints modificados están actualizados en README.
[ ] Los endpoints modificados están actualizados en Postman.
[ ] Los ejemplos request/response siguen siendo correctos.
[ ] Si cambió un DTO, se revisó frontend/Postman/tests.
[ ] Si cambió una entidad, se revisó DAO/service/DTO/seed/tests/diagramas.
[ ] Si cambió seguridad, se revisó SecurityConfig/JwtService/UserService/Postman/frontend.
[ ] Si cambió odontograma, se revisaron piezas/superficies/marcas/estados/puentes/seed.
[ ] Si cambió subida de archivos, se revisó Cloudinary/Supabase/multipart/permisos.
[ ] Si cambió seed, se revisaron usuarios demo, IDs y colección Postman.
[ ] Si cambió configuración, se revisó Render/Docker/application.properties/variables de entorno.
```

---

## 13. Reglas específicas por tipo de cambio

### 13.1 Si cambias un endpoint

Actualizar:

```text
Controller
Service
DTOs si aplica
Postman
README
Tests
Frontend si aplica
```

Verificar:

```text
Auth requerida
Roles permitidos
Body esperado
Respuesta esperada
Errores 400/401/403/404
Compatibilidad con seed
```

---

### 13.2 Si cambias un DTO

Actualizar:

```text
Controller
Service
Mapper manual si existe
Postman
README
Tests
Frontend
```

No cambies nombres de propiedades sin revisar todos los consumidores.

---

### 13.3 Si cambias una entidad

Actualizar:

```text
DAO
DAO Impl Hibernate
Service
DTO
Seed
Tests
Diagramas README
Base de datos/migración
```

Revisar especialmente:

```text
Relaciones JPA
Cascades
Fetch type
Nullability
Enums
Restricciones de dominio
```

---

### 13.4 Si cambias autenticación o roles

Actualizar:

```text
SecurityConfig
JwtConfig
JwtService
UserService
RoleDto/LoginResponse si aplica
Postman auth
README
Tests 401/403
Frontend
```

No abras endpoints accidentalmente.

---

### 13.5 Si cambias odontograma

Revisar:

```text
OdontogramController
OdontogramService
DentalPieceService
DentalSurfaceService
DentalBridgeService
DentalPiece
DentalSurface
DentalSurfaceMark
DentalPieceState
DentalBridge
DentalBridgePiece
DTOs dentales
Seed
Postman
Tests
README diagramas
```

Mantén coherencia entre:

```text
pieceNumber
surfaceType
markType
markState
stateType
bridgeState
pieceRole
viewMode
```

---

### 13.6 Si cambias documentos o imágenes

Documentos:

```text
DocumentController
DocumentService
SupabaseStorageService
SupabaseConfig
Document
DocumentDto
Postman multipart
README troubleshooting
```

Imágenes:

```text
UserController
UserService
CloudinaryService
CloudinaryConfig
Person
ProfileDto
Postman multipart
README troubleshooting
```

Verificar:

```text
Permisos
Tipo de archivo
Tamaño máximo
Multipart field names
Borrado externo
Persistencia de URL
Errores de servicio externo
```

---

### 13.7 Si cambias seed

Actualizar:

```text
ApplicationSeed
src/main/resources/seed/
README
Postman
Tests/diagnósticos
Usuarios demo
Datos por defecto
```

Verificar:

```text
No se ejecuta accidentalmente en producción
No borra datos externos no controlados
No deja referencias rotas a archivos
Los usuarios demo pueden hacer login
Postman sigue funcionando con los datos generados
```

---

## 14. Estilo de código y convenciones

Mantener el estilo existente del proyecto:

- Nombres de clases en inglés.
- Nombres de paquetes existentes.
- Controllers con rutas REST claras.
- DTOs separados de entidades.
- Services para lógica de negocio.
- DAOs para persistencia.
- Validaciones cerca del dominio cuando ya exista ese patrón.

Evitar:

- Duplicar lógica.
- Crear utilidades innecesarias.
- Mezclar responsabilidades.
- Hacer cambios masivos sin necesidad.
- Renombrar endpoints existentes sin motivo.
- Introducir dependencias nuevas sin justificar.

---

## 15. Errores comunes que deben considerarse

Al modificar el backend, revisar posibles errores:

```text
401 Unauthorized: token ausente, expirado o inválido.
403 Forbidden: usuario autenticado sin permisos suficientes.
400 Bad Request: JSON mal construido o validación fallida.
404 Not Found: recurso no existe o no pertenece a la clínica/usuario.
415 Unsupported Media Type: multipart o content-type incorrecto.
500 Internal Server Error: error no controlado, DB, Cloudinary o Supabase.
```

Casos delicados:

- Token sin prefijo `Bearer`.
- JSON con campos renombrados.
- Multipart con nombres de campos incorrectos.
- PDF rechazado por validación.
- Imagen rechazada por tipo/tamaño.
- Base de datos externa no accesible.
- Seed ejecutado sobre entorno equivocado.
- `ddl-auto=update` alterando esquema inesperadamente.

---


### Desincronización entre entidades Java y esquema MySQL

La IA debe tener en cuenta que la base de datos externa puede conservar columnas antiguas que ya no existen en las entidades Java. `spring.jpa.hibernate.ddl-auto=update` puede actualizar parte del esquema, pero no debe asumirse que elimina columnas obsoletas.

Caso real detectado:

```text
Field 'city' doesn't have a default value
insert into dentist (...)
```

Interpretación:

- la tabla `dentist` tenía una columna antigua `city` obligatoria;
- la entidad `Dentist` actual no contiene `city`;
- los datos personales como ciudad pertenecen a `Person`;
- el seed falla al insertar un dentista.

Antes de proponer añadir campos al modelo, comprobar si la columna es realmente parte del diseño actual o si es residuo de una versión anterior.

Comandos útiles para diagnóstico:

```sql
SHOW COLUMNS FROM dentist;

SELECT COLUMN_NAME, IS_NULLABLE, COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'dentist';
```

Si una columna obligatoria no existe en la entidad Java ni forma parte del diseño actual, proponer corregir el esquema o migración, no añadir código innecesario.

Ejemplo de corrección para una columna antigua:

```sql
ALTER TABLE dentist DROP COLUMN city;
```

Regla: si se detecta desincronización entre entidad y tabla, actualizar README, agente, seed/tests si aplica, y valorar migración formal.


## 16. Documentación que debe mantenerse sincronizada

Actualizar README cuando cambie:

```text
Arquitectura
Endpoints
DTOs
Roles/permisos
JWT
Variables de entorno
Render/Docker
Seed
Postman
Tests
Odontograma
Documentos/imágenes
Troubleshooting
Diagramas Mermaid
```

Actualizar Postman cuando cambie:

```text
Ruta
Método
Body
Headers
Auth
Parámetros
Respuesta esperada
Valores de seed
baseUrl
```

Actualizar AGENTS.md cuando cambie:

```text
Arquitectura base
Reglas de seguridad
Convenciones importantes
Checklist obligatorio
Flujos críticos del proyecto
```

---

## 17. Política de no invención

Cuando una IA no esté segura:

- No debe inventar comportamiento.
- No debe asumir endpoints inexistentes.
- No debe asumir permisos no codificados.
- No debe documentar como real algo que solo parece probable.

Usar frases como:

```text
Pendiente de confirmar.
No detectado en el código actual.
Existe modelo/servicio, pero no endpoint REST detectado.
Revisar con frontend antes de cambiar este contrato.
```

---

## 18. Prioridades al modificar el proyecto

Orden recomendado:

1. Seguridad y datos sensibles.
2. Compatibilidad de API con frontend/Postman.
3. Integridad de base de datos.
4. Seed y datos demo.
5. Tests.
6. Documentación.
7. Limpieza/refactor.

No sacrifiques seguridad por comodidad.

---

## 19. Recordatorios finales

- Si se cambia un endpoint, actualizar README, Postman y ejemplos.
- Si se cambia una entidad, revisar DTOs, DAOs, servicios, seed, tests y diagramas.
- Si se cambia autenticación o roles, revisar `SecurityConfig`, `UserService`, `JwtService`, Postman y frontend.
- Si se cambia odontograma, revisar piezas, superficies, marcas, estados, puentes, seed y ejemplos.
- Si se cambia subida de archivos, revisar Cloudinary, Supabase, documentos, imágenes, permisos y multipart.
- Si se cambia seed, revisar valores por defecto de Postman.
- Si se detectan secretos, no repetirlos: recomendar moverlos a variables de entorno y rotarlos.

