package com.rakeshgupta.cafebrew_backend.config;

import com.rakeshgupta.cafebrew_backend.admin.entity.AdminUser;
import com.rakeshgupta.cafebrew_backend.admin.repository.AdminUserRepository;
import com.rakeshgupta.cafebrew_backend.common.enums.AdminRole;
import com.rakeshgupta.cafebrew_backend.customer.entity.Category;
import com.rakeshgupta.cafebrew_backend.customer.entity.MenuItem;
import com.rakeshgupta.cafebrew_backend.customer.repository.CategoryRepository;
import com.rakeshgupta.cafebrew_backend.customer.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Data Initializer to create default admin users and categories on application startup.
 * Creates an OWNER and STAFF user for admin panel access.
 * Seeds default categories and migrates existing menu items to use Category entities.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;

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

    @Value("${app.categories.default.enabled:true}")
    private boolean defaultCategoriesEnabled;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (defaultAdminEnabled) {
            createDefaultAdminUsers();
        } else {
            log.info("Default admin user creation is disabled.");
        }
        
        if (defaultCategoriesEnabled) {
            createDefaultCategories();
            migrateMenuItemsToCategories();
        } else {
            log.info("Default category creation is disabled.");
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
    
    /**
     * Creates default categories if they don't exist.
     * Default categories: Coffee, Food, Beverages, Desserts
     */
    private void createDefaultCategories() {
        createCategoryIfNotExists("Coffee", "Hot and cold coffee drinks", 1);
        createCategoryIfNotExists("Food", "Sandwiches, pastries, and snacks", 2);
        createCategoryIfNotExists("Beverages", "Non-coffee drinks including tea and smoothies", 3);
        createCategoryIfNotExists("Desserts", "Sweet treats and baked goods", 4);
    }
    
    private void createCategoryIfNotExists(String name, String description, int displayOrder) {
        if (categoryRepository.existsByName(name)) {
            log.info("Category '{}' already exists. Skipping creation.", name);
            return;
        }
        
        Category category = new Category(name, description, displayOrder);
        categoryRepository.save(category);
        log.info("Category '{}' created with display order: {}", name, displayOrder);
    }
    
    /**
     * Migrates existing menu items that have string categories to reference Category entities.
     * This maintains backward compatibility by linking items with legacy category strings
     * to the corresponding Category entity.
     */
    private void migrateMenuItemsToCategories() {
        List<MenuItem> menuItems = menuItemRepository.findAll();
        int migratedCount = 0;
        
        for (MenuItem menuItem : menuItems) {
            // Skip if already has a category entity reference
            if (menuItem.getCategoryEntity() != null) {
                continue;
            }
            
            // Get the legacy category string
            String legacyCategory = menuItem.getCategory();
            if (legacyCategory == null || legacyCategory.isBlank()) {
                log.warn("Menu item '{}' (ID: {}) has no category. Skipping migration.", 
                        menuItem.getName(), menuItem.getId());
                continue;
            }
            
            // Find or create the category
            Optional<Category> categoryOpt = categoryRepository.findByName(legacyCategory);
            Category category;
            
            if (categoryOpt.isPresent()) {
                category = categoryOpt.get();
            } else {
                // Create a new category for this legacy category string
                int nextDisplayOrder = (int) categoryRepository.count() + 1;
                category = new Category(legacyCategory, null, nextDisplayOrder);
                category = categoryRepository.save(category);
                log.info("Created new category '{}' from legacy menu item category", legacyCategory);
            }
            
            // Update the menu item to reference the category entity
            menuItem.setCategoryEntity(category);
            menuItemRepository.save(menuItem);
            migratedCount++;
        }
        
        if (migratedCount > 0) {
            log.info("Migrated {} menu items to use Category entities", migratedCount);
        } else {
            log.info("No menu items needed migration to Category entities");
        }
    }
}