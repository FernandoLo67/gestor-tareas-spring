# Gestor de Tareas — Spring Boot + PostgreSQL

API REST para gestionar proyectos y tareas, con Spring Boot 3, Spring Data JPA, PostgreSQL, validacion con Bean Validation, paginacion y tests de integracion.

Es el unico proyecto del portafolio que usa un framework. Todos los demas — JWT, WebSocket, microservicios, el parser JSON — estan hechos desde cero con la biblioteca estandar de Java. Este existe para mostrar la otra mitad: que despues de entender como funcionan las cosas por dentro, se usan frameworks **con criterio** y no por inercia.

## Por que Spring Boot y no otro framework

Porque es lo que pide el mercado, y porque resuelve exactamente los problemas que los otros proyectos resuelven a mano:

| Este portafolio a mano | Spring Boot lo resuelve con |
|---|---|
| Router con Map de rutas (`api-rest-ecommerce`) | `@GetMapping`, `@PostMapping` |
| JSON parser escrito a mano (`app-clima-api`) | Jackson, autoconfigurado |
| Servidor HTTP con `com.sun.net.httpserver` | Tomcat embebido |
| JDBC directo, SQL a mano | Spring Data JPA: interfaces sin implementacion |
| Validacion manual en constructores | `@NotBlank`, `@Size`, `@Valid` |

La diferencia entre alguien que usa `@Transactional` porque asi dice el tutorial y alguien que lo usa **sabiendo** que por debajo hay un proxy de AOP que gestiona la conexion y el commit es la diferencia que hace este portafolio.

## La API

### Proyectos

| Metodo | Ruta | Que hace |
|---|---|---|
| `GET` | `/api/proyectos?page=0&size=10` | Listar con paginacion |
| `GET` | `/api/proyectos/{id}` | Detalle con total de tareas |
| `POST` | `/api/proyectos` | Crear |
| `PUT` | `/api/proyectos/{id}` | Actualizar |
| `DELETE` | `/api/proyectos/{id}` | Eliminar (con sus tareas) |

### Tareas

| Metodo | Ruta | Que hace |
|---|---|---|
| `GET` | `/api/proyectos/{id}/tareas?estado=PENDIENTE&prioridad=ALTA` | Listar con filtros y paginacion |
| `GET` | `/api/tareas/{id}` | Detalle |
| `POST` | `/api/proyectos/{id}/tareas` | Crear dentro de un proyecto |
| `PUT` | `/api/tareas/{id}` | Actualizar |
| `PATCH` | `/api/tareas/{id}/estado` | Cambiar estado |
| `DELETE` | `/api/tareas/{id}` | Eliminar |

Estados: `PENDIENTE`, `EN_PROGRESO`, `COMPLETADA`. Prioridades: `BAJA`, `MEDIA`, `ALTA`.

## Ejemplo

```bash
# Crear un proyecto
curl -X POST http://localhost:8080/api/proyectos \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Portafolio", "descripcion": "Mejoras pendientes"}'

# Agregar una tarea
curl -X POST http://localhost:8080/api/proyectos/1/tareas \
  -H "Content-Type: application/json" \
  -d '{"titulo": "Agregar Docker", "prioridad": "ALTA", "fechaLimite": "2026-11-30"}'

# Marcarla como en progreso
curl -X PATCH http://localhost:8080/api/tareas/1/estado \
  -H "Content-Type: application/json" \
  -d '{"estado": "EN_PROGRESO"}'

# Filtrar solo las pendientes con prioridad alta
curl "http://localhost:8080/api/proyectos/1/tareas?estado=PENDIENTE&prioridad=ALTA"
```

## Las decisiones

**DTOs separados para entrada y salida.** `ProyectoRequest` tiene lo que el cliente envia; `ProyectoResponse` tiene lo que el servidor devuelve, incluido el `totalTareas` que no existe en la entidad. Mezclar las dos cosas en una sola clase obliga a ignorar campos con `@JsonIgnore`, que es fragil y confuso.

**Records para los DTOs.** Son inmutables por construccion y no necesitan getters, setters ni equals escritos a mano. Jackson los deserializa sin problemas desde Spring Boot 3.

**Manejo global de excepciones con `@RestControllerAdvice`.** Todos los errores salen con el mismo formato JSON — timestamp, codigo, mensaje — en vez de dejar que Spring devuelva su traza por defecto, que expone internos del servidor.

**`@Transactional(readOnly = true)` en las consultas.** No es decoracion: le dice a Hibernate que no necesita hacer dirty checking al final, y a la base de datos que puede usar una replica de solo lectura si la hay.

**Cascade delete de proyecto a tareas.** Borrar un proyecto borra sus tareas. La alternativa — rechazar el delete si tiene tareas — obliga al cliente a borrarlas una por una primero, que es un fastidio sin beneficio en este dominio.

**H2 para los tests, PostgreSQL para produccion.** Los tests no dependen de tener PostgreSQL instalado ni corriendo, lo que los hace reproducibles en cualquier maquina y en CI. El perfil `test` de Spring selecciona H2 automaticamente.

**`open-in-view=false`.** Desactiva el antipatron de abrir la sesion de Hibernate en el filtro web y mantenerla hasta que se renderiza la respuesta. Con OSIV activo, un lazy load puede disparar queries en la capa del controlador sin que nadie se de cuenta — y un dia el endpoint empieza a hacer 200 queries por peticion. Con OSIV apagado, si algo no se cargo dentro del `@Transactional`, falla rapido.

## Estructura

```
src/main/java/com/fernando/tareas/
├── TareasApplication.java
├── modelo/
│   ├── Proyecto.java                 @Entity, @OneToMany a Tarea
│   ├── Tarea.java                    @Entity, @ManyToOne a Proyecto
│   ├── EstadoTarea.java              PENDIENTE, EN_PROGRESO, COMPLETADA
│   └── Prioridad.java                BAJA, MEDIA, ALTA
├── repositorio/
│   ├── ProyectoRepositorio.java      JpaRepository
│   └── TareaRepositorio.java         Queries derivadas con filtros
├── servicio/
│   ├── ProyectoServicio.java         CRUD + busqueda de entidad
│   └── TareaServicio.java            CRUD + filtros + cambio de estado
├── controlador/
│   ├── ProyectoControlador.java      REST /api/proyectos
│   └── TareaControlador.java         REST /api/tareas y /api/proyectos/{id}/tareas
├── dto/
│   ├── ProyectoRequest.java          Entrada con validacion
│   ├── ProyectoResponse.java         Salida con totalTareas
│   ├── TareaRequest.java             Entrada con validacion
│   ├── TareaResponse.java            Salida con proyectoId
│   └── CambioEstadoRequest.java      Para el PATCH de estado
└── excepcion/
    ├── RecursoNoEncontradoException.java
    └── ManejadorGlobalDeExcepciones.java
src/test/java/com/fernando/tareas/controlador/
├── ProyectoControladorTest.java      10 pruebas de integracion
└── TareaControladorTest.java         14 pruebas de integracion
```

## Las 24 pruebas

Todas son **tests de integracion**: levantan el contexto de Spring, usan MockMvc para hacer peticiones HTTP reales contra los controladores, y H2 como base de datos.

**`ProyectoControladorTest` (10)** — crear, validacion de nombre vacio y nulo, paginacion, buscar por id, 404, actualizar, eliminar, eliminar inexistente, totalTareas en cero.

**`TareaControladorTest` (14)** — crear con todos los campos, titulo vacio, proyecto inexistente, prioridad por defecto, paginacion, filtro por estado, filtro por prioridad, buscar por id, actualizar, cambiar estado, eliminar, cascade delete con el proyecto, totalTareas actualizado.

## Requisitos

- Java 17+
- Maven 3.8+
- PostgreSQL 14+ (para ejecutar; los tests usan H2)

## Como ejecutar

```bash
# Crear la base de datos
createdb gestor_tareas

# Compilar y arrancar
mvn spring-boot:run
```

La aplicacion arranca en `http://localhost:8080`. Hibernate crea las tablas automaticamente con `ddl-auto=update`.

## Como correr las pruebas

```bash
mvn test
```

No necesita PostgreSQL — las pruebas usan H2 en memoria.
