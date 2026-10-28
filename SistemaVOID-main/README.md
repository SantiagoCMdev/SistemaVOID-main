# SistemaVOID 🚀

Plataforma de Streaming VOD construida con Node, Express, TypeScript y Prisma.

## Arquitectura
- **Backend:** Node.js, Express, TypeScript (Clean Architecture)
- **Base de Datos:** PostgreSQL con Prisma ORM
- **Cache & Colas:** Redis & BullMQ
- **Validaciones:** Zod
- **Seguridad:** JWT, Helmet, Rate Limiting

## Levantar con Docker

```bash
docker-compose up -d
```

## Desarrollo Local

1. Instalar dependencias: `npm install`
2. Configurar variables de entorno copiando `.env.example` a `.env`
3. Generar cliente Prisma: `npm run prisma:generate`
4. Correr migraciones: `npm run prisma:migrate`
5. Levantar el servidor: `npm run dev`

## Tests

```bash
npm test
```
