# CafeBrew Backend - Frontend Integration Guide

## Project Overview

CafeBrew is a cafe ordering system backend built with Spring Boot. It supports:
- Customer-facing menu browsing and order placement
- Admin panel for order management and menu control
- Online payments via Razorpay integration
- Real-time order tracking

## Base URL

```
http://localhost:8080/api
```

---

## Authentication

### JWT Token Authentication

Admin endpoints require JWT Bearer token authentication.

**Header Format:**
```
Authorization: Bearer <jwt_token>
```

**Token Payload:**
```json
{
  "sub": "username",
  "role": "OWNER|STAFF|ADMIN",
  "iat": 1234567890,
  "exp": 1234567890
}
```

---

## Enums Reference

### OrderStatus
| Value | Description |
|-------|-------------|
| `NEW` | Order just placed |
| `PREPARING` | Kitchen is preparing |
| `READY` | Ready for pickup |
| `COMPLETED` | Order delivered/picked up |
| `CANCELLED` | Order cancelled |

### PaymentMode
| Value | Description |
|-------|-------------|
| `ONLINE` | Razorpay payment |
| `COD` | Cash on delivery |

### PaymentStatus
| Value | Description |
|-------|-------------|
| `PENDING` | Awaiting payment |
| `PAID` | Payment successful |
| `FAILED` | Payment failed |
| `REFUNDED` | Payment refunded |

### AdminRole
| Value | Description |
|-------|-------------|
| `ADMIN` | Full access |
| `OWNER` | Owner privileges |
| `STAFF` | Staff privileges |

---

## Public Endpoints (No Auth Required)

### 1. Get Menu

Fetches all available menu items for customers.

```
GET /api/menu
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "Cappuccino",
    "description": "Classic Italian coffee",
    "category": "Coffee",
    "price": 150.00,
    "available": true,
    "createdAt": "2025-01-07T10:00:00",
    "updatedAt": "2025-01-07T10:00:00"
  }
]
```

**Frontend Usage:**
```typescript
const fetchMenu = async () => {
  const response = await fetch('/api/menu');
  const menuItems = await response.json();
  return menuItems;
};
```

---

### 2. Place Order

Creates a new order with items.

```
POST /api/orders
```

**Request Body:**
```json
{
  "customerName": "John Doe",
  "paymentMode": "ONLINE",
  "items": [
    {
      "menuItemId": 1,
      "quantity": 2
    },
    {
      "menuItemId": 3,
      "quantity": 1
    }
  ]
}
```

**Validation Rules:**
- `customerName`: Required, max 50 characters
- `paymentMode`: Required, must be `ONLINE` or `COD`
- `items`: Required, at least 1 item
- `items[].menuItemId`: Required
- `items[].quantity`: Required, minimum 1

**Response:** `201 Created`
```json
{
  "orderCode": "ORD-A1B2C3D4",
  "orderStatus": "NEW",
  "paymentStatus": "PENDING",
  "totalAmount": 450.00,
  "razorpayOrderId": "rzp_order_123456789",
  "message": "Order placed successfully"
}
```

**Note:** `razorpayOrderId` is only returned when `paymentMode` is `ONLINE`.

**Frontend Usage:**
```typescript
interface PlaceOrderRequest {
  customerName: string;
  paymentMode: 'ONLINE' | 'COD';
  items: Array<{
    menuItemId: number;
    quantity: number;
  }>;
}

const placeOrder = async (order: PlaceOrderRequest) => {
  const response = await fetch('/api/orders', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(order)
  });
  return response.json();
};
```

---

### 3. Track Order

Track order status using order code (no authentication needed).

```
GET /api/orders/track/{orderCode}
```

**Response:** `200 OK`
```json
{
  "orderCode": "ORD-A1B2C3D4",
  "customerName": "John Doe",
  "status": "PREPARING",
  "paymentStatus": "PAID",
  "items": [
    {
      "name": "Cappuccino",
      "quantity": 2
    },
    {
      "name": "Croissant",
      "quantity": 1
    }
  ]
}
```

**Frontend Usage:**
```typescript
const trackOrder = async (orderCode: string) => {
  const response = await fetch(`/api/orders/track/${orderCode}`);
  return response.json();
};
```

---

## Admin Endpoints (Auth Required)

All admin endpoints require JWT Bearer token with `OWNER` or `STAFF` role.

### 1. Admin Login

Authenticate admin user and receive JWT token.

```
POST /api/admin/login
```

**Request Body:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Validation Rules:**
- `username`: Required, max 50 characters
- `password`: Required, 6-100 characters

**Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "role": "OWNER"
}
```

**Frontend Usage:**
```typescript
const adminLogin = async (username: string, password: string) => {
  const response = await fetch('/api/admin/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  });
  const data = await response.json();
  
  // Store token for subsequent requests
  localStorage.setItem('adminToken', data.token);
  localStorage.setItem('adminRole', data.role);
  
  return data;
};
```

---

### 2. Get Orders by Status

Fetch orders filtered by status (for kitchen display/order queue).

```
GET /api/admin/orders?status={status}
```

**Query Parameters:**
| Parameter | Type | Required | Values |
|-----------|------|----------|--------|
| `status` | string | Yes | `NEW`, `PREPARING`, `READY`, `COMPLETED`, `CANCELLED` |

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response:** `200 OK`
```json
[
  {
    "orderId": 1,
    "orderCode": "ORD-A1B2C3D4",
    "customerName": "John Doe",
    "status": "NEW",
    "totalAmount": 450.00,
    "createdAt": "2025-01-07T10:30:00"
  }
]
```

**Frontend Usage:**
```typescript
const getOrdersByStatus = async (status: string) => {
  const token = localStorage.getItem('adminToken');
  const response = await fetch(`/api/admin/orders?status=${status}`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  return response.json();
};

// Kitchen display - fetch NEW orders
const newOrders = await getOrdersByStatus('NEW');

// Ready queue - fetch READY orders
const readyOrders = await getOrdersByStatus('READY');
```

---

### 3. Update Order Status

Update order status (follows state machine rules).

```
PUT /api/admin/orders/{orderId}/status
```

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Request Body:**
```json
{
  "status": "PREPARING"
}
```

**Response:** `200 OK` (empty body)

**State Machine Rules:**
```
NEW → PREPARING → READY → COMPLETED
```

**Frontend Usage:**
```typescript
const updateOrderStatus = async (orderId: number, status: string) => {
  const token = localStorage.getItem('adminToken');
  const response = await fetch(`/api/admin/orders/${orderId}/status`, {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ status })
  });
  return response.ok;
};
```

---

### 4. Complete Order (Shortcut)

Quick endpoint to mark order as completed.

```
PUT /api/admin/orders/{orderId}/complete
```

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response:** `200 OK` (empty body)

**Frontend Usage:**
```typescript
const completeOrder = async (orderId: number) => {
  const token = localStorage.getItem('adminToken');
  const response = await fetch(`/api/admin/orders/${orderId}/complete`, {
    method: 'PUT',
    headers: { 'Authorization': `Bearer ${token}` }
  });
  return response.ok;
};
```

---

### 5. Toggle Menu Item Availability

Enable/disable menu items.

```
PUT /api/admin/menu/{id}/availability
```

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Request Body:**
```json
{
  "available": false
}
```

**Response:** `200 OK` (empty body)

**Frontend Usage:**
```typescript
const toggleMenuAvailability = async (menuItemId: number, available: boolean) => {
  const token = localStorage.getItem('adminToken');
  const response = await fetch(`/api/admin/menu/${menuItemId}/availability`, {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ available })
  });
  return response.ok;
};
```

---

## Payment Integration (Razorpay)

### Webhook Endpoint

```
POST /api/payments/webhook
```

**Headers:**
```
X-Razorpay-Signature: <signature>
```

**Request Body (from Razorpay):**
```json
{
  "event": "payment.captured",
  "payload": {
    "payment": {
      "entity": {
        "order_id": "rzp_order_123456789",
        "id": "pay_ABC123",
        "status": "captured"
      }
    }
  }
}
```

### Frontend Razorpay Integration

```typescript
// After placing order with ONLINE payment mode
const initiatePayment = (orderResponse: PlaceOrderResponse) => {
  const options = {
    key: 'YOUR_RAZORPAY_KEY_ID',
    amount: orderResponse.totalAmount * 100, // in paise
    currency: 'INR',
    order_id: orderResponse.razorpayOrderId,
    name: 'CafeBrew',
    description: `Order ${orderResponse.orderCode}`,
    handler: function(response: any) {
      // Payment successful - order status will be updated via webhook
      console.log('Payment ID:', response.razorpay_payment_id);
      // Redirect to order tracking page
      window.location.href = `/track/${orderResponse.orderCode}`;
    },
    prefill: {
      name: 'Customer Name',
      email: 'customer@example.com'
    }
  };
  
  const rzp = new Razorpay(options);
  rzp.open();
};
```

---

## Error Handling

### Error Response Format

```json
{
  "timestamp": "2025-01-07T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Customer name is required"
}
```

### Common HTTP Status Codes

| Code | Meaning | When |
|------|---------|------|
| `200` | OK | Successful GET/PUT |
| `201` | Created | Successful POST (order created) |
| `400` | Bad Request | Validation errors |
| `401` | Unauthorized | Missing/invalid JWT token |
| `403` | Forbidden | Insufficient role permissions |
| `404` | Not Found | Resource not found |
| `500` | Server Error | Internal server error |

### Frontend Error Handler

```typescript
const apiRequest = async (url: string, options: RequestInit = {}) => {
  try {
    const response = await fetch(url, options);
    
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Request failed');
    }
    
    // Handle empty responses (204, or empty 200)
    const text = await response.text();
    return text ? JSON.parse(text) : null;
    
  } catch (error) {
    console.error('API Error:', error);
    throw error;
  }
};
```

---

## TypeScript Interfaces

```typescript
// Enums
type OrderStatus = 'NEW' | 'PREPARING' | 'READY' | 'COMPLETED' | 'CANCELLED';
type PaymentMode = 'ONLINE' | 'COD';
type PaymentStatus = 'PENDING' | 'PAID' | 'FAILED' | 'REFUNDED';
type AdminRole = 'ADMIN' | 'OWNER' | 'STAFF';

// Menu
interface MenuItem {
  id: number;
  name: string;
  description: string;
  category: string;
  price: number;
  available: boolean;
  createdAt: string;
  updatedAt: string;
}

// Order Request
interface PlaceOrderRequest {
  customerName: string;
  paymentMode: PaymentMode;
  items: OrderItemRequest[];
}

interface OrderItemRequest {
  menuItemId: number;
  quantity: number;
}

// Order Response
interface PlaceOrderResponse {
  orderCode: string;
  orderStatus: OrderStatus;
  paymentStatus: PaymentStatus;
  totalAmount: number;
  razorpayOrderId?: string;
  message: string;
}

// Track Order Response
interface TrackOrderResponse {
  orderCode: string;
  customerName: string;
  status: OrderStatus;
  paymentStatus: PaymentStatus;
  items: OrderItemResponse[];
}

interface OrderItemResponse {
  name: string;
  quantity: number;
}

// Admin
interface AdminLoginRequest {
  username: string;
  password: string;
}

interface AdminLoginResponse {
  token: string;
  role: AdminRole;
}

interface AdminOrderResponse {
  orderId: number;
  orderCode: string;
  customerName: string;
  status: OrderStatus;
  totalAmount: number;
  createdAt: string;
}

interface UpdateOrderStatusRequest {
  status: OrderStatus;
}

interface ToggleMenuAvailabilityRequest {
  available: boolean;
}

// Error
interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
}
```

---

## Data Models (Database Schema)

### MenuItem
| Field | Type | Description |
|-------|------|-------------|
| id | Long | Primary key |
| name | String | Item name |
| description | String | Item description |
| category | String | Category (Coffee, Food, etc.) |
| price | BigDecimal | Price |
| available | Boolean | Availability status |
| createdAt | DateTime | Creation timestamp |
| updatedAt | DateTime | Last update timestamp |

### Order
| Field | Type | Description |
|-------|------|-------------|
| id | Long | Primary key |
| orderCode | String | Unique order code (ORD-XXXXXXXX) |
| customerName | String | Customer name |
| status | OrderStatus | Order status |
| paymentMode | PaymentMode | Payment method |
| paymentStatus | PaymentStatus | Payment status |
| totalAmount | BigDecimal | Total order amount |
| items | List<OrderItem> | Order items |
| createdAt | DateTime | Creation timestamp |
| updatedAt | DateTime | Last update timestamp |

### OrderItem
| Field | Type | Description |
|-------|------|-------------|
| id | Long | Primary key |
| order | Order | Parent order |
| menuItemId | Long | Menu item reference |
| menuItemName | String | Item name (snapshot) |
| price | BigDecimal | Unit price (snapshot) |
| quantity | Integer | Quantity ordered |
| totalPrice | BigDecimal | Line total |

### Payment
| Field | Type | Description |
|-------|------|-------------|
| id | Long | Primary key |
| order | Order | Associated order |
| paymentMode | PaymentMode | Payment method |
| paymentStatus | PaymentStatus | Payment status |
| razorpayOrderId | String | Razorpay order ID |
| razorpayPaymentId | String | Razorpay payment ID |
| amount | BigDecimal | Payment amount |
| createdAt | DateTime | Creation timestamp |

### AdminUser
| Field | Type | Description |
|-------|------|-------------|
| id | Long | Primary key |
| username | String | Unique username |
| password | String | BCrypt encoded password |
| role | AdminRole | User role |
| createdAt | DateTime | Creation timestamp |

---

## Frontend App Structure Suggestion

```
src/
├── api/
│   ├── client.ts          # Base API client with auth
│   ├── menu.ts            # Menu API calls
│   ├── orders.ts          # Order API calls
│   └── admin.ts           # Admin API calls
├── types/
│   └── index.ts           # TypeScript interfaces
├── hooks/
│   ├── useMenu.ts         # Menu data hook
│   ├── useOrder.ts        # Order operations hook
│   └── useAuth.ts         # Admin auth hook
├── pages/
│   ├── customer/
│   │   ├── Menu.tsx       # Menu browsing
│   │   ├── Cart.tsx       # Shopping cart
│   │   ├── Checkout.tsx   # Order placement
│   │   └── Track.tsx      # Order tracking
│   └── admin/
│       ├── Login.tsx      # Admin login
│       ├── Dashboard.tsx  # Order queue
│       ├── Kitchen.tsx    # Kitchen display
│       └── Menu.tsx       # Menu management
└── components/
    ├── MenuItem.tsx
    ├── OrderCard.tsx
    └── StatusBadge.tsx
```

---

## Quick Start Checklist

### Customer App
- [ ] Fetch and display menu (`GET /api/menu`)
- [ ] Implement cart functionality (local state)
- [ ] Place order (`POST /api/orders`)
- [ ] Integrate Razorpay for online payments
- [ ] Order tracking page (`GET /api/orders/track/{code}`)

### Admin App
- [ ] Login page (`POST /api/admin/login`)
- [ ] Store JWT token securely
- [ ] Kitchen display - NEW orders (`GET /api/admin/orders?status=NEW`)
- [ ] Update order status (`PUT /api/admin/orders/{id}/status`)
- [ ] Menu management - toggle availability (`PUT /api/admin/menu/{id}/availability`)

---

## Environment Variables

Backend requires these environment variables:

```env
# Database
DB_URL=jdbc:postgresql://localhost:5432/cafebrew
DB_USERNAME=postgres
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your-256-bit-secret-key-here
JWT_EXPIRATION=86400000

# Default Admin
ADMIN_DEFAULT_USERNAME=admin
ADMIN_DEFAULT_PASSWORD=admin123
ADMIN_DEFAULT_ENABLED=true
```

---

## CORS Configuration

If frontend runs on different port/domain, ensure CORS is configured in backend. Add to `SecurityConfig.java`:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

---

## API Summary Table

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/menu` | No | Get available menu |
| POST | `/api/orders` | No | Place new order |
| GET | `/api/orders/track/{code}` | No | Track order |
| POST | `/api/admin/login` | No | Admin login |
| GET | `/api/admin/orders?status=X` | Yes | Get orders by status |
| PUT | `/api/admin/orders/{id}/status` | Yes | Update order status |
| PUT | `/api/admin/orders/{id}/complete` | Yes | Complete order |
| PUT | `/api/admin/menu/{id}/availability` | Yes | Toggle menu availability |
| POST | `/api/payments/webhook` | No* | Razorpay webhook |

*Webhook should verify Razorpay signature
