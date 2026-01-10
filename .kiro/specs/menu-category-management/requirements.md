# Requirements Document

## Introduction

This feature adds comprehensive menu item and category management capabilities to the CafeBrew admin panel. Currently, menu items can only be added via direct database inserts, and categories are hardcoded strings. This feature will enable admins and owners to dynamically manage menu items (create, update, delete) and categories through the admin panel, with proper backend APIs and frontend interfaces.

## Glossary

- **Admin_Panel**: The cafe-command-center frontend application used by staff and owners to manage the cafe
- **Menu_Item**: A product offered by the cafe (e.g., Cappuccino, Croissant) with name, description, price, category, availability, and optional image
- **Category**: A grouping for menu items (e.g., Coffee, Food, Beverages, Desserts) that can be dynamically created and managed
- **Admin_User**: An authenticated user with OWNER or STAFF role who can access the admin panel
- **Customer_App**: The cafebrew-orderhub frontend application used by customers to browse menu and place orders

## Requirements

### Requirement 1: Category Entity and Management

**User Story:** As an admin, I want to manage menu categories dynamically, so that I can organize menu items without requiring database changes.

#### Acceptance Criteria

1. THE Backend SHALL store categories as a separate entity with id, name, description, displayOrder, and active status
2. WHEN an admin creates a new category, THE Backend SHALL validate that the category name is unique and not empty
3. WHEN an admin updates a category, THE Backend SHALL persist the changes and update all associated menu items
4. WHEN an admin deactivates a category, THE Backend SHALL hide all menu items in that category from the customer app
5. THE Backend SHALL provide an endpoint to list all categories sorted by displayOrder
6. THE Backend SHALL provide an endpoint to reorder categories by updating displayOrder values

### Requirement 2: Category API Endpoints

**User Story:** As a frontend developer, I want RESTful API endpoints for category management, so that I can build the admin UI.

#### Acceptance Criteria

1. THE Backend SHALL expose `GET /api/admin/categories` to list all categories (requires authentication)
2. THE Backend SHALL expose `POST /api/admin/categories` to create a new category (requires authentication)
3. THE Backend SHALL expose `PUT /api/admin/categories/{id}` to update a category (requires authentication)
4. THE Backend SHALL expose `DELETE /api/admin/categories/{id}` to delete a category (requires authentication)
5. THE Backend SHALL expose `GET /api/categories` as a public endpoint to list active categories for customers
6. IF a category has associated menu items, THEN THE Backend SHALL prevent deletion and return an appropriate error

### Requirement 3: Menu Item CRUD Operations

**User Story:** As an admin, I want to create, update, and delete menu items through the admin panel, so that I can manage the cafe menu without database access.

#### Acceptance Criteria

1. WHEN an admin creates a menu item, THE Backend SHALL validate required fields (name, price, categoryId) and persist the item
2. WHEN an admin updates a menu item, THE Backend SHALL validate the data and persist changes
3. WHEN an admin deletes a menu item, THE Backend SHALL remove it from the database
4. THE Backend SHALL validate that price is a positive number
5. THE Backend SHALL validate that the referenced category exists and is active
6. WHEN a menu item is created or updated, THE Backend SHALL automatically set createdAt and updatedAt timestamps

### Requirement 4: Menu Item API Endpoints

**User Story:** As a frontend developer, I want RESTful API endpoints for menu item management, so that I can build the admin UI.

#### Acceptance Criteria

1. THE Backend SHALL expose `POST /api/admin/menu` to create a new menu item (requires authentication)
2. THE Backend SHALL expose `PUT /api/admin/menu/{id}` to update a menu item (requires authentication)
3. THE Backend SHALL expose `DELETE /api/admin/menu/{id}` to delete a menu item (requires authentication)
4. THE Backend SHALL expose `GET /api/admin/menu/{id}` to get a single menu item details (requires authentication)
5. WHEN creating or updating a menu item, THE Backend SHALL return the complete menu item object in the response

### Requirement 5: Admin Panel Category Management UI

**User Story:** As an admin, I want a user interface to manage categories, so that I can organize the menu structure.

#### Acceptance Criteria

1. WHEN an admin navigates to category management, THE Admin_Panel SHALL display a list of all categories with their status
2. THE Admin_Panel SHALL provide a form to create new categories with name, description, and display order
3. THE Admin_Panel SHALL provide inline editing or a modal to update existing categories
4. THE Admin_Panel SHALL allow admins to toggle category active status
5. THE Admin_Panel SHALL show the count of menu items in each category
6. THE Admin_Panel SHALL prevent deletion of categories that have menu items and show an appropriate message

### Requirement 6: Admin Panel Menu Item Management UI

**User Story:** As an admin, I want a user interface to create and edit menu items, so that I can manage the cafe offerings.

#### Acceptance Criteria

1. THE Admin_Panel SHALL provide a form/modal to create new menu items with all required fields
2. THE Admin_Panel SHALL provide a form/modal to edit existing menu items
3. THE Admin_Panel SHALL display a category dropdown populated from the categories API
4. THE Admin_Panel SHALL validate form inputs before submission (required fields, positive price)
5. THE Admin_Panel SHALL show success/error feedback after create/update/delete operations
6. THE Admin_Panel SHALL allow admins to delete menu items with a confirmation dialog

### Requirement 7: Customer App Category Integration

**User Story:** As a customer, I want to see dynamically managed categories, so that I can browse the menu by current offerings.

#### Acceptance Criteria

1. WHEN the customer app loads the menu, THE Customer_App SHALL fetch categories from the public categories endpoint
2. THE Customer_App SHALL display only active categories in the category filter
3. THE Customer_App SHALL update the category filter dynamically based on API response
4. IF no categories are returned, THEN THE Customer_App SHALL show all items without category filtering

### Requirement 8: Menu Item-Category Relationship

**User Story:** As a system architect, I want menu items properly linked to categories, so that the data model supports dynamic category management.

#### Acceptance Criteria

1. THE Backend SHALL update the MenuItem entity to reference Category by foreign key instead of string
2. WHEN fetching menu items, THE Backend SHALL include the category name in the response for display purposes
3. THE Backend SHALL maintain backward compatibility by migrating existing string categories to the new Category entity
4. WHEN a category is deactivated, THE Backend SHALL exclude its menu items from the public menu endpoint

### Requirement 9: Data Validation and Error Handling

**User Story:** As a developer, I want consistent validation and error responses, so that the frontend can handle errors gracefully.

#### Acceptance Criteria

1. WHEN validation fails, THE Backend SHALL return a 400 status with descriptive error messages
2. WHEN a resource is not found, THE Backend SHALL return a 404 status with an appropriate message
3. WHEN an unauthorized request is made, THE Backend SHALL return a 401 status
4. THE Backend SHALL validate that menu item names are not empty and not longer than 100 characters
5. THE Backend SHALL validate that category names are not empty and not longer than 50 characters
6. THE Backend SHALL validate that descriptions are not longer than 500 characters
