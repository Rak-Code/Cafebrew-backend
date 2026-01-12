# Requirements Document

## Introduction

This feature introduces an Extra Ingredients system for CafeBrew that allows administrators to create and manage additional ingredients (like extra cheese, extra toppings, etc.) that customers can add to their cart items. Extra ingredients are mapped to categories (not individual menu items), so they appear as add-on options for all items within those categories. This enables upselling opportunities while maintaining logical groupings (e.g., cheese options only appear for pizza/sandwich categories, not coffee).

## Glossary

- **Extra_Ingredient**: An additional item that can be added to cart items for an extra price (e.g., extra cheese, extra shot of espresso)
- **Category**: A grouping of menu items (e.g., Pizza, Sandwiches, Coffee)
- **Category_Mapping**: The association between an Extra_Ingredient and one or more Categories
- **Cart_Item**: A menu item in the customer's shopping cart
- **Order_Item**: A finalized item in a placed order
- **Order_Item_Extra**: The record of extra ingredients added to an order item
- **Admin_Panel**: The cafe-command-center frontend for administrators
- **Customer_UI**: The cafebrew-orderhub frontend for customers

## Requirements

### Requirement 1: Extra Ingredient Entity Management

**User Story:** As an admin, I want to create and manage extra ingredients with names and prices, so that I can offer add-ons to customers.

#### Acceptance Criteria

1. THE Admin_Panel SHALL provide a dedicated page for managing Extra_Ingredients
2. WHEN an admin creates an Extra_Ingredient, THE System SHALL require a name (unique, max 100 chars) and price (positive decimal)
3. WHEN an admin creates an Extra_Ingredient, THE System SHALL allow optional description (max 500 chars)
4. THE System SHALL allow an Extra_Ingredient to be marked as active or inactive
5. WHEN an Extra_Ingredient is inactive, THE Customer_UI SHALL NOT display it as an option
6. THE Admin_Panel SHALL display a list of all Extra_Ingredients with name, price, mapped categories, and status
7. WHEN an admin edits an Extra_Ingredient, THE System SHALL update the name, description, price, or status
8. WHEN an admin deletes an Extra_Ingredient that has no order history, THE System SHALL remove it from the database
9. IF an admin attempts to delete an Extra_Ingredient with order history, THEN THE System SHALL prevent deletion and show an error message

### Requirement 2: Category Mapping for Extra Ingredients

**User Story:** As an admin, I want to map extra ingredients to specific categories, so that relevant add-ons appear only for appropriate menu items.

#### Acceptance Criteria

1. WHEN creating or editing an Extra_Ingredient, THE Admin_Panel SHALL display a multi-select list of all active categories
2. THE System SHALL allow one Extra_Ingredient to be mapped to multiple categories
3. THE System SHALL allow one Category to have multiple Extra_Ingredients mapped to it
4. WHEN a category is selected during Extra_Ingredient creation, THE System SHALL create the Category_Mapping
5. WHEN a category mapping is removed, THE System SHALL delete the Category_Mapping record
6. THE Admin_Panel SHALL display which categories each Extra_Ingredient is mapped to
7. WHEN a Category is deactivated, THE Customer_UI SHALL NOT show Extra_Ingredients for items in that category

### Requirement 3: Customer Cart Extra Ingredients Selection

**User Story:** As a customer, I want to add extra ingredients to items in my cart, so that I can customize my order.

#### Acceptance Criteria

1. WHEN a customer views the cart page, THE Customer_UI SHALL check if each cart item's category has mapped Extra_Ingredients
2. IF a cart item's category has active Extra_Ingredients, THEN THE Customer_UI SHALL display an "Add Extras" option for that item
3. WHEN a customer clicks "Add Extras", THE Customer_UI SHALL show available Extra_Ingredients with names and prices
4. THE Customer_UI SHALL allow selecting multiple Extra_Ingredients for a single cart item
5. WHEN an Extra_Ingredient is selected, THE Customer_UI SHALL add its price to the cart item's total
6. THE Customer_UI SHALL display the updated item total including selected Extra_Ingredients
7. WHEN an Extra_Ingredient is deselected, THE Customer_UI SHALL subtract its price from the cart item's total
8. THE Cart_Context SHALL persist selected Extra_Ingredients in local storage with the cart

### Requirement 4: Order Placement with Extra Ingredients

**User Story:** As a customer, I want my selected extra ingredients to be included in my order, so that the kitchen knows what to prepare.

#### Acceptance Criteria

1. WHEN placing an order, THE System SHALL include selected Extra_Ingredients for each order item
2. THE Order_Item_Extra entity SHALL store the extra ingredient ID, name, and price at time of order
3. THE System SHALL calculate the total order amount including all Extra_Ingredient prices
4. WHEN an order is placed, THE System SHALL validate that selected Extra_Ingredients are still active
5. IF an Extra_Ingredient becomes inactive before order placement, THEN THE System SHALL reject the order with an error message

### Requirement 5: Kitchen Display of Extra Ingredients

**User Story:** As kitchen staff, I want to see extra ingredients for each order item, so that I can prepare orders correctly.

#### Acceptance Criteria

1. WHEN displaying an order in the Admin_Panel, THE System SHALL show Extra_Ingredients for each order item
2. THE Admin_Panel SHALL display Extra_Ingredient names clearly alongside the menu item name
3. THE Admin_Panel SHALL show the price of each Extra_Ingredient in the order details
4. THE Order_Item response SHALL include a list of Extra_Ingredients with name and price

### Requirement 6: API Endpoints for Extra Ingredients

**User Story:** As a developer, I want well-defined API endpoints for extra ingredients, so that frontend applications can integrate properly.

#### Acceptance Criteria

1. THE Backend SHALL provide GET /api/admin/extra-ingredients endpoint to list all Extra_Ingredients
2. THE Backend SHALL provide POST /api/admin/extra-ingredients endpoint to create an Extra_Ingredient
3. THE Backend SHALL provide PUT /api/admin/extra-ingredients/{id} endpoint to update an Extra_Ingredient
4. THE Backend SHALL provide DELETE /api/admin/extra-ingredients/{id} endpoint to delete an Extra_Ingredient
5. THE Backend SHALL provide GET /api/extra-ingredients/by-category/{categoryId} endpoint for customers to fetch Extra_Ingredients by category
6. THE Backend SHALL provide GET /api/extra-ingredients/by-categories endpoint accepting category IDs to fetch Extra_Ingredients for multiple categories
7. WHEN creating/updating Extra_Ingredient, THE Backend SHALL accept categoryIds array in the request body

