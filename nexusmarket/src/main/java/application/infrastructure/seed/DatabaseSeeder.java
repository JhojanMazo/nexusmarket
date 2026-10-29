package application.infrastructure.seed;

import application.adapters.security.BCryptPasswordServiceAdapter;
import application.domain.enums.BuyerCommercialStatus;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.enums.UserRole;
import application.domain.enums.WarehouseType;
import application.domain.models.Buyer;
import application.domain.models.Inventory;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.PasswordServicePort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Currency;
import java.util.UUID;

/**
 * Enterprise database seeder executing on startup to initialize default roles, administrators,
 * warehouses (Marketplace & Seller) and demo catalog products with real persisted inventory.
 */
@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final UserRepositoryPort userRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final PasswordServicePort passwordServicePort;

    @Autowired
    public DatabaseSeeder(UserRepositoryPort userRepositoryPort,
                          ProductRepositoryPort productRepositoryPort,
                          WarehouseRepositoryPort warehouseRepositoryPort,
                          InventoryRepositoryPort inventoryRepositoryPort,
                          PasswordServicePort passwordServicePort) {
        this.userRepositoryPort = userRepositoryPort;
        this.productRepositoryPort = productRepositoryPort;
        this.warehouseRepositoryPort = warehouseRepositoryPort;
        this.inventoryRepositoryPort = inventoryRepositoryPort;
        this.passwordServicePort = passwordServicePort != null ? passwordServicePort : new BCryptPasswordServiceAdapter();
    }

    public DatabaseSeeder(UserRepositoryPort userRepositoryPort,
                          ProductRepositoryPort productRepositoryPort,
                          WarehouseRepositoryPort warehouseRepositoryPort,
                          InventoryRepositoryPort inventoryRepositoryPort) {
        this(userRepositoryPort, productRepositoryPort, warehouseRepositoryPort, inventoryRepositoryPort, new BCryptPasswordServiceAdapter());
    }

    @Override
    public void run(String... args) {
        Seller defaultSeller = seedDefaultUsers();
        seedWarehousesAndProducts(defaultSeller);
    }

    private Seller seedDefaultUsers() {
        String defaultPasswordHash = passwordServicePort.encode("NexusMarket2025!");

        // Administrator
        Email adminEmail = new Email("admin@nexusmarket.com");
        var adminOpt = userRepositoryPort.findByEmail(adminEmail);
        if (adminOpt.isEmpty()) {
            User admin = User.createStaff(
                    "NexusMarket Root Administrator",
                    adminEmail,
                    new IdentityDocument("CC", "1000000001"),
                    UserRole.ADMINISTRATOR,
                    defaultPasswordHash
            );
            userRepositoryPort.save(admin);
            log.info("Seeded default administrator: {}", adminEmail.value());
        } else if (adminOpt.get().getPasswordHash() == null) {
            User admin = adminOpt.get();
            admin.setPasswordHash(defaultPasswordHash);
            userRepositoryPort.save(admin);
        }

        // Supervisor
        Email supervisorEmail = new Email("supervisor@nexusmarket.com");
        var supervisorOpt = userRepositoryPort.findByEmail(supervisorEmail);
        if (supervisorOpt.isEmpty()) {
            User supervisor = User.createStaff(
                    "NexusMarket Quality Supervisor",
                    supervisorEmail,
                    new IdentityDocument("CC", "1000000002"),
                    UserRole.SUPERVISOR,
                    defaultPasswordHash
            );
            userRepositoryPort.save(supervisor);
            log.info("Seeded default supervisor: {}", supervisorEmail.value());
        } else if (supervisorOpt.get().getPasswordHash() == null) {
            User supervisor = supervisorOpt.get();
            supervisor.setPasswordHash(defaultPasswordHash);
            userRepositoryPort.save(supervisor);
        }

        // Logistics Operator
        Email logisticsEmail = new Email("logistics@nexusmarket.com");
        var logisticsOpt = userRepositoryPort.findByEmail(logisticsEmail);
        if (logisticsOpt.isEmpty()) {
            User logistics = User.createStaff(
                    "NexusMarket Logistics Lead",
                    logisticsEmail,
                    new IdentityDocument("CC", "1000000003"),
                    UserRole.LOGISTICS_OPERATOR,
                    defaultPasswordHash
            );
            userRepositoryPort.save(logistics);
            log.info("Seeded default logistics operator: {}", logisticsEmail.value());
        } else if (logisticsOpt.get().getPasswordHash() == null) {
            User logistics = logisticsOpt.get();
            logistics.setPasswordHash(defaultPasswordHash);
            userRepositoryPort.save(logistics);
        }

        // Seller
        Email sellerEmail = new Email("seller@nexusmarket.com");
        Seller seller;
        var existingSeller = userRepositoryPort.findByEmail(sellerEmail);
        if (existingSeller.isEmpty()) {
            seller = Seller.create(
                    UUID.randomUUID(),
                    "NexusMarket Certified Merchant",
                    sellerEmail,
                    new IdentityDocument("NIT", "900123456-1"),
                    LocalDate.now()
            );
            seller.setPasswordHash(defaultPasswordHash);
            seller = (Seller) userRepositoryPort.save(seller);
            log.info("Seeded default seller: {}", sellerEmail.value());
        } else {
            seller = (Seller) existingSeller.get();
            if (seller.getPasswordHash() == null) {
                seller.setPasswordHash(defaultPasswordHash);
                userRepositoryPort.save(seller);
            }
        }

        // Buyer
        Email buyerEmail = new Email("buyer@nexusmarket.com");
        var buyerOpt = userRepositoryPort.findByEmail(buyerEmail);
        if (buyerOpt.isEmpty()) {
            Address buyerAddress = new Address("Avenida El Dorado 100", "Bogotá", "Cundinamarca", "Colombia", "110111", "Torre 2 Apt 405");
            Buyer buyer = Buyer.create(
                    "NexusMarket Prime Buyer",
                    buyerEmail,
                    new IdentityDocument("CC", "1000000005"),
                    buyerAddress,
                    Collections.emptyList(),
                    BuyerCommercialStatus.ENABLED,
                    defaultPasswordHash
            );
            userRepositoryPort.save(buyer);
            log.info("Seeded default buyer: {}", buyerEmail.value());
        } else if (buyerOpt.get().getPasswordHash() == null) {
            Buyer buyer = (Buyer) buyerOpt.get();
            buyer.setPasswordHash(defaultPasswordHash);
            userRepositoryPort.save(buyer);
        }

        return seller;
    }

    private void seedWarehousesAndProducts(Seller defaultSeller) {
        Currency cop = Currency.getInstance("COP");

        Warehouse centralWarehouse;
        if (warehouseRepositoryPort.findAll().isEmpty()) {
            Address centralAddress = new Address("Calle 26 No. 68B-70", "Bogotá", "Cundinamarca", "Colombia", "110931", "Hub Logístico Central");
            centralWarehouse = new Warehouse(
                    UUID.randomUUID(),
                    "Bodega Central Marketplace",
                    centralAddress,
                    WarehouseType.MARKETPLACE,
                    null,
                    50000.0
            );
            centralWarehouse = warehouseRepositoryPort.save(centralWarehouse);
            log.info("Seeded central marketplace warehouse.");
        } else {
            centralWarehouse = warehouseRepositoryPort.findAll().get(0);
        }

        Warehouse sellerWarehouse = null;
        if (defaultSeller != null) {
            var sellerWarehouses = warehouseRepositoryPort.findBySellerId(defaultSeller.getUserId());
            if (sellerWarehouses.isEmpty()) {
                Address sellerWhAddress = new Address("Carrera 43A No. 1-50", "Medellín", "Antioquia", "Colombia", "050021", "Centro Empresarial El Poblado");
                sellerWarehouse = new Warehouse(
                        UUID.randomUUID(),
                        "Bodega Principal Merchant",
                        sellerWhAddress,
                        WarehouseType.SELLER,
                        defaultSeller.getUserId(),
                        15000.0
                );
                sellerWarehouse = warehouseRepositoryPort.save(sellerWarehouse);
                log.info("Seeded seller warehouse for default merchant.");
            } else {
                sellerWarehouse = sellerWarehouses.get(0);
            }
        }

        if (productRepositoryPort.findAll().isEmpty()) {
            Product p1 = productRepositoryPort.save(new Product(
                    UUID.randomUUID(),
                    "MacBook Pro M3 Max",
                    new ProductVariant("Almacenamiento", "1TB SSD / 36GB RAM"),
                    new Money(new BigDecimal("14500000"), cop),
                    ProductStatus.PUBLISHED,
                    ProductType.PHYSICAL
            ));

            Product p2 = productRepositoryPort.save(new Product(
                    UUID.randomUUID(),
                    "Licencia Nexus Cloud Enterprise",
                    new ProductVariant("Suscripción", "Anual Multi-Región"),
                    new Money(new BigDecimal("2800000"), cop),
                    ProductStatus.PUBLISHED,
                    ProductType.DIGITAL
            ));

            Product p3 = productRepositoryPort.save(new Product(
                    UUID.randomUUID(),
                    "Monitor UltraWide 4K OLED 34\"",
                    new ProductVariant("Panel", "240Hz HDR TrueBlack"),
                    new Money(new BigDecimal("4950000"), cop),
                    ProductStatus.PUBLISHED,
                    ProductType.PHYSICAL
            ));

            Product p4 = productRepositoryPort.save(new Product(
                    UUID.randomUUID(),
                    "Teclado Mecánico Pro Wireless",
                    new ProductVariant("Switch", "Táctil Silencioso RGB"),
                    new Money(new BigDecimal("680000"), cop),
                    ProductStatus.PUBLISHED,
                    ProductType.PHYSICAL
            ));

            Product p5 = productRepositoryPort.save(new Product(
                    UUID.randomUUID(),
                    "Suite Seguridad DevSecOps Pro",
                    new ProductVariant("Edición", "Licencia Corporativa 25 Nodos"),
                    new Money(new BigDecimal("3600000"), cop),
                    ProductStatus.PUBLISHED,
                    ProductType.DIGITAL
            ));

            Product p6 = productRepositoryPort.save(new Product(
                    UUID.randomUUID(),
                    "Auriculares Studio Noise-Cancelling",
                    new ProductVariant("Color", "Negro Titanio Hi-Res"),
                    new Money(new BigDecimal("1450000"), cop),
                    ProductStatus.PUBLISHED,
                    ProductType.PHYSICAL
            ));

            log.info("Seeded 6 initial demo products for NexusMarket catalog.");

            if (inventoryRepositoryPort.findAll().isEmpty()) {
                Warehouse targetWh = sellerWarehouse != null ? sellerWarehouse : centralWarehouse;
                inventoryRepositoryPort.save(Inventory.create(p1, centralWarehouse, 50));
                inventoryRepositoryPort.save(Inventory.create(p3, targetWh, 35));
                inventoryRepositoryPort.save(Inventory.create(p4, targetWh, 120));
                inventoryRepositoryPort.save(Inventory.create(p6, centralWarehouse, 80));
                log.info("Seeded initial inventory lots for physical products.");
            }
        }
    }
}
