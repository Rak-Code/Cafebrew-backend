# Implementation Plan: Extra Ingredients Feature

## Overview

This implementation plan covers the Extra Ingredients feature across three codebases: cafebrew-backend (Java/Spring Boot), cafe-command-center (React admin panel), and cafebrew-orderhub (React customer UI). Tasks are ordered to build incrementally, starting with backend entities and APIs, then admin panel management, and finally customer cart integration.

## Tasks

- [x] 1. Create ExtraIngredient entity and repository
  - Create ExtraIngredient.java entity with id, name, description, price, active, categories (ManyToMany), timestamps
  - Create ExtraIngredientRepository.java with custom queries for finding by category and active status
  - Add bidirectional mapping to Category entity
  - _Requirements: 1.2, 1.3, 1.4, 2.2, 2.3_

- [x] 2. Create OrderItemExtra entity and update OrderItem
  - Create OrderItemExtra.java entity with id, orderItem (ManyToOne), extraIngredientId, extraIngredientName, price
  - Update OrderItem.java to include OneToMany relationship to OrderItemExtra
  - Create OrderItemExtraRepository.java
  - _Requirements: 4.2_

- [x] 3. Create ExtraIngredient DTOs
  - Create CreateExtraIngredientRequest.java with name, description, price, categoryIds
  - Create UpdateExtraIngredientRequest.java with name, description, price, active, categoryIds
  - Create ExtraIngredientResponse.java with all fields including category summaries
  - Create CategorySummaryDto.java for nested category info
  - Create OrderItemExtraResponse.java with fromEntity() method
  - Update AdminOrderItemResponse to include extras list
  - _Requirements: 6.7, 5.4_

- [x] 4. Implement ExtraIngredientService
  - Create ExtraIngredientService.java interface
  - Create ExtraIngredientServiceImpl.java with CRUD operations
  - Implement category mapping logic (add/remove mappings on update)
  - Implement active status filtering for customer queries
  - Implement deletion protection (check for order history)
  - Add validation for name uniqueness, length limits, price positivity
  - _Requirements: 1.2, 1.3, 1.7, 1.8, 1.9, 2.4, 2.5_

- [ ]* 4.1 Write property test for Extra Ingredient Validation
  - **Property 1: Extra Ingredient Validation**
  - **Validates: Requirements 1.2, 1.3**

- [ ]* 4.2 Write property test for Active Status Filtering
  - **Property 2: Active Status Filtering**
  - **Validates: Requirements 1.5, 2.7**

- [ ]* 4.3 Write property test for Category Mapping Integrity
  - **Property 3: Category Mapping Integrity**
  - **Validates: Requirements 2.2, 2.3, 2.4, 2.5**

- [ ]* 4.4 Write property test for Deletion Protection
  - **Property 8: Deletion Protection**
  - **Validates: Requirements 1.8, 1.9**

- [x] 5. Create Admin ExtraIngredient Controller
  - Create AdminExtraIngredientController.java
  - Implement GET /api/admin/extra-ingredients (list all)
  - Implement POST /api/admin/extra-ingredients (create)
  - Implement PUT /api/admin/extra-ingredients/{id} (update)
  - Implement DELETE /api/admin/extra-ingredients/{id} (delete)
  - Add proper authentication/authorization
  - _Requirements: 6.1, 6.2, 6.3, 6.4_

- [x] 6. Create Customer ExtraIngredient Controller
  - Create ExtraIngredientController.java in customer/controller (public endpoints)
  - Implement GET /api/extra-ingredients/by-category/{categoryId}
  - Implement GET /api/extra-ingredients/by-categories?ids=1,2,3
  - Return only active ingredients for active categories
  - _Requirements: 6.5, 6.6_

- [-] 7. Update OrderService for Extra Ingredients
  - [x] 7.1 Update PlaceOrderRequest to include extraIngredientIds per item
    - Add extraIngredientIds field to OrderItemRequest inner class
    - _Requirements: 4.1_
  - [ ] 7.2 Modify OrderService.placeOrder to create OrderItemExtra records
    - Inject ExtraIngredientService into OrderService
    - Fetch extra ingredients by IDs from request
    - Validate that selected extras are active
    - Create OrderItemExtra records with price snapshot
    - Update total calculation to include extras using OrderItem.calculateTotalPrice()
    - _Requirements: 4.1, 4.3, 4.4, 4.5_
  - [ ] 7.3 Update AdminOrderService.toAdminOrderResponse to include extras
    - Modify toAdminOrderResponse to populate extras list from OrderItem.extras
    - Use OrderItemExtraResponse.fromEntity() for conversion
    - _Requirements: 5.4_
  - [ ] 7.4 Update OrderService.toAdminOrderResponse to include extras
    - Ensure WebSocket notifications include extras data
    - _Requirements: 5.4_

- [ ]* 7.5 Write property test for Order Creation with Extras
  - **Property 6: Order Creation with Extras**
  - **Validates: Requirements 4.1, 4.2**

- [ ]* 7.6 Write property test for Order Total Calculation
  - **Property 7: Order Total Calculation**
  - **Validates: Requirements 4.3**

- [ ] 8. Checkpoint - Backend Complete
  - Ensure all backend tests pass
  - Verify API endpoints work correctly
  - Ask the user if questions arise

- [x] 9. Add ExtraIngredient types to Admin Panel
  - Add ExtraIngredient interface to cafe-command-center/src/types/index.ts
  - Add CreateExtraIngredientRequest interface
  - Add UpdateExtraIngredientRequest interface
  - Add CategorySummary interface
  - Add OrderItemExtra interface
  - Update AdminOrderItem to include extras array
  - _Requirements: 1.6, 5.1_

- [x] 10. Create useExtraIngredients hook
  - Create cafe-command-center/src/hooks/useExtraIngredients.ts
  - Implement fetchAll, create, update, delete operations
  - Handle loading, error, and saving states
  - _Requirements: 1.6, 1.7_

- [x] 11. Create ExtraIngredientForm component
  - Create cafe-command-center/src/components/extra-ingredients/ExtraIngredientForm.tsx
  - Include name, description, price fields
  - Add multi-select for categories using existing categories data
  - Add active toggle for edit mode
  - Implement form validation
  - _Requirements: 1.2, 1.3, 2.1_

- [x] 12. Create ExtraIngredientList component
  - Create cafe-command-center/src/components/extra-ingredients/ExtraIngredientList.tsx
  - Display name, price, mapped categories, active status
  - Add edit, delete, toggle active actions
  - _Requirements: 1.6, 2.6_

- [x] 13. Create ExtraIngredientsPage and add navigation
  - Create cafe-command-center/src/pages/ExtraIngredientsPage.tsx
  - Integrate ExtraIngredientList and ExtraIngredientForm
  - Add delete confirmation dialog
  - Add route in App.tsx
  - Add navigation link to sidebar/menu
  - _Requirements: 1.1_

- [x] 14. Update Admin Order Display
  - Update cafe-command-center/src/components/orders/OrderCard.tsx to show extras
  - Display extra ingredient names and prices for each order item in expanded view
  - _Requirements: 5.1, 5.3_

- [ ] 15. Checkpoint - Admin Panel Complete
  - Ensure admin panel extra ingredients management works
  - Verify order display shows extras
  - Ask the user if questions arise

- [x] 16. Add ExtraIngredient types to Customer UI
  - Add ExtraIngredient interface to cafebrew-orderhub/src/types/index.ts
  - Add SelectedExtra interface
  - Update CartItem type to include extras array
  - Update OrderItemRequest to include extraIngredientIds
  - _Requirements: 3.4_

- [ ] 17. Create useExtraIngredients hook for Customer UI
  - Create cafebrew-orderhub/src/hooks/useExtraIngredients.ts
  - Implement fetchByCategories to get extras for cart items
  - Cache results to avoid repeated API calls
  - _Requirements: 3.1_

- [x] 18. Update CartContext for Extra Ingredients
  - Add updateItemExtras method to context
  - Update totalAmount calculation to include extras
  - Update local storage persistence to include extras
  - _Requirements: 3.5, 3.6, 3.7, 3.8_

- [ ]* 18.1 Write property test for Cart Item Total Calculation
  - **Property 4: Cart Item Total Calculation**
  - **Validates: Requirements 3.5, 3.6, 3.7**

- [ ]* 18.2 Write property test for Cart Persistence Round Trip
  - **Property 5: Cart Persistence Round Trip**
  - **Validates: Requirements 3.8**

- [x] 19. Create ExtraIngredientSelector component
  - Create cafebrew-orderhub/src/components/cart/ExtraIngredientSelector.tsx
  - Display available extras with checkboxes and prices
  - Handle selection/deselection
  - Show updated item total
  - _Requirements: 3.2, 3.3, 3.4_

- [x] 20. Update CartItem component
  - Update cafebrew-orderhub/src/components/cart/CartItem.tsx
  - Add "Add Extras" button when extras available for category
  - Integrate ExtraIngredientSelector
  - Display selected extras and updated total
  - _Requirements: 3.2, 3.5, 3.6_

- [x] 21. Update Order Placement
  - Update cafebrew-orderhub/src/pages/CheckoutPage.tsx to include extras in order request
  - Map cart item extras to extraIngredientIds in order items
  - Handle validation errors for inactive extras
  - _Requirements: 4.1, 4.4, 4.5_

- [ ] 22. Final Checkpoint - All Features Complete
  - Ensure all tests pass across all codebases
  - Verify end-to-end flow: admin creates extra → customer adds to cart → order placed → kitchen sees extras
  - Ask the user if questions arise

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties
- The implementation order ensures each step builds on previous work without orphaned code
