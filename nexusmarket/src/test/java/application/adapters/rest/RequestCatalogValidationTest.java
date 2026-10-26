package application.adapters.rest;

import application.adapters.rest.dtos.requests.AddCartItemRequestDTO;
import application.adapters.rest.dtos.requests.AddressDTO;
import application.adapters.rest.dtos.requests.CreateBuyerRequestDTO;
import application.adapters.rest.dtos.requests.CreateCartRequestDTO;
import application.adapters.rest.dtos.requests.CreateOrderRequestDTO;
import application.adapters.rest.dtos.requests.CreateSellerRequestDTO;
import application.adapters.rest.dtos.requests.CreateStaffUserRequestDTO;
import application.adapters.rest.dtos.requests.GenerateInvoiceRequestDTO;
import application.adapters.rest.dtos.requests.LoginRequestDTO;
import application.adapters.rest.dtos.requests.ProcessPaymentRequestDTO;
import application.adapters.rest.dtos.requests.ProductPriceUpdateRequestDTO;
import application.adapters.rest.dtos.requests.ProductRequestDTO;
import application.adapters.rest.dtos.requests.ProductStatusUpdateRequestDTO;
import application.adapters.rest.dtos.requests.UpdateOrderStatusRequestDTO;
import application.adapters.rest.dtos.requests.UpdateUserStatusRequestDTO;
import application.domain.enums.OrderStatus;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.enums.UserRole;
import application.domain.enums.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RequestCatalogValidationTest {

    @Test
    @DisplayName("Validate AddressDTO contract")
    void testAddressDTO() {
        AddressDTO dto = new AddressDTO("Calle 10", "Medellin", "Antioquia", "Colombia", "05001", "Apt 201");
        assertEquals("Calle 10", dto.street());
        assertEquals("Medellin", dto.city());
        assertEquals("Antioquia", dto.stateOrProvince());
        assertEquals("Colombia", dto.country());
        assertEquals("05001", dto.postalCode());
        assertEquals("Apt 201", dto.reference());
    }

    @Test
    @DisplayName("Validate LoginRequestDTO contract")
    void testLoginRequestDTO() {
        LoginRequestDTO dto = new LoginRequestDTO("test@nexus.com", "Secret123");
        assertEquals("test@nexus.com", dto.email());
        assertEquals("Secret123", dto.password());
    }

    @Test
    @DisplayName("Validate CreateBuyerRequestDTO contract")
    void testCreateBuyerRequestDTO() {
        AddressDTO address = new AddressDTO("Cra 5", "Bogota", "DC", "Colombia", "1101", null);
        CreateBuyerRequestDTO dto = new CreateBuyerRequestDTO("John Doe", "john@nexus.com", "CC", "123456", address);
        assertEquals("John Doe", dto.fullName());
        assertEquals("john@nexus.com", dto.email());
        assertEquals("CC", dto.documentType());
        assertEquals("123456", dto.documentNumber());
        assertNotNull(dto.primaryAddress());
    }

    @Test
    @DisplayName("Validate CreateSellerRequestDTO contract")
    void testCreateSellerRequestDTO() {
        LocalDate now = LocalDate.now();
        UUID adminId = UUID.randomUUID();
        CreateSellerRequestDTO dto = new CreateSellerRequestDTO(adminId, "Store LLC", "store@nexus.com", "NIT", "900123", now);
        assertEquals(adminId, dto.adminUserId());
        assertEquals("Store LLC", dto.fullName());
        assertEquals("store@nexus.com", dto.email());
        assertEquals("NIT", dto.documentType());
        assertEquals(now, dto.onboardingDate());
    }

    @Test
    @DisplayName("Validate CreateStaffUserRequestDTO contract")
    void testCreateStaffUserRequestDTO() {
        CreateStaffUserRequestDTO dto = new CreateStaffUserRequestDTO("Admin Staff", "admin@nexus.com", "CC", "999", UserRole.ADMINISTRATOR);
        assertEquals("Admin Staff", dto.fullName());
        assertEquals(UserRole.ADMINISTRATOR, dto.role());
    }

    @Test
    @DisplayName("Validate UpdateUserStatusRequestDTO contract")
    void testUpdateUserStatusRequestDTO() {
        UpdateUserStatusRequestDTO dto = new UpdateUserStatusRequestDTO(UserStatus.BLOCKED);
        assertEquals(UserStatus.BLOCKED, dto.status());
    }

    @Test
    @DisplayName("Validate Cart and Item request DTOs")
    void testCartRequestDTOs() {
        UUID buyerId = UUID.randomUUID();
        CreateCartRequestDTO cartReq = new CreateCartRequestDTO(buyerId);
        assertEquals(buyerId, cartReq.buyerId());

        UUID productId = UUID.randomUUID();
        AddCartItemRequestDTO itemReq = new AddCartItemRequestDTO(productId, 2, BigDecimal.valueOf(50), "USD");
        assertEquals(productId, itemReq.productId());
        assertEquals(2, itemReq.quantity());
        assertEquals(BigDecimal.valueOf(50), itemReq.unitPrice());
        assertEquals("USD", itemReq.currency());
    }

    @Test
    @DisplayName("Validate Order and Payment request DTOs")
    void testOrderAndPaymentRequestDTOs() {
        UUID cartId = UUID.randomUUID();
        AddressDTO address = new AddressDTO("Main St", "Cali", "Valle", "Colombia", "7600", null);
        CreateOrderRequestDTO orderReq = new CreateOrderRequestDTO(cartId, address);
        assertEquals(cartId, orderReq.cartId());
        assertNotNull(orderReq.deliveryAddress());

        UpdateOrderStatusRequestDTO statusReq = new UpdateOrderStatusRequestDTO(OrderStatus.SHIPPED);
        assertEquals(OrderStatus.SHIPPED, statusReq.status());

        UUID orderId = UUID.randomUUID();
        ProcessPaymentRequestDTO payReq = new ProcessPaymentRequestDTO(orderId, "CREDIT_CARD", BigDecimal.valueOf(100), "USD");
        assertEquals(orderId, payReq.orderId());
        assertEquals(BigDecimal.valueOf(100), payReq.amount());
        assertEquals("USD", payReq.currency());
        assertEquals("CREDIT_CARD", payReq.paymentMethod());
    }

    @Test
    @DisplayName("Validate Invoice and Product request DTOs")
    void testInvoiceAndProductRequestDTOs() {
        UUID orderId = UUID.randomUUID();
        GenerateInvoiceRequestDTO invReq = new GenerateInvoiceRequestDTO(orderId, BigDecimal.valueOf(19), "USD");
        assertEquals(orderId, invReq.orderId());
        assertEquals(BigDecimal.valueOf(19), invReq.taxAmount());
        assertEquals("USD", invReq.currency());
        assertEquals("USD", invReq.currencyCode());

        ProductRequestDTO prodReq = new ProductRequestDTO("Laptop", "RAM", "16GB", BigDecimal.valueOf(1200), "USD", ProductStatus.PUBLISHED, ProductType.PHYSICAL);
        assertEquals("Laptop", prodReq.name());
        assertEquals("16GB", prodReq.variantValue());

        ProductPriceUpdateRequestDTO priceReq = new ProductPriceUpdateRequestDTO(BigDecimal.valueOf(1100), "USD");
        assertEquals(BigDecimal.valueOf(1100), priceReq.price());

        ProductStatusUpdateRequestDTO prodStatusReq = new ProductStatusUpdateRequestDTO(ProductStatus.DISCONTINUED);
        assertEquals(ProductStatus.DISCONTINUED, prodStatusReq.status());
    }
}
