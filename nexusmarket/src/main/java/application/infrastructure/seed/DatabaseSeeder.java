package application.infrastructure.seed;

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
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Currency;
import java.util.UUID;

/**
 * Enterprise database seeder executing on startup to initialize default roles, administrators, warehouses and demo catalog products.
 */
@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final UserRepositoryPort userRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final InventoryRepositoryPort inventoryRepositoryPort;

    public DatabaseSeeder(UserRepositoryPort userRepositoryPort,
                          ProductRepositoryPort productRepositoryPort,
                          WarehouseRepositoryPort warehouseRepositoryPort,
                          InventoryRepositoryPort inventoryRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.productRepositoryPort = productRepositoryPort;
        this.warehouseRepositoryPort = warehouseRepositoryPort;
        this.inventoryRepositoryPort = inventoryRepositoryPort;
    }

    @Override
    public void run(String... args) {
        seedDefaultUsers();
        seedWarehousesAndProducts();
    }

    private void seedDefaultUsers() {
        // Administrator
        Email adminEmail = new Email("admin@nexusmarket.com");
        if (userRepositoryPort.findByEmail(adminEmail).isEmpty()) {
            User admin = User.createStaff(
                    "NexusMarket Root Administrator",
                    adminEmail,
                    new IdentityDocument("CC", "1000000001"),
                    UserRole.ADMINISTRATOR
            );
            userRepositoryPort.save(admin);
            log.info("Seeded default administrator: {}", adminEmail.value());
        }

        // Supervisor
        Email supervisorEmail = new Email("supervisor@nexusmarket.com");
        if (userRepositoryPort.findByEmail(supervisorEmail).isEmpty()) {
            User supervisor = User.createStaff(
                    "NexusMarket Quality Supervisor",
                    supervisorEmail,
                    new IdentityDocument("CC", "1000000002"),
                    UserRole.SUPERVISOR
            );
            userRepositoryPort.save(supervisor);
            log.info("Seeded default supervisor: {}", supervisorEmail.value());
        }

        // Logistics Operator
        Email logisticsEmail = new Email("logistics@nexusmarket.com");
        if (userRepositoryPort.findByEmail(logisticsEmail).isEmpty()) {
            User logistics = User.createStaff(
                    "NexusMarket Logistics Lead",
                    logisticsEmail,
                    new IdentityDocument("CC", "1000000003"),
                    UserRole.LOGISTICS_OPERATOR
            );
            userRepositoryPort.save(logistics);
            log.info("Seeded default logistics operator: {}", logisticsEmail.value());
        }

        // Seller
        Email sellerEmail = new Email("seller@nexusmarket.com");
        if (userRepositoryPort.findByEmail(sellerEmail).isEmpty()) {
            Seller seller = Seller.create(
                    UUID.randomUUID(),
                    "NexusMarket Certified Merchant",
                    sellerEmail,
                    new IdentityDocument("NIT", "900123456-1"),
                    LocalDate.now()
            );
            userRepositoryPort.save(seller);
            log.info("Seeded default seller: {}", sellerEmail.value());
        }

        // Buyer
        Email buyerEmail = new Email("buyer@nexusmarket.com");
        if (userRepositoryPort.findByEmail(buyerEmail).isEmpty()) {
            Address buyerAddress = new Address("Avenida El Dorado 100", "Bogotá", "Cundinamarca", "Colombia", "110111", "Torre 2 Apt 405");
            Buyer buyer = Buyer.create(
                    "NexusMarket Prime Buyer",
                    buyerEmail,
                    new IdentityDocument("CC", "1000000005"),
                    buyerAddress,
                    Collections.emptyList(),
                    BuyerCommercialStatus.ENABLED
            );
            userRepositoryPort.save(buyer);
            log.info("Seeded default buyer: {}", buyerEmail.value());
        }
    }

    private void seedWarehousesAndProducts() {
        Currency cop = Currency.getInstance("COP");

        // Seed Marketplace Central Warehouse
        Warehouse centralWarehouse = null;
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

        // Seed Products & Inventory
        if (productRepositoryPort.findAll().isEmpty()) {
            Product p1 = new Product(
                    UUID.randomUUID(),
                    "MacBook Pro M3 Max",
                    new ProductVariant("Storage", "1TB SSD"),
                    new Money(new BigDecimal("14500000"), cop),
                    ProductStatus.PUBLISHED,
                    ProductType.PHYSICAL
            );

            Product p2 = new Product(
                    UUID.randomUUID(),
                    "Licencia Nexus Cloud Enterprise",
                    new ProductVariant("Tier", "Annual Subscription"),
                    new Money(new BigDecimal("2800000"), cop),
                    ProductStatus.PUBLISHED,
                    ProductType.DIGITAL
            );

            p1 = productRepositoryPort.save(p1);
            p2 = productRepositoryPort.save(p2);
            log.info("Seeded initial demo products for NexusMarket catalog.");

            if (centralWarehouse != null && inventoryRepositoryPort.findAll().isEmpty()) {
                Inventory inventory = Inventory.create(p1, centralWarehouse, 50);
                inventoryRepositoryPort.save(inventory);
                log.info("Seeded initial inventory for physical product.");
            }
        }
    }
}
