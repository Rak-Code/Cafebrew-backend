## CafeBrew Backend - Project Specification

### 1. Overview
- **Project Name**: CafeBrew Backend
- **Purpose**: Backend API for a cafe ordering system supporting customer ordering, admin management, online payments (Razorpay), and real-time order tracking.
- **Tech Stack**:
  - **Language**: Java 21
  - **Framework**: Spring Boot 3.x (Web, Data JPA, Security, Validation, WebSocket)
  - **Database**: PostgreSQL
  - **Security**: JWT-based authentication for admin endpoints
  - **Messaging**: STOMP over WebSocket for real-time updates
  - **Payments**: Razorpay Java SDK
  - **Logging**: Log4j2 (async logging, configured via `log4j2.xml`)

### 2. Runtime & Configuration
- **Main Application**: `CafebrewBackendApplication`
  - Enables Spring Boot auto-configuration and scheduling (`@EnableScheduling`).
- **Profiles**:
  - Default profile: `default` (configured via `spring.profiles.active=default`).
- **Environment Variables** (see `ENV_SETUP.md` and `.env.example`):
  - **Database**: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (PostgreSQL JDBC URL and credentials).
  - **JWT**: `JWT_SECRET`, `JWT_EXPIRATION` (signing key and token validity in ms).
  - **Default Admin**:
    - `ADMIN_DEFAULT_ENABLED` / `app.admin.default.enabled`
    - `ADMIN_OWNER_USERNAME`, `ADMIN_OWNER_PASSWORD`
    - `ADMIN_STAFF_USERNAME`, `ADMIN_STAFF_PASSWORD`.
  - **CORS**: `CORS_ALLOWED_ORIGINS`.
  - **App URL / Keep-alive**: `APP_URL`, `app.health-check.endpoint`.
  - **Razorpay**: `RAZORPAY_KEY_ID`, `RAZORPAY_KEY_SECRET`.
- **JPA / Hibernate**:
  - `spring.jpa.hibernate.ddl-auto=update` (schema auto-update).
  - SQL logging reduced to warnings for production friendliness.
- **Connection Pool**:
  - HikariCP with tuned pool size and timeouts.
- **Compression**:
  - HTTP response compression enabled for JSON/text responses (`server.compression.*`).

### 3. Security & Authentication
- **Security Configuration** (`SecurityConfig`):
  - Stateless API (`SessionCreationPolicy.STATELESS`).
  - CSRF disabled for APIs.
  - CORS configured via injected `CorsConfigurationSource`.
  - **Public endpoints**:
    - `POST /api/admin/login`
    - `/ws/**` (WebSocket handshake)
    - All `/api/**` endpoints are permitted at the Spring Security layer, but admin-specific behavior is further scoped via path and JWT role claims.
  - **Admin Protection**:
    - `requestMatchers("/api/admin/**")` require roles `ROLE_ADMIN`, `ROLE_OWNER`, or `ROLE_STAFF` (via JWT filter).
  - **JWT Filter**:
    - `JwtAuthenticationFilter` is added before `UsernamePasswordAuthenticationFilter` and uses `JwtTokenProvider` to parse/validate tokens.
- **JWT Token Provider** (`JwtTokenProvider`):
  - Initializes an HMAC-SHA256 `secretKey` from `jwt.secret` and expiration from `jwt.expiration`.
  - **Claims**:
    - `sub` → admin username
    - `role` → one of `ADMIN`, `OWNER`, `STAFF`.
  - Exposes:
    - `generateToken(username, role)`.
    - `validateToken(token)` for signature & expiration.
    - `getUsername(token)` and `getRole(token)` to extract claims.

### 4. WebSocket & Real-time Updates
- **WebSocketConfig**:
  - Enables STOMP message broker with:
    - Broker destination prefix: `/topic`.
    - Application destination prefix: `/app`.
  - STOMP endpoint: `/ws` with SockJS fallback and permissive CORS (`setAllowedOriginPatterns("*")`).
- **Usage in Domain**:
  - `OrderNotificationService` (admin module) broadcasts:
    - New orders.
    - Order status updates.
  - Clients (e.g., admin dashboard) subscribe to `/topic/...` destinations to receive real-time updates.

### 5. Domain Model
- **Core Entities (Customer module)**:
  - **Category**:
    - Fields include: `id`, `name`, `description`, `displayOrder`, `active`, timestamps.
    - Used to group `MenuItem`s and order extra ingredients.
  - **MenuItem**:
    - Fields include: `id`, `name`, `description`, `price`, `available`, `imageUrl`, `category` (string), `categoryEntity` (relation), timestamps.
    - `available` controls customer visibility; admin sees all.
  - **Order**:
    - Fields include: `id`, `orderCode`, `customerName`, `customerPhone`, `status`, `paymentMode`, `paymentStatus`, `totalAmount`, timestamps.
    - One-to-many relation with `OrderItem`s.
  - **OrderItem**:
    - Contains `menuItemId`, `menuItemName`, `price`, `quantity`, `totalPrice`, and relation back to `Order`.
    - May have associated `OrderItemExtra` rows for extra ingredients.
  - **OrderItemExtra**:
    - Represents chosen extra ingredients per order item.
  - **Payment**:
    - Linked one-to-one with `Order`.
    - Fields: `paymentMode`, `paymentStatus`, `amount`, `razorpayOrderId`, `razorpayPaymentId`.
  - **ExtraIngredient**:
    - Represents add-ons (e.g., extra shot, syrups).
    - Fields: `name`, `description`, `price`, `active`.
    - Many-to-many with `Category` (which menu categories can use this extra).
- **Admin Entities**:
  - **AdminUser**:
    - Fields: `username`, `password` (BCrypt), `role` (`AdminRole`), timestamps.
    - Stored in database and used for admin authentication.

### 6. Enums
- **OrderStatus**:
  - `NEW`, `PREPARING`, `READY`, `COMPLETED`, `CANCELLED`.
- **PaymentMode**:
  - `ONLINE`, `COD`.
- **PaymentStatus**:
  - `PENDING`, `PAID`, `FAILED`, `REFUNDED` (though webhook currently maps captured vs non-captured → `PAID` / `FAILED`).
- **AdminRole**:
  - `ADMIN`, `OWNER`, `STAFF`.

### 7. Public (Customer) API
Base path for all customer APIs: `/api`.

- **Menu** (`MenuController`):
  - **GET `/api/menu`**
    - Public, no auth.
    - Returns customer-facing list of available menu items.
    - Delegates to `MenuService.getAvailableMenu()` which filters on `available=true` and active categories.

- **Orders** (`OrderController` / `OrderService`):
  - **POST `/api/orders`**
    - Public, no auth.
    - Request: `PlaceOrderRequest`.
      - Fields include `customerName`, `customerPhone`, `paymentMode` (`ONLINE`/`COD`), and `items` (each with `menuItemId`, `quantity`, and potentially extra ingredients via nested DTOs).
    - Behavior:
      - Validates there is at least one item and quantities are > 0.
      - For each item, loads `MenuItem` via `MenuItemRepository.findByIdAndAvailableTrue`.
      - Computes `totalAmount` as sum of `price * quantity` (and potentially extras via `OrderItemExtra`).
      - Creates `Order` with:
        - Generated `orderCode` (format `ORD-XXXXXXXX`).
        - `status = NEW`, `paymentStatus = PENDING`, `paymentMode` from request.
      - Persists `Order` and `OrderItem`s (`@Transactional`).
      - Sends a WebSocket notification to admin via `OrderNotificationService.notifyNewOrder(...)`.
      - Creates corresponding `Payment` row with `PENDING` status and amount.
      - If `paymentMode == ONLINE`, calls `PaymentService.createOnlinePayment(order)` to create a Razorpay order and store `razorpayOrderId`.
      - Response: `PlaceOrderResponse` containing order code, order status, payment status, total amount, optional `razorpayOrderId`, and message.
  - **GET `/api/orders/track/{orderCode}`**
    - Public, no auth.
    - Looks up `Order` by `orderCode` (via `OrderRepository.findByOrderCode`).
    - Returns `TrackOrderResponse` with order code, customer name, `OrderStatus`, `PaymentStatus`, and items (name + quantity).

- **Payment Webhook** (`PaymentWebhookController` / `PaymentService`):
  - **POST `/api/payments/webhook`**
    - Public endpoint for Razorpay to call.
    - Accepts raw JSON and optional `X-Razorpay-Signature` header.
    - Behavior:
      - Logs receipt of webhook.
      - If signature is present, verifies HMAC-SHA256 signature using `razorpay.key.secret` via `PaymentService.verifyWebhookSignature`.
        - If invalid, returns `400 Bad Request`.
      - Deserializes payload into `PaymentWebhookRequest`.
      - Calls `PaymentService.handleWebhook(payload)`.
        - Reads `order_id`, `id`, `status` from payload.
        - Locates `Payment` by `razorpayOrderId`.
        - On `status == "captured"`:
          - Sets `PaymentStatus.PAID`, stores `razorpayPaymentId`, reflects `PAID` on `Order`.
        - On other statuses:
          - Marks `PaymentStatus.FAILED` on payment and order.
        - Saves both `Payment` and `Order`.
      - Returns `200 OK` or `500` on internal errors.

### 8. Admin API
Base path for admin APIs: `/api/admin`.
All admin endpoints (except login) are protected by JWT with roles `ADMIN`, `OWNER`, `STAFF`.

- **Authentication** (`AdminAuthController` / `AdminAuthService`):
  - **POST `/api/admin/login`**
    - Request: `AdminLoginRequest` (username, password).
    - Looks up `AdminUser` by username.
    - Validates password using `PasswordEncoder` (BCrypt).
    - Generates JWT using `JwtTokenProvider.generateToken(username, role)`.
    - Response: `AdminLoginResponse` with `token` and `role`.
  - **POST `/api/admin/refresh-token`**
    - Requires `Authorization: Bearer <token>` header.
    - Extracts token and validates via `jwtTokenProvider.validateToken`.
    - Extracts username and role, ensures user still exists.
    - Issues a new token and returns `AdminLoginResponse` with refreshed token.

- **Orders** (`AdminOrderController` / `AdminOrderService`):
  - **GET `/api/admin/orders`** with optional parameters:
    - `status` (OrderStatus) – filter by status.
    - `query` – search by customer name or order code.
    - `page`, `size` – for pagination.
    - `paginated` (boolean) – toggles between paginated and non-paginated responses.
  - Behavior:
    - If `query` is provided → `AdminOrderService.searchOrders` with pagination.
    - Else if `paginated=true` → returns `Page<Order>` filtered or not by status.
    - Else → returns non-paginated list (backward compatibility).
    - All results map `Order` → `AdminOrderResponse` with item details.
  - **PUT `/api/admin/orders/{orderId}/status`**
    - Request body: `UpdateOrderStatusRequest` with target `OrderStatus`.
    - Uses `AdminOrderService.updateOrderStatus` which:
      - Validates status transitions using `VALID_TRANSITIONS` map (state machine).
        - NEW → PREPARING / CANCELLED
        - PREPARING → READY / CANCELLED
        - READY → COMPLETED / CANCELLED
        - COMPLETED / CANCELLED → no further transitions.
      - Persists updated status.
      - Notifies clients via WebSocket (`notifyOrderStatusUpdate`).
  - **PUT `/api/admin/orders/{orderId}/complete`**
    - Convenience endpoint to set status directly to `COMPLETED` subject to validation.

- **Menu Management** (`AdminMenuController` / `MenuService`):
  - **GET `/api/admin/menu`**
    - Returns all menu items (including unavailable and items in inactive categories).
  - **GET `/api/admin/menu/{id}`**
    - Returns detailed view of a single menu item (`MenuItemResponse`).
  - **POST `/api/admin/menu`**
    - Creates a new menu item (`CreateMenuItemRequest`).
    - `MenuService.createMenuItem`:
      - Validates category exists and is active.
      - Sets initial `available` flag (defaults to true).
  - **PUT `/api/admin/menu/{id}`**
    - Updates existing menu item (`UpdateMenuItemRequest`).
    - Ensures category exists and is active.
  - **DELETE `/api/admin/menu/{id}`**
    - Deletes a menu item by ID; throws `MenuItemNotFoundException` if not found.
  - **PUT `/api/admin/menu/{id}/availability`**
    - Toggles availability via `ToggleMenuAvailabilityRequest`.

- **Category Management** (`AdminCategoryController` / `CategoryService`):
  - **GET `/api/admin/categories`**
    - Returns all categories (active and inactive), mapping each to `CategoryResponse` enriched with item count.
  - **POST `/api/admin/categories`**
    - Creates a category (`CreateCategoryRequest`).
    - Validates uniqueness of name.
  - **PUT `/api/admin/categories/{id}`**
    - Updates category (`UpdateCategoryRequest`) including name, description, display order, and active flag.
    - Enforces unique name across categories.
  - **DELETE `/api/admin/categories/{id}`**
    - Deletes a category only if no menu items are associated.
    - Uses `MenuItemRepository.countByCategoryEntityId` for this check.
  - **PUT `/api/admin/categories/reorder`**
    - Bulk updates category display order based on list of `CategoryOrderRequest`s.

- **Extra Ingredient Management** (`AdminExtraIngredientController` / `ExtraIngredientServiceImpl`):
  - **GET `/api/admin/extra-ingredients`**
    - Returns all extra ingredients (including inactive) with category mappings.
  - **POST `/api/admin/extra-ingredients`**
    - Creates extra ingredient (`CreateExtraIngredientRequest`).
    - Validates unique name and optionally associates with categories by IDs.
  - **PUT `/api/admin/extra-ingredients/{id}`**
    - Updates extra ingredient (`UpdateExtraIngredientRequest`) including name, price, active flag, and category mappings.
  - **DELETE `/api/admin/extra-ingredients/{id}`**
    - Deletes extra ingredient only if it has no order history (`OrderItemExtraRepository.countByExtraIngredientId`).

### 9. Validation & Business Rules
- **Menu & Categories**:
  - Menu items must belong to existing, active categories for create/update.
  - Categories must have unique names; categories with items cannot be deleted.
  - Extra ingredients must have unique names; inactive or non-existent extras cannot be used in orders.
- **Orders**:
  - Must have at least one item; quantities > 0.
  - Order state is governed by explicit state machines:
    - Customer-facing (in `OrderService.isValidTransition`) and admin-facing (in `AdminOrderService.VALID_TRANSITIONS`), both enforcing forward-only progression and disallowing changes after completion/cancellation.
- **Payments**:
  - Payment record is always created with `PENDING` when order is placed.
  - Razorpay webhook is the single source of truth for final payment status.
  - Signature verification is recommended and implemented when header is present.

### 10. Error Handling
- **GlobalExceptionHandler** (not fully detailed here but present in `config`):
  - Ensures consistent JSON error shape: `timestamp`, `status`, `error`, `message`.
  - Maps validation errors, not-found conditions, and business exceptions (e.g., `CategoryHasItemsException`, `ExtraIngredientHasOrdersException`) to appropriate HTTP status codes.

### 11. Non-functional Aspects
- **Logging**:
  - Log4j2 configuration optimizes logging for Render (async, no file appenders by default).
  - Sensitive data (passwords, secrets) is not logged.
- **Scheduling**:
  - `@EnableScheduling` is enabled; `KeepAliveService` periodically pings the health endpoint to keep the Render free dyno warm.
- **CORS**:
  - Centralized in `CorsConfig`, with allowed origins controlled via `CORS_ALLOWED_ORIGINS` env.

### 12. Typical Flows
- **Customer Online Order Flow**:
  1. Customer fetches menu via `GET /api/menu`.
  2. Customer places order via `POST /api/orders` with `paymentMode=ONLINE`.
  3. Backend creates `Order` + `Payment` (PENDING), Razorpay order, and returns `razorpayOrderId`.
  4. Frontend initiates Razorpay checkout using `razorpayOrderId`.
  5. Razorpay calls `POST /api/payments/webhook` on payment capture.
  6. Backend verifies signature (if present), marks payment as `PAID`/`FAILED`, and updates `Order.paymentStatus`.
  7. Customer tracks order via `GET /api/orders/track/{orderCode}`; admin sees real-time updates via WebSocket.

- **Admin Order Management Flow**:
  1. Admin logs in via `POST /api/admin/login` and receives JWT.
  2. Admin dashboard lists orders via `GET /api/admin/orders` (with filters/search/pagination).
  3. Kitchen staff updates order status through `PUT /api/admin/orders/{id}/status` following allowed transitions.
  4. Real-time updates are pushed to all connected admin clients via WebSocket.

This `specs.md` reflects the current behavior of the CafeBrew backend as implemented in the codebase and can be used as a high-level system and API specification for future development and integrations.

### 13. User & Frontend Experience (Non-Technical Overview)

This section explains CafeBrew in plain language, as if you are walking a non-technical café owner or team member through what the system does for them.

#### 13.1 For Café Customers

- **Browsing the menu**
  - A customer opens your website or app and sees a clean, organized menu: coffees, teas, snacks, etc.
  - Each item shows its name, description, price, and whether it’s currently available.
  - Categories and item order are controlled by you in the admin panel, so customers always see the most important items first.

- **Placing an order**
  - The customer selects items and quantities (e.g., 2 Cappuccinos, 1 Croissant).
  - They can add **extras** where allowed (e.g., extra shot, flavors) which are configured by you.
  - They enter basic details like their name and (optionally) phone number.
  - They choose how to pay:
    - **Online payment** (e.g., card/UPI via Razorpay), or
    - **Cash on Delivery (COD)** / pay at counter.

- **Paying online**
  - If they choose online payment, they’re redirected to the familiar Razorpay payment popup.
  - The amount there is exactly what the system calculated from their order.
  - Once payment completes, Razorpay notifies our backend automatically (via a secure webhook), and the order’s payment status is updated.

- **Tracking their order**
  - When an order is placed, the customer gets a unique **order code** (like `ORD-A1B2C3D4`).
  - They can go to a simple tracking screen (e.g., `/track/{orderCode}`) that shows:
    - Their order items.
    - Where the order is in the process: NEW → PREPARING → READY → COMPLETED.
    - Whether the payment is pending, paid, or failed.
  - They don’t need an account or login – the order code is enough to track.

#### 13.2 For Café Staff / Admins

- **Logging in**
  - Staff or the owner log in to an **admin panel** using a username and password.
  - The system checks their credentials securely and gives them a temporary **access token**.
  - Based on their **role** (OWNER, STAFF, ADMIN), they may have different permissions (e.g., only owner can do certain critical tasks).

- **Managing the menu**
  - In the admin UI, they can:
    - Add new menu items (name, description, price, image, which category they belong to).
    - Mark items as available/unavailable (e.g., if something is out of stock, simply flip a switch).
    - Update item details and change item categories.
    - Remove items that are no longer sold.
  - These actions immediately reflect on the customer side so the menu is always up to date.

- **Managing categories**
  - Staff can create and edit **categories** like “Hot Coffee”, “Cold Coffee”, “Snacks”.
  - They can control **display order**, so the most important categories appear first.
  - They can activate/deactivate categories and only delete a category if it’s no longer used by any items (to avoid breaking the menu).

- **Managing extra ingredients**
  - Staff can define **extras** like extra espresso shot, syrup flavors, or toppings with their own prices.
  - They can choose which menu categories each extra applies to (e.g., “extra shot” for coffee drinks only).
  - Extras can be turned off or removed, but only if they’re not part of existing order history (to keep reports consistent).

- **Seeing and handling orders in real time**
  - When a customer places an order, it appears on the admin/kitchen screen almost instantly.
  - Orders are shown in different views or filters by **status**:
    - NEW: just placed, waiting to be prepared.
    - PREPARING: currently being made.
    - READY: prepared and ready for pickup/delivery.
    - COMPLETED: finished and handed over.
    - CANCELLED: cancelled (if applicable in the UI).
  - Staff move orders along this pipeline, updating status with one click or tap.
  - As they update the status, **all connected screens** (e.g., owner dashboard, other staff stations) update in real time via WebSockets.

- **Payments and issues**
  - For online payments, staff don’t have to manually check Razorpay; the system automatically updates an order’s payment status based on the webhook.
  - If a payment fails, the order is marked accordingly, and staff can decide whether to cancel or ask the customer to retry.

#### 13.3 For the Café Owner / Business Stakeholder

- **What you’re “getting” with this system**
  - A **central backend brain** that:
    - Manages your menu, categories, and extras.
    - Accepts and tracks customer orders.
    - Integrates with online payments.
    - Keeps your staff dashboards in sync in real-time.
  - It’s designed so you can plug in different frontends (web, mobile app, kiosk) all talking to the same backend.

- **Operational benefits**
  - Less manual coordination: staff see orders instantly without phone calls or handwritten tickets.
  - Fewer errors: menu, prices, and availability are controlled from a single source of truth.
  - Clear order flow: you can always see how many orders are NEW, PREPARING, READY, or COMPLETED.
  - Easy expansion: you can add new items or categories without changing the underlying backend logic.

- **Technical independence**
  - All of this is exposed via stable APIs.
  - That means you can redesign the frontend (website/mobile) or switch vendors without rewriting the business logic, order rules, or payment handling.

In short, CafeBrew provides the invisible “control center” behind your café’s digital experience: customers see a smooth menu and ordering journey, staff see a live order queue, and the owner gets a consistent, reliable system that can grow with the business.