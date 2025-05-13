package org.sid.usersaas;

import org.hibernate.tool.schema.spi.CommandAcceptanceException;
import org.sid.usersaas.entities.UsageType;
import org.sid.usersaas.enums.ServiceCategory;
import org.sid.usersaas.repository.UsageTypeRepository;
import org.sid.usersaas.service.AccountService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;

@SpringBootApplication
public class UserSaaSApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserSaaSApplication.class, args);
    }

    @Bean
    CommandLineRunner initRoles(AccountService accountService) {
        return args -> {
            try {
                accountService.addNewRole("USER");
            } catch (RuntimeException e) {
                System.out.println("USER role already exists");
            }

            try {
                accountService.addNewRole("ADMIN");
            } catch (RuntimeException e) {
                System.out.println("ADMIN role already exists");
            }
        };
    }

    @Bean
    CommandLineRunner initUsageType(UsageTypeRepository usageTypeRepository) {
        return args -> {
            for (ServiceCategory category : ServiceCategory.values()) {

                // Check if a UsageType already exists for this category
                // Requires a method in your UsageTypeRepository interface:
                // boolean existsByServiceCategory(ServiceCategory serviceCategory);
                boolean exists = usageTypeRepository.existsByServiceCategory(category);

                if (!exists) {
                    // If no UsageType exists for this category, create one with a specific price

                    BigDecimal unitPrice;
                    String unit; // Define a default unit per category if needed

                    // --- Determine the price and unit based on the ServiceCategory ---
                    switch (category) {
                        case OTP:
                            unitPrice = new BigDecimal("0.20"); // 0.2 $
                            unit = "OTP"; // Assuming OTP often uses SMS
                            break;
                        case DEMANDE_APPROBATION:
                            unitPrice = new BigDecimal("0.30"); // 0.3 $
                            unit = "Request"; // Or whatever unit makes sense
                            break;
                        case MARKETING:
                            unitPrice = new BigDecimal("0.05"); // 0.05 $
                            unit = "MARKETING"; // Assuming marketing is via SMS, adjust if email/other
                            break;
                        case SMS:
                            // If MARKETING is SMS, maybe this is for transactional SMS?
                            unitPrice = new BigDecimal("0.08"); // Example price, adjust as needed
                            unit = "SMS";
                            break;
                        // Add more cases for any other ServiceCategory values
                        // It's good practice to have a default case
                        default:
                            System.out.println("Warning: No specific price defined for category: " + category.name() + ". Using default price 0.");
                            unitPrice = BigDecimal.ZERO;
                            unit = "unit"; // Generic unit
                            break;
                    }
                    // --- End of price determination ---


                    UsageType usageType = UsageType.builder()
                            // Don't set ID, let the database generate it
                            .id(unit)
                            .name("Using"+ unit) // Set the unit name (e.g., "SMS", "Request")
                            .unitPrice(unitPrice)      // Set the determined price
                            .serviceCategory(category) // Link to the current enum category
                            .build();

                    // Save the usage type to the database
                    usageTypeRepository.save(usageType);
                    System.out.println("Created default UsageType for category: " + category.name() + " with price " + unitPrice + " $/unit.");

                } else {
                    System.out.println("UsageType for category " + category.name() + " already exists.");
                }
            }

            System.out.println("UsageType data loading complete.");
        };
    }
}
