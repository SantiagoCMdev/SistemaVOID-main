# Sistema VOID

Plataforma de streaming de video bajo demanda (VOD) construida con una arquitectura moderna desacoplada. El sistema cuenta con un backend en Java con Spring Boot basado en Clean Architecture y un frontend web desarrollado en Next.js con TypeScript y Tailwind CSS.

## Estructura del Proyecto

El repositorio esta organizado en las siguientes areas:

- frontend: Aplicacion web desarrollada en Next.js (React 19, TypeScript, Tailwind CSS, Framer Motion).
- void-spring: Backend de servicios RESTful construido con Java 21 y Spring Boot.
- database: Scripts de inicializacion y esquemas SQL para PostgreSQL.
- prisma: Esquemas de modelado de datos para soporte adicional.

## Arquitectura del Backend

El backend sigue los principios de Clean Architecture y separacion de responsabilidades:

- Dominio (com.voidapp.domain.model): Entidades centrales como User, Subscription, Plan, Video, PlaybackProgress, Invoice y UserSettings.
- Casos de Uso (com.voidapp.core.application.usecase): Logica de negocio pura que incluye registro, autenticacion, control de suscripciones, prorrateo, reproduccion y concurrencia.
- Infraestructura (com.voidapp.infrastructure): Controladores REST, repositorios JPA, configuracion de seguridad con Spring Security, persistencia en base de datos y cache en Redis.

## Caracteristicas Principales

- Catalogo audiovisual interactivo con reproduccion continua y recomendaciones.
- Gestion de suscripciones con soporte de planes, facturas idempotentes y calculo de prorrateo en upgrades.
- Control de streaming concurrente gestionado en memoria con Redis (Sorted Sets) para limitar pantallas en simultaneo segun el plan.
- Sincronizacion de progreso de reproduccion en tiempo real.
- Configuracion personalizada de perfil y reproduccion (idioma, tema, notificaciones, autoplay).

## Requisitos Previos

- Java Development Kit (JDK) 21 o superior
- Apache Maven 3.9 o superior
- Node.js 18 o superior y npm
- Docker y Docker Compose (opcional para PostgreSQL y Redis)

## Puesta en Marcha

### 1. Iniciar el Backend (Spring Boot)

Desde la terminal en el directorio del backend:

```bash
cd void-spring
mvn spring-boot:run
```

El servicio iniciara por defecto en:
http://localhost:8080

La consola de administracion H2 se encuentra disponible en:
http://localhost:8080/h2-console

### 2. Iniciar el Frontend (Next.js)

Desde una segunda terminal en el directorio del frontend:

```bash
cd frontend
npm install
npm run dev
```

La aplicacion web estara disponible en:
http://localhost:3000

Tambien es posible ejecutar los scripts por lotes incluidos en el proyecto:
- start-server.bat para levantar el backend.
- start-frontend.bat para levantar el frontend.
- start-all.bat para iniciar ambos componentes simultaneamente.

## Pruebas Automatizadas

El backend incluye una suite completa de pruebas unitarias implementadas con JUnit 5, Mockito y AssertJ sin levantar el contexto pesado de Spring, garantizando ejecuciones rapidas y aisladas:

- RegisterUserUseCaseTest: Registro de usuarios y control de correos duplicados.
- LoginUserUseCaseTest: Autenticacion de credenciales y emision de tokens.
- ManageSubscriptionUseCaseTest: Creacion de suscripciones, facturacion, upgrades, downgrades y cancelacion.
- PlaybackUseCaseTest: Control de sesiones concurrentes con Redis y sincronizacion de progreso.
- UpdateUserProfileUseCaseTest: Actualizacion de datos de usuario y validaciones.
- UpdateUserSettingsUseCaseTest: Configuracion granular de preferencias.

Para ejecutar toda la suite de pruebas del backend:

```bash
cd void-spring
mvn test
```

## Licencia

Este proyecto esta bajo una licencia de uso privada para fines academicos y de desarrollo.
