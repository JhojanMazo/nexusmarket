# C4 - Nivel 1: Diagrama de Contexto del Sistema

## 1. Diagrama de Contexto
El siguiente diagrama muestra el sistema NexusMarket en relación con sus usuarios humanos y los sistemas externos circundantes:

```mermaid
graph TD
    Buyer["Comprador (Buyer)<br>[Persona]<br>Explora catálogo, crea carritos y genera órdenes de compra"]
    Seller["Vendedor (Seller)<br>[Persona]<br>Publica productos, define variantes y revisa ventas"]
    Admin["Administrador (Admin)<br>[Persona]<br>Gestiona usuarios, aprueba vendedores y audita el sistema"]
    Logistics["Operador Logístico<br>[Persona]<br>Despacha pedidos, actualiza envíos y procesa devoluciones"]

    NexusSystem["Sistema NexusMarket<br>[Microservicio Spring Boot]<br>Plataforma hexagonal de comercio electrónico y gestión multialmacén"]

    ExtPayment["Pasarela de Pagos Externa<br>[Sistema Externo]<br>Valida y liquida cobros con tarjeta y transferencias"]
    ExtShipping["Operador de Envíos Externo<br>[Sistema Externo]<br>Genera guías de transporte y rastreo físico de paquetes"]

    Buyer -->|"Usa API HTTPS"| NexusSystem
    Seller -->|"Usa API HTTPS"| NexusSystem
    Admin -->|"Usa API HTTPS"| NexusSystem
    Logistics -->|"Usa API HTTPS"| NexusSystem

    NexusSystem -->|"Procesa pagos"| ExtPayment
    NexusSystem -->|"Coordina guías"| ExtShipping
```

---

## 2. Descripción de Entidades de Contexto
- **Comprador:** Realiza el recorrido de compra de punta a punta.
- **Vendedor:** Monitorea su inventario y catálogo de productos.
- **Administrador:** Garantiza la seguridad y cumplimiento regulatorio.
- **Operador Logístico:** Gestiona los movimientos en almacenes físicos.
- **Sistemas Externos:** Integración futura mediante puertos de salida para procesamiento de pagos bancarios y empresas de transporte de carga.
