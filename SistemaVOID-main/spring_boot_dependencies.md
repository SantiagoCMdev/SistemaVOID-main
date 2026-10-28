# Dependencias del Proyecto Spring Boot (Sistema VOID)



## ¿Dónde están configuradas?

Todas las dependencias del proyecto están declaradas en el archivo principal de configuración de Maven:
[`void-spring/pom.xml`](file:///c:/Users/david/SistemaVOID/void-spring/pom.xml)

Maven se encarga de leer este archivo, descargar automáticamente estas librerías desde los repositorios de internet (Maven Central) y añadirlas al proyecto. Físicamente en tu computadora, las guarda en una carpeta oculta de tu usuario llamada `.m2` (generalmente en `C:\Users\david\.m2\repository`).

---

## Lista de Dependencias Utilizadas

Aquí tienes cada dependencia, cómo se llama en el proyecto y **para qué sirve exactamente**:

### 1. Spring Boot Web (`spring-boot-starter-web`)
* **Uso**: Es la base del proyecto. Sirve para crear aplicaciones web y APIs RESTful (los endpoints como `@GetMapping` y `@PostMapping`).
* **Incluye**: Un servidor web integrado (Apache Tomcat) para que la aplicación corra por sí sola sin instalar servidores adicionales.

### 2. Spring Data JPA (`spring-boot-starter-data-jpa`)
* **Uso**: Facilita la comunicación con la base de datos de forma orientada a objetos (ORM).
* **Función**: En lugar de escribir sentencias SQL a mano, usamos código Java y repositorios (ej. `UserRepository`). Usa Hibernate por debajo.

### 3. Driver de PostgreSQL (`postgresql`)
* **Uso**: Es el traductor (driver) que le permite a Spring Data JPA comunicarse específicamente con tu base de datos PostgreSQL.

### 4. Spring Security (`spring-boot-starter-security`)
* **Uso**: Se encarga de toda la seguridad, protegiendo las rutas de la API, administrando el contexto del usuario actual y cifrando las contraseñas (`PasswordEncoder`).

### 5. JJWT - Java JWT (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`)
* **Uso**: Librería usada en combinación con Spring Security para crear, firmar y leer los **JSON Web Tokens (JWT)**.
* **Función**: Mantiene las sesiones de los usuarios para que, una vez inicien sesión, envíen este token en los headers de sus peticiones y el sistema sepa quiénes son.

### 6. Spring Data Redis (`spring-boot-starter-data-redis`)
* **Uso**: Sirve para conectar la aplicación con la base de datos en memoria Redis.
* **Función en el sistema**: Lo usas en el `PlaybackUseCase` para contar cuántas pantallas/streams concurrentes tiene un usuario activo en tiempo real y aplicar los límites de cada plan.

### 7. Thymeleaf (`spring-boot-starter-thymeleaf`)
* **Uso**: Motor de plantillas del lado del servidor.
* **Función**: Sirve para renderizar las vistas HTML como tu tabla del directorio de usuarios (`users.html`). Combina el HTML estático con los datos del servidor (como las variables `${users}`).

### 8. Lombok (`lombok`)
* **Uso**: Es una herramienta de productividad para escribir menos código.
* **Función**: Genera automáticamente getters, setters, constructores y más cosas "por debajo de la mesa" al compilar, usando anotaciones como `@RequiredArgsConstructor` (usado para inyectar los repositorios en los servicios).

### 9. Herramientas de Desarrollo y Testing
* **Spring Boot DevTools**: Para recargar la aplicación automáticamente cuando haces un cambio en el código (Hot Reload).
* **Spring Boot Starter Test**: Incluye JUnit y Mockito para cuando decidas hacer pruebas unitarias del código.

---

> [!TIP]
> **Consejo para explicarle al profesor:** 
> Puedes resumir diciendo: *"El proyecto usa Maven como gestor de dependencias (en el archivo `pom.xml`). El núcleo es Spring Boot Web para la API, Spring Data JPA junto al driver de Postgres para interactuar con la base de datos, Spring Security junto con JJWT para la autenticación basada en tokens, y Redis para controlar los límites de sesiones simultáneas del streaming."*
