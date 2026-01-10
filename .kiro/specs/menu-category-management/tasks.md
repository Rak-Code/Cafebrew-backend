# Implementation Plan: Menu and Category Management

## Overview

This implementation plan breaks down the menu and category management feature into discrete coding tasks. The approach is backend-first, establishing the data model and APIs before implementing the frontend components. Tasks are ordered to ensure incremental progress with no orphaned code.

## Tasks

- [x] 1. Create Category entity and repository
  - Create Category.java entity with all fields (id, name, description, displayOrder, active, timestamps)
  - Create CategoryRepository.java with custom query methods
  - Add database migration or let JPA auto-create the table
  - _Requirements: 1.1_

- [x] 2. Create Category DTOs and exceptions
  - Create CreateCategoryRequest.java with validation annotations
  - Create UpdateCategoryRequest.java with validation annotations
  - Create CategoryOrderRequest.java for reordering
  - Create CategoryResponse.java for API responses
  - Create CategoryHasItemsException.java
  - Create DuplicateCategoryNameException.java
  - Create CategoryNotFoundException.java
  - _Requirements: 9.1, 9.2_

- [x] 3. Implement CategoryService
  - [x] 3.1 Create CategoryService.java with all CRUD methods
    - Implement getAllCategories() - returns all categories sorted by displayOrder
    - Implement getActiveCategories() - returns only active categories
    - Implement getCategoryById(Long id)
    - Implement createCategory(CreateCategoryRequest request)
    - Implement updateCategory(Long id, UpdateCategoryRequest request)
    - Implement deleteCategory(Long id) - with item count check
    - Implement reorderCategories(List<CategoryOrderRequest> requests)
    - _Requirements: 1.2, 1.3, 1.4, 1.5, 1.6, 2.6_

  - [ ]* 3.2 Write property test for Category CRUD round-trip
    - **Property 1: Category CRUD Round-Trip**
    - **Validates: Requirements 1.1, 1.3**

  - [ ]* 3.3 Write property test for Category name uniqueness
    - **Property 2: Category Name Uniqueness**
    - **Validates: Requirements 1.2, 9.5**

  - [ ]* 3.4 Write property test for Category display order sorting
    - **Property 3: Category Display Order Sorting**
    - **Validates: Requirements 1.5, 1.6**

- [x] 4. Create Category controllers
  - [x] 4.1 Create AdminCategoryController.java
    - GET /api/admin/categories - list all categories
    - POST /api/admin/categories - create category
    - PUT /api/admin/categories/{id} - update category
    - DELETE /api/admin/categories/{id} - delete category
    - PUT /api/admin/categories/reorder - reorder categories
    - _Requirements: 2.1, 2.2, 2.3, 2.4_

  - [x] 4.2 Create public CategoryController.java
    - GET /api/categories - list active categories
    - _Requirements: 2.5_

- [x] 5. Update MenuItem entity and repository
  - [x] 5.1 Update MenuItem.java to use Category foreign key
    - Add @ManyToOne relationship to Category
    - Keep backward compatibility during migration
    - _Requirements: 8.1_

  - [x] 5.2 Update MenuItemRepository.java
    - Add query methods that join with Category
    - Update existing queries to filter by active category
    - _Requirements: 8.4_

- [x] 6. Create Menu Item DTOs
  - Create CreateMenuItemRequest.java with validation annotations
  - Create UpdateMenuItemRequest.java with validation annotations
  - Create/Update MenuItemResponse.java to include categoryId and categoryName
  - Create MenuItemNotFoundException.java
  - _Requirements: 4.5, 8.2_

- [x] 7. Extend MenuService with CRUD operations
  - [x] 7.1 Add CRUD methods to MenuService.java
    - Implement getMenuItemById(Long id)
    - Implement createMenuItem(CreateMenuItemRequest request)
    - Implement updateMenuItem(Long id, UpdateMenuItemRequest request)
    - Implement deleteMenuItem(Long id)
    - Update getAvailableMenu() to filter by active category
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_

  - [ ]* 7.2 Write property test for Menu Item CRUD round-trip
    - **Property 6: Menu Item CRUD Round-Trip**
    - **Validates: Requirements 3.1, 3.2, 3.3, 4.5**

  - [ ]* 7.3 Write property test for Menu Item validation
    - **Property 7: Menu Item Validation**
    - **Validates: Requirements 3.4, 3.5, 9.1**

  - [ ]* 7.4 Write property test for Field length validation
    - **Property 8: Field Length Validation**
    - **Validates: Requirements 9.4, 9.5, 9.6**

- [x] 8. Update AdminMenuController with CRUD endpoints
  - Add GET /api/admin/menu/{id} - get single menu item
  - Add POST /api/admin/menu - create menu item
  - Add PUT /api/admin/menu/{id} - update menu item
  - Add DELETE /api/admin/menu/{id} - delete menu item
  - _Requirements: 4.1, 4.2, 4.3, 4.4_

- [x] 9. Update global exception handler
  - Add handlers for CategoryHasItemsException
  - Add handlers for DuplicateCategoryNameException
  - Add handlers for CategoryNotFoundException
  - Add handlers for MenuItemNotFoundException
  - Ensure consistent error response format
  - _Requirements: 9.1, 9.2, 9.3_

- [ ]* 10. Write property test for Error response format
  - **Property 9: Error Response Format**
  - **Validates: Requirements 9.1, 9.2, 9.3**

- [x] 11. Create DataInitializer for categories
  - Migrate existing string categories to Category entities
  - Seed default categories (Coffee, Food, Beverages, Desserts)
  - Update existing menu items to reference new Category entities
  - _Requirements: 8.3_

- [ ]* 12. Write property tests for category-menu item relationship
  - [ ]* 12.1 Write property test for Category deactivation hides menu items
    - **Property 4: Category Deactivation Hides Menu Items**
    - **Validates: Requirements 1.4, 8.4**

  - [ ]* 12.2 Write property test for Category deletion prevention
    - **Property 5: Category Deletion Prevention**
    - **Validates: Requirements 2.6**

  - [ ]* 12.3 Write property test for Timestamp auto-management
    - **Property 10: Timestamp Auto-Management**
    - **Validates: Requirements 3.6**

  - [ ]* 12.4 Write property test for Menu item response includes category name
    - **Property 11: Menu Item Response Includes Category Name**
    - **Validates: Requirements 8.2**

- [ ] 13. Checkpoint - Backend complete
  - Ensure all backend tests pass, ask the user if questions arise.

- [x] 14. Update Admin Panel types and API client
  - [x] 14.1 Add Category types to cafe-command-center/src/types/index.ts
    - Add Category interface
    - Add CreateCategoryRequest interface
    - Add UpdateCategoryRequest interface
    - Update MenuItem interface with categoryId and categoryName
    - _Requirements: 5.1, 6.3_

  - [x] 14.2 Add category API methods to cafe-command-center/src/api/endpoints.ts
    - Add categoriesApi.getAll()
    - Add categoriesApi.create()
    - Add categoriesApi.update()
    - Add categoriesApi.delete()
    - Add categoriesApi.reorder()
    - _Requirements: 5.1_

  - [x] 14.3 Add menu item CRUD methods to menuApi
    - Add menuApi.getById()
    - Add menuApi.create()
    - Add menuApi.update()
    - Add menuApi.delete()
    - _Requirements: 6.1, 6.2_

- [x] 15. Create useCategories hook
  - Create cafe-command-center/src/hooks/useCategories.ts
  - Implement fetch, create, update, delete, reorder operations
  - Handle loading and error states
  - _Requirements: 5.1_

- [x] 16. Extend useMenu hook with CRUD operations
  - Add createMenuItem function
  - Add updateMenuItem function
  - Add deleteMenuItem function
  - Update state management for CRUD operations
  - _Requirements: 6.1, 6.2, 6.6_

- [x] 17. Create Category management UI components
  - [x] 17.1 Create CategoryCard.tsx component
    - Display category name, description, status, item count
    - Include toggle switch for active status
    - Include edit and delete buttons
    - _Requirements: 5.1, 5.4, 5.5_

  - [x] 17.2 Create CategoryForm.tsx component
    - Form fields for name, description, displayOrder
    - Validation for required fields and max lengths
    - Support both create and edit modes
    - _Requirements: 5.2, 5.3_

  - [x] 17.3 Create CategoryList.tsx component
    - Display categories in a sortable list
    - Support drag-and-drop reordering
    - _Requirements: 5.1_

- [x] 18. Create CategoriesPage.tsx
  - Create cafe-command-center/src/pages/CategoriesPage.tsx
  - Integrate CategoryList, CategoryForm components
  - Add create category button and modal
  - Handle delete confirmation with item count warning
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6_

- [x] 19. Create Menu Item management UI components
  - [x] 19.1 Create MenuItemForm.tsx component
    - Form fields for name, description, category, price, availability, imageUrl
    - Category dropdown populated from categories API
    - Validation for required fields and positive price
    - Support both create and edit modes
    - _Requirements: 6.1, 6.2, 6.3, 6.4_

  - [x] 19.2 Create DeleteConfirmDialog.tsx component
    - Confirmation dialog for delete operations
    - Display item name being deleted
    - _Requirements: 6.6_

- [x] 20. Update MenuManagePage.tsx
  - Add "Add Menu Item" button
  - Integrate MenuItemForm modal for create/edit
  - Add edit button to MenuItemCard
  - Add delete button with confirmation dialog
  - Show success/error toast notifications
  - _Requirements: 6.1, 6.2, 6.5, 6.6_

- [ ]* 21. Write property test for Admin Panel form validation
  - **Property 8: Field Length Validation (Frontend)**
  - **Validates: Requirements 6.4**

- [x] 22. Add routing for CategoriesPage
  - Add route to cafe-command-center/src/App.tsx router
  - Add navigation link in Navbar.tsx
  - _Requirements: 5.1_

- [ ] 23. Checkpoint - Admin Panel complete
  - Ensure all admin panel functionality works, ask the user if questions arise.

- [x] 24. Update Customer App types and API
  - [x] 24.1 Update cafebrew-orderhub/src/types/index.ts
    - Add Category interface
    - Update MenuCategory type to be dynamic (string instead of union type)
    - Update MenuItem interface with categoryId and categoryName
    - _Requirements: 7.1, 7.2_

  - [x] 24.2 Add getCategories to customerApi
    - Add customerApi.getCategories() method
    - _Requirements: 7.1_

- [x] 25. Update Customer App useMenu hook
  - Fetch categories from API instead of hardcoded list
  - Handle empty categories gracefully
  - Update filtering logic to use dynamic categories
  - Remove mock data fallback for categories
  - _Requirements: 7.1, 7.2, 7.4_

- [x] 26. Update CategoryFilter component
  - Render categories dynamically from API response
  - Handle loading state
  - Show all items if no categories available
  - _Requirements: 7.2, 7.3, 7.4_

- [ ]* 27. Write property test for Customer App category filtering
  - **Property 4: Category Deactivation Hides Menu Items (Frontend)**
  - **Validates: Requirements 7.2**

- [ ] 28. Final checkpoint - All tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties
- Unit tests validate specific examples and edge cases
- Backend tasks (1-13) should be completed before frontend tasks (14-27)


## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties
- Unit tests validate specific examples and edge cases
- Backend tasks (1-13) should be completed before frontend tasks (14-28)
- Tasks 1-18 are complete (backend + admin panel category management)
- Remaining work: Menu item CRUD UI (19-20), routing (22), customer app integration (24-26)
