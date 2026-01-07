# CafeBrew Customer Frontend Integration Guide

## Overview

This document covers all API endpoints needed for the **Customer-facing UI**. This frontend requires **NO authentication** - all endpoints are public.

## Base URL

```
http://localhost:8080/api
```

---

## API Endpoints

### 1. Get Menu

Fetches all available menu items for display.

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **Endpoint** | `/api/menu` |
| **Auth Required** | No |
| **Content-Type** | - |

**Request:** No body required

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
    "imageUrl": "https://example.com/images/cappuccino.jpg",
    "createdAt": "2025-01-07T10:00:00",
    "updatedAt": "2025-01-07T10:00:00"
  },
  {
    "id": 2,
    "name": "Croissant",
    "description": "Freshly baked butter croissant",
    "category": "Food",
    "price": 120.00,
    "available": true,
    "imageUrl": "https://example.com/images/croissant.jpg",
    "createdAt": "2025-01-07T10:00:00",
    "updatedAt": "2025-01-07T10:00:00"
  }
]
```

**Usage Example:**
```typescript
const fetchMenu = async (): Promise<MenuItem[]> => {
  const response = await fetch('/api/menu');
  if (!response.ok) throw new Error('Failed to fetch menu');
  return response.json();
};
```

---

### 2. Place Order

Creates a new order with selected items.

| Property | Value |
|----------|-------|
| **Method** | `POST` |
| **Endpoint** | `/api/orders` |
| **Auth Required** | No |
| **Content-Type** | `application/json` |

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
| Field | Rule |
|-------|------|
| `customerName` | Required, max 50 characters |
| `paymentMode` | Required, must be `ONLINE` or `COD` |
| `items` | Required, at least 1 item |
| `items[].menuItemId` | Required |
| `items[].quantity` | Required, minimum 1 |

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

> **Note:** `razorpayOrderId` is only returned when `paymentMode` is `ONLINE`.

**Usage Example:**
```typescript
const placeOrder = async (order: PlaceOrderRequest): Promise<PlaceOrderResponse> => {
  const response = await fetch('/api/orders', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(order)
  });
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message);
  }
  return response.json();
};
```

---

### 3. Track Order

Track order status using order code.

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **Endpoint** | `/api/orders/track/{orderCode}` |
| **Auth Required** | No |
| **Content-Type** | - |

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `orderCode` | string | Order code (e.g., `ORD-A1B2C3D4`) |

**Request:** No body required

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

**Usage Example:**
```typescript
const trackOrder = async (orderCode: string): Promise<TrackOrderResponse> => {
  const response = await fetch(`/api/orders/track/${orderCode}`);
  if (!response.ok) throw new Error('Order not found');
  return response.json();
};
```

---

## Razorpay Payment Integration

When customer selects `ONLINE` payment, integrate Razorpay checkout.

**Step 1:** Place order with `paymentMode: "ONLINE"`

**Step 2:** Use returned `razorpayOrderId` to open Razorpay checkout

```typescript
// Include Razorpay script in HTML
// <script src="https://checkout.razorpay.com/v1/checkout.js"></script>

declare const Razorpay: any;

const initiatePayment = (orderResponse: PlaceOrderResponse, customerName: string) => {
  const options = {
    key: 'YOUR_RAZORPAY_KEY_ID', // Get from environment
    amount: orderResponse.totalAmount * 100, // Convert to paise
    currency: 'INR',
    order_id: orderResponse.razorpayOrderId,
    name: 'CafeBrew',
    description: `Order ${orderResponse.orderCode}`,
    handler: function(response: RazorpayResponse) {
      // Payment successful
      // Backend webhook will update order status automatically
      console.log('Payment ID:', response.razorpay_payment_id);
      // Redirect to tracking page
      window.location.href = `/track/${orderResponse.orderCode}`;
    },
    prefill: {
      name: customerName
    },
    theme: {
      color: '#6F4E37' // Coffee brown theme
    }
  };
  
  const rzp = new Razorpay(options);
  rzp.on('payment.failed', function(response: any) {
    alert('Payment failed. Please try again.');
  });
  rzp.open();
};

interface RazorpayResponse {
  razorpay_payment_id: string;
  razorpay_order_id: string;
  razorpay_signature: string;
}
```

---

## TypeScript Interfaces

```typescript
// Enums
type OrderStatus = 'NEW' | 'PREPARING' | 'READY' | 'COMPLETED' | 'CANCELLED';
type PaymentMode = 'ONLINE' | 'COD';
type PaymentStatus = 'PENDING' | 'PAID' | 'FAILED' | 'REFUNDED';

// Menu Item
interface MenuItem {
  id: number;
  name: string;
  description: string;
  category: string;
  price: number;
  available: boolean;
  imageUrl?: string;
  createdAt: string;
  updatedAt: string;
}

// Place Order Request
interface PlaceOrderRequest {
  customerName: string;
  paymentMode: PaymentMode;
  items: OrderItemRequest[];
}

interface OrderItemRequest {
  menuItemId: number;
  quantity: number;
}

// Place Order Response
interface PlaceOrderResponse {
  orderCode: string;
  orderStatus: OrderStatus;
  paymentStatus: PaymentStatus;
  totalAmount: number;
  razorpayOrderId?: string; // Only for ONLINE payments
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

// Error Response
interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
}

// Cart Item (Frontend only)
interface CartItem {
  menuItem: MenuItem;
  quantity: number;
}
```

---

## Error Handling

**Error Response Format:**
```json
{
  "timestamp": "2025-01-07T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Customer name is required"
}
```

**HTTP Status Codes:**
| Code | Meaning |
|------|---------|
| `200` | Success |
| `201` | Order created |
| `400` | Validation error |
| `404` | Order not found |
| `500` | Server error |

**Error Handler Utility:**
```typescript
class ApiError extends Error {
  status: number;
  
  constructor(message: string, status: number) {
    super(message);
    this.status = status;
  }
}

const apiRequest = async <T>(url: string, options: RequestInit = {}): Promise<T> => {
  const response = await fetch(url, options);
  
  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'Request failed' }));
    throw new ApiError(error.message, response.status);
  }
  
  const text = await response.text();
  return text ? JSON.parse(text) : null;
};
```

---

## API Service Module

Complete API service for customer frontend:

```typescript
// api/customerApi.ts

const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

export const customerApi = {
  // Get all menu items
  getMenu: async (): Promise<MenuItem[]> => {
    const response = await fetch(`${BASE_URL}/menu`);
    if (!response.ok) throw new Error('Failed to fetch menu');
    return response.json();
  },

  // Place a new order
  placeOrder: async (order: PlaceOrderRequest): Promise<PlaceOrderResponse> => {
    const response = await fetch(`${BASE_URL}/orders`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(order)
    });
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to place order');
    }
    return response.json();
  },

  // Track order by code
  trackOrder: async (orderCode: string): Promise<TrackOrderResponse> => {
    const response = await fetch(`${BASE_URL}/orders/track/${orderCode}`);
    if (!response.ok) {
      if (response.status === 404) throw new Error('Order not found');
      throw new Error('Failed to track order');
    }
    return response.json();
  }
};
```

---

## Suggested Page Structure

```
src/
├── api/
│   └── customerApi.ts       # API calls
├── types/
│   └── index.ts             # TypeScript interfaces
├── pages/
│   ├── MenuPage.tsx         # Browse menu, add to cart
│   ├── CartPage.tsx         # View cart, proceed to checkout
│   ├── CheckoutPage.tsx     # Enter name, select payment, place order
│   └── TrackOrderPage.tsx   # Track order status
├── components/
│   ├── MenuItemCard.tsx     # Single menu item display
│   ├── CartItemRow.tsx      # Cart item with quantity controls
│   ├── OrderSummary.tsx     # Order total display
│   └── StatusTracker.tsx    # Order status progress indicator
├── hooks/
│   ├── useMenu.ts           # Fetch and cache menu
│   ├── useCart.ts           # Cart state management
│   └── useOrderTracking.ts  # Poll order status
└── App.tsx
```

---

## User Flow

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  Menu Page  │ ──► │  Cart Page  │ ──► │  Checkout   │ ──► │   Track     │
│             │     │             │     │    Page     │     │   Order     │
│ GET /menu   │     │ (Local)     │     │ POST /order │     │ GET /track  │
└─────────────┘     └─────────────┘     └─────────────┘     └─────────────┘
                                              │
                                              ▼
                                        ┌─────────────┐
                                        │  Razorpay   │
                                        │  (if ONLINE)│
                                        └─────────────┘
```

---

## Environment Variables

```env
# .env
VITE_API_URL=http://localhost:8080/api
VITE_RAZORPAY_KEY_ID=rzp_test_xxxxxxxxxxxxx
```

---

## API Summary

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/menu` | Get all available menu items |
| `POST` | `/api/orders` | Place a new order |
| `GET` | `/api/orders/track/{orderCode}` | Track order by code |
