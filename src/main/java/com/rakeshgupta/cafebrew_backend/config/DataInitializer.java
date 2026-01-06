package com.rakeshgupta.cafebrew_backend.config;

import com.rakeshgupta.cafebrew_backend.admin.entity.AdminUser;
import com.rakeshgupta.cafebrew_backend.admin.repository.AdminUserRepository;
import com.rakeshgupta.cafebrew_backend.common.enums.AdminRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Data Initializer to create default admin user on application startup
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.default.username:admin}")
    private String defaultUsername;

    @Value("${app.admin.default.password:admin123}")
    private String defaultPassword;

    @Value("${app.admin.default.enabled:true}")
    private boolean defaultAdminEnabled;

    @Override
    public void run(String... args) throws Exception {
        if (defaultAdminEnabled) {
            createDefaultAdminUser();
        } else {
            log.info("Default admin user creation is disabled.");
        }
    }

    private void createDefaultAdminUser() {
        // Check if admin user already exists
        if (adminUserRepository.findByUsername(defaultUsername).isPresent()) {
            log.info("Default admin user '{}' already exists. Skipping creation.", defaultUsername);
            return;
        }

        // Create default admin user
        AdminUser adminUser = new AdminUser(
                defaultUsername,
                passwordEncoder.encode(defaultPassword),
                AdminRole.ADMIN
        );

        adminUserRepository.save(adminUser);
        log.info("Default admin user created successfully with username: '{}'", defaultUsername);
        log.warn("Please change the default password for security purposes!");
    }
}