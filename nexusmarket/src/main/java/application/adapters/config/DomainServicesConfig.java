package application.adapters.config;

import application.domain.ports.out.AuditLogRepositoryPort;
import application.domain.ports.out.AuthorizationPort;
import application.domain.ports.out.EventPublisherPort;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.OperationRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.services.authorization.AuthorizeActionService;
import application.domain.services.authorization.AuthorizeChangeUserStatusService;
import application.domain.services.authorization.AuthorizeCustomerRegistrationService;
import application.domain.services.authorization.AuthorizeInventoryOperationService;
import application.domain.services.authorization.AuthorizeOrderOperationService;
import application.domain.services.authorization.AuthorizeProductOperationService;
import application.domain.services.authorization.AuthorizeReturnApprovalService;
import application.domain.services.authorization.AuthorizeUserRegistrationService;
import application.domain.services.authorization.RolePermissionManagementService;
import application.domain.services.authorization.ValidateBuyerOwnershipService;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateSellerOwnershipService;
import application.domain.services.authorization.ValidateSupervisorAuthorizationService;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import application.domain.services.billing.AdministrativeReportsConsolidationService;
import application.domain.services.billing.BillingAdministrationService;
import application.domain.services.billing.RefundsManagementService;
import application.domain.services.inventory.CriticalStockValidationService;
import application.domain.services.inventory.DistributedInventoryManagementService;
import application.domain.services.inventory.InventoryAdjustmentsReturnsService;
import application.domain.services.inventory.InventoryEntryRegistrationService;
import application.domain.services.inventory.InventoryReservationService;
import application.domain.services.inventory.SalesExitRegistrationService;
import application.domain.services.inventory.WarehouseClassificationService;
import application.domain.services.inventory.WarehouseInformationControlService;
import application.domain.services.logistics.LogisticsShippingManagementService;
import application.domain.services.logistics.ReturnsAdministrationService;
import application.domain.services.operation.ConsultAuditLogsService;
import application.domain.services.operation.ConsultOperationsService;
import application.domain.services.operation.RegisterAuditLogService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.services.operation.RegisterOperationService;
import application.domain.services.order.CompletedOrderImmutabilityService;
import application.domain.services.order.ExpiredOrderCleanupScheduler;
import application.domain.services.order.OrderStatusMonitoringService;
import application.domain.services.order.ShoppingCartManagementService;
import application.domain.services.product.ProductCatalogManagementService;
import application.domain.services.product.ProductStateControlService;
import application.domain.services.product.ProductTypeDifferentiationService;
import application.domain.services.product.ProductVariantManagementService;
import application.domain.services.user.AuthenticationAccessControlService;
import application.domain.services.user.BuyerCommercialStateControlService;
import application.domain.services.user.BuyerManagementService;
import application.domain.services.user.PrimaryAddressManagementService;
import application.domain.services.user.SecondaryAddressManagementService;
import application.domain.services.user.SellerAdministrativeRegistrationService;
import application.domain.services.user.UniqueIdentityValidationService;
import application.domain.services.user.UserInformationManagementService;
import application.domain.services.user.UserOperationalStateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Hexagonal Architecture configuration adapter registering pure Domain Services
 * into the Spring IoC ApplicationContext without polluting domain classes with framework annotations.
 */
@Configuration
public class DomainServicesConfig {

    @Bean
    public AdministrativeReportsConsolidationService administrativeReportsConsolidationService() {
        return new AdministrativeReportsConsolidationService();
    }

    @Bean
    public AuthenticationAccessControlService authenticationAccessControlService() {
        return new AuthenticationAccessControlService();
    }

    @Bean
    public AuthorizeActionService authorizeActionService(AuthorizationPort authorizationPort) {
        return new AuthorizeActionService(authorizationPort);
    }

    @Bean
    public AuthorizeChangeUserStatusService authorizeChangeUserStatusService() {
        return new AuthorizeChangeUserStatusService();
    }

    @Bean
    public AuthorizeCustomerRegistrationService authorizeCustomerRegistrationService() {
        return new AuthorizeCustomerRegistrationService();
    }

    @Bean
    public AuthorizeInventoryOperationService authorizeInventoryOperationService() {
        return new AuthorizeInventoryOperationService();
    }

    @Bean
    public AuthorizeOrderOperationService authorizeOrderOperationService() {
        return new AuthorizeOrderOperationService();
    }

    @Bean
    public AuthorizeProductOperationService authorizeProductOperationService() {
        return new AuthorizeProductOperationService();
    }

    @Bean
    public AuthorizeReturnApprovalService authorizeReturnApprovalService() {
        return new AuthorizeReturnApprovalService();
    }

    @Bean
    public AuthorizeUserRegistrationService authorizeUserRegistrationService() {
        return new AuthorizeUserRegistrationService();
    }

    @Bean
    public BillingAdministrationService billingAdministrationService() {
        return new BillingAdministrationService();
    }

    @Bean
    public BuyerCommercialStateControlService buyerCommercialStateControlService() {
        return new BuyerCommercialStateControlService();
    }

    @Bean
    public BuyerManagementService buyerManagementService() {
        return new BuyerManagementService();
    }

    @Bean
    public CompletedOrderImmutabilityService completedOrderImmutabilityService() {
        return new CompletedOrderImmutabilityService();
    }

    @Bean
    public ConsultAuditLogsService consultAuditLogsService(AuditLogRepositoryPort auditLogRepositoryPort) {
        return new ConsultAuditLogsService(auditLogRepositoryPort);
    }

    @Bean
    public ConsultOperationsService consultOperationsService(OperationRepositoryPort operationRepositoryPort) {
        return new ConsultOperationsService(operationRepositoryPort);
    }

    @Bean
    public CriticalStockValidationService criticalStockValidationService() {
        return new CriticalStockValidationService();
    }

    @Bean
    public DistributedInventoryManagementService distributedInventoryManagementService() {
        return new DistributedInventoryManagementService();
    }

    @Bean
    public ExpiredOrderCleanupScheduler expiredOrderCleanupScheduler(
            OrderRepositoryPort orderRepositoryPort,
            InventoryRepositoryPort inventoryRepositoryPort,
            InventoryReservationService inventoryReservationService,
            EventPublisherPort eventPublisherPort,
            @Value("${nexusmarket.orders.ttl-minutes:30}") long ttlMinutes) {
        return new ExpiredOrderCleanupScheduler(orderRepositoryPort, inventoryRepositoryPort, inventoryReservationService, eventPublisherPort, ttlMinutes);
    }

    @Bean
    public InventoryAdjustmentsReturnsService inventoryAdjustmentsReturnsService() {
        return new InventoryAdjustmentsReturnsService();
    }

    @Bean
    public InventoryEntryRegistrationService inventoryEntryRegistrationService() {
        return new InventoryEntryRegistrationService();
    }

    @Bean
    public InventoryReservationService inventoryReservationService() {
        return new InventoryReservationService();
    }

    @Bean
    public LogisticsShippingManagementService logisticsShippingManagementService() {
        return new LogisticsShippingManagementService();
    }

    @Bean
    public OrderStatusMonitoringService orderStatusMonitoringService() {
        return new OrderStatusMonitoringService();
    }

    @Bean
    public PrimaryAddressManagementService primaryAddressManagementService() {
        return new PrimaryAddressManagementService();
    }

    @Bean
    public ProductCatalogManagementService productCatalogManagementService() {
        return new ProductCatalogManagementService();
    }

    @Bean
    public ProductStateControlService productStateControlService() {
        return new ProductStateControlService();
    }

    @Bean
    public ProductTypeDifferentiationService productTypeDifferentiationService() {
        return new ProductTypeDifferentiationService();
    }

    @Bean
    public ProductVariantManagementService productVariantManagementService() {
        return new ProductVariantManagementService();
    }

    @Bean
    public RefundsManagementService refundsManagementService() {
        return new RefundsManagementService();
    }

    @Bean
    public RegisterAuditLogService registerAuditLogService(AuditLogRepositoryPort auditLogRepositoryPort) {
        return new RegisterAuditLogService(auditLogRepositoryPort);
    }

    @Bean
    public RegisterOperationAndAuditService registerOperationAndAuditService(RegisterOperationService registerOperationService, RegisterAuditLogService registerAuditLogService) {
        return new RegisterOperationAndAuditService(registerOperationService, registerAuditLogService);
    }

    @Bean
    public RegisterOperationService registerOperationService(OperationRepositoryPort operationRepositoryPort) {
        return new RegisterOperationService(operationRepositoryPort);
    }

    @Bean
    public ReturnsAdministrationService returnsAdministrationService() {
        return new ReturnsAdministrationService();
    }

    @Bean
    public RolePermissionManagementService rolePermissionManagementService() {
        return new RolePermissionManagementService();
    }

    @Bean
    public SalesExitRegistrationService salesExitRegistrationService() {
        return new SalesExitRegistrationService();
    }

    @Bean
    public SecondaryAddressManagementService secondaryAddressManagementService() {
        return new SecondaryAddressManagementService();
    }

    @Bean
    public SellerAdministrativeRegistrationService sellerAdministrativeRegistrationService() {
        return new SellerAdministrativeRegistrationService();
    }

    @Bean
    public ShoppingCartManagementService shoppingCartManagementService() {
        return new ShoppingCartManagementService();
    }

    @Bean
    public UniqueIdentityValidationService uniqueIdentityValidationService() {
        return new UniqueIdentityValidationService();
    }

    @Bean
    public UserInformationManagementService userInformationManagementService() {
        return new UserInformationManagementService();
    }

    @Bean
    public UserOperationalStateService userOperationalStateService() {
        return new UserOperationalStateService();
    }

    @Bean
    public ValidateBuyerOwnershipService validateBuyerOwnershipService() {
        return new ValidateBuyerOwnershipService();
    }

    @Bean
    public ValidateRoleAuthorizationService validateRoleAuthorizationService() {
        return new ValidateRoleAuthorizationService();
    }

    @Bean
    public ValidateSellerOwnershipService validateSellerOwnershipService() {
        return new ValidateSellerOwnershipService();
    }

    @Bean
    public ValidateSupervisorAuthorizationService validateSupervisorAuthorizationService() {
        return new ValidateSupervisorAuthorizationService();
    }

    @Bean
    public ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService() {
        return new ValidateUserAuthorizationStatusService();
    }

    @Bean
    public WarehouseClassificationService warehouseClassificationService() {
        return new WarehouseClassificationService();
    }

    @Bean
    public WarehouseInformationControlService warehouseInformationControlService() {
        return new WarehouseInformationControlService();
    }

}
