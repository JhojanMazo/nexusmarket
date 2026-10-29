package application.domain.ports.in;

import application.domain.enums.BuyerCommercialStatus;
import application.domain.enums.UserRole;
import application.domain.enums.UserStatus;
import application.domain.enums.WarehouseType;
import application.domain.models.AdministrativeReport;
import application.domain.models.Buyer;
import application.domain.models.Invoice;
import application.domain.models.Operation;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.PageResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AdministratorPort {
    User registerStaff(String fullName, String email, String docType, String docNumber, UserRole role);
    Seller registerSeller(UUID adminUserId, String fullName, String email, String docType, String docNumber, LocalDate onboardingDate);
    Seller registerSellerWithWarehouse(UUID adminUserId, String fullName, String email, String docType, String docNumber,
                                       LocalDate onboardingDate, String warehouseName, Address warehouseAddress, Double capacity);
    User updateUserStatus(UUID userId, UserStatus newStatus);
    User getUserById(UUID userId);
    List<User> listAllUsers();
    default PageResult<User> listAllUsers(int page, int size) {
        return PageResult.of(listAllUsers(), page, size);
    }

    Buyer updateBuyerCommercialStatus(UUID buyerId, BuyerCommercialStatus status);

    Warehouse createWarehouse(String name, Address address, WarehouseType type, UUID sellerId, double capacity);
    Warehouse updateWarehouseCapacity(UUID warehouseId, double capacity);
    Warehouse getWarehouseById(UUID warehouseId);
    List<Warehouse> listAllWarehouses();
    List<Warehouse> listWarehousesByType(WarehouseType type);
    List<Warehouse> listWarehousesBySeller(UUID sellerId);

    ReturnRequest approveReturn(UUID returnRequestId);
    ReturnRequest rejectReturn(UUID returnRequestId);
    Refund processRefund(UUID returnRequestId, BigDecimal amount, String currencyCode);
    List<ReturnRequest> listAllReturns();
    List<Refund> listAllRefunds();

    default PageResult<Order> listAllOrders(int page, int size, String sortBy, String direction) {
        return PageResult.of(List.of(), page, size);
    }
    default PageResult<Product> listAllProducts(int page, int size, String sortBy, String direction) {
        return PageResult.of(List.of(), page, size);
    }

    AdministrativeReport getAdministrativeReport();

    Invoice generateInvoice(UUID orderId, BigDecimal taxAmount, String currencyCode);
    Invoice getInvoiceById(UUID invoiceId);
    Invoice getInvoiceByOrderId(UUID orderId);
    List<Invoice> getAllInvoices();

    Operation recordOperation(String operationType, String description, UUID operatorId, String payload);
    Operation getOperationById(UUID operationId);
    List<Operation> getAllOperations();
}
