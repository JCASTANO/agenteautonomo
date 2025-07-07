# FastShop Order Management Service

Sistema de gestión de pedidos para FastShop, implementado con arquitectura hexagonal.

## Tecnologías

- Java 17
- Spring Boot 3.2.1
- Gradle 8.5
- Arquitectura Hexagonal

## Funcionalidades

### Crear Pedido
- **Endpoint**: `POST /pedidos`
- **Descripción**: Crea un nuevo pedido validando stock y procesando el pago

#### Ejemplo de request:
```json
{
  "customerId": 123,
  "items": [
    { "productId": 456, "cantidad": 2 },
    { "productId": 789, "cantidad": 1 }
  ],
  "paymentMethod": "CREDIT_CARD"
}
```

#### Ejemplo de response exitoso (201 Created):
```json
{
  "orderId": 1,
  "customerId": 123,
  "items": [
    { "productId": 456, "cantidad": 2, "unitPrice": 100.00 },
    { "productId": 789, "cantidad": 1, "unitPrice": 250.00 }
  ],
  "totalAmount": 450.00,
  "status": "PAGADO"
}
```

### Consultar Pedido
- **Endpoint**: `GET /orders/{orderId}`
- **Descripción**: Obtiene el detalle de un pedido existente

#### Ejemplo de response exitoso (200 OK):
```json
{
  "orderId": 1,
  "customerId": 123,
  "items": [
    { "productId": 456, "cantidad": 2, "unitPrice": 100.00 },
    { "productId": 789, "cantidad": 1, "unitPrice": 250.00 }
  ],
  "totalAmount": 450.00,
  "status": "PAGADO"
}
```

## Validaciones

- Campos obligatorios: `customerId`, `items` (no vacío), `paymentMethod`
- Cantidad máxima por item: 10 unidades
- Método de pago permitido: solo "CREDIT_CARD"
- Validación de stock disponible

## Códigos de Error

- **400 Bad Request**: Payload inválido (campos faltantes, cantidad > 10, método de pago no permitido)
- **402 Payment Required**: Pago rechazado
- **409 Conflict**: Stock insuficiente o falla en reserva
- **404 Not Found**: Orden no encontrada
- **500 Internal Server Error**: Error inesperado

## Datos de Prueba

El sistema incluye los siguientes productos en el inventario:

| Product ID | Stock | Precio Unitario |
|------------|-------|----------------|
| 456        | 5     | $100.00        |
| 789        | 2     | $250.00        |
| 321        | 10    | $50.00         |

## Ejecución

### Requisitos
- Java 17 o superior
- No se requiere instalación de Gradle (incluye wrapper)

### Comandos

```bash
# Compilar y ejecutar tests
./gradlew test

# Ejecutar la aplicación
./gradlew bootRun

# Crear JAR ejecutable
./gradlew build
```

La aplicación se ejecuta en el puerto 8080.

### Ejemplos de uso

```bash
# Crear un pedido
curl -X POST http://localhost:8080/pedidos \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 123,
    "items": [
      {"productId": 456, "cantidad": 2},
      {"productId": 789, "cantidad": 1}
    ],
    "paymentMethod": "CREDIT_CARD"
  }'

# Consultar un pedido
curl http://localhost:8080/orders/1
```

## Arquitectura

El proyecto sigue los principios de arquitectura hexagonal:

- **Domain**: Entidades de negocio y lógica de dominio
- **Application**: Casos de uso y DTOs
- **Infrastructure**: Adaptadores para persistencia, servicios externos y REST

### Estructura del proyecto

```
src/main/java/com/fastshop/
├── domain/
│   ├── model/          # Entidades de dominio
│   ├── port/           # Interfaces (puertos)
│   └── service/        # Servicios de dominio
├── application/
│   ├── dto/            # Data Transfer Objects
│   └── usecase/        # Casos de uso
└── infrastructure/
    └── adapter/
        ├── controller/ # Controladores REST
        ├── repository/ # Repositorios en memoria
        └── service/    # Servicios simulados
```

## Principios Aplicados

- **Single Responsibility**: Cada clase tiene una única responsabilidad
- **Open/Closed**: Preparado para extensión sin modificación
- **Clean Code**: Nombres descriptivos, mínima duplicación
- **DRY**: Don't Repeat Yourself
- **SOLID**: Principios de diseño orientado a objetos
