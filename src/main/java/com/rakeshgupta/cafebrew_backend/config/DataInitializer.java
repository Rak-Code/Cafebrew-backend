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
 * Data Initializer to create default admin users on application startup.
 * Creates an OWNER and STAFF user for admin panel access.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.owner.username:owner}")
    private String ownerUsername;

    @Value("${app.admin.owner.password:owner123}")
    private String ownerPassword;

    @Value("${app.admin.staff.username:staff}")
    private String staffUsername;

    @Value("${app.admin.staff.password:staff123}")
    private String staffPassword;

    @Value("${app.admin.default.enabled:true}")
    private boolean defaultAdminEnabled;

    @Override
    public void run(String... args) throws Exception {
        if (defaultAdminEnabled) {
            createDefaultAdminUsers();
        } else {
            log.info("Default admin user creation is disabled.");
        }
    }

    private void createDefaultAdminUsers() {
        createUserIfNotExists(ownerUsername, ownerPassword, AdminRole.OWNER);
        createUserIfNotExists(staffUsername, staffPassword, AdminRole.STAFF);
    }

    private void createUserIfNotExists(String username, String password, AdminRole role) {
        if (adminUserRepository.findByUsername(username).isPresent()) {
            log.info("Admin user '{}' already exists. Skipping creation.", username);
            return;
        }

        AdminUser adminUser = new AdminUser(
                username,
                passwordEncoder.encode(password),
                role
        );

        adminUserRepository.save(adminUser);
        log.info("Admin user '{}' created with role: {}", username, role);
        log.warn("Please change the default password for '{}' in production!", username);
    }
}