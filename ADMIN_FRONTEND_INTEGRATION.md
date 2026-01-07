# CafeBrew Admin Frontend Integration Guide

## Overview

This document covers all API endpoints needed for the **Admin Panel UI**. The landing page is the **Login Page** - all other pages require JWT authentication.

## Base URL

```
https://cafebrew-d6cp.onrender.com/api
```

---

## Authentication Flow

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  Login Page │ ──► │ Store Token │ ──► │  Dashboard  │
│             │     │ (localStorage)    │             │
│ POST /login │     │             │     │ (Protected) │
└─────────────┘     └─────────────┘     └─────────────┘
```

**All protected endpoints require:**
```
Authorization: Bearer <jwt_token>
```

---

## API Endpoints

### 1. Admin Login

Authenticate admin user and receive JWT token.

| Property | Value |
|----------|-------|
| **Method** | `POST` |
| **Endpoint** | `/api/admin/login` |
| **Auth Required** | No |
| **Content-Type** | `application/json` |

**Request Body:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Validation Rules:**
| Field | Rule |
|-------|------|
| `username` | Required, max 50 characters |
| `password` | Required, 6-100 characters |

**Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJPV05FUiIsImlhdCI6MTcwNDYyMDAwMCwiZXhwIjoxNzA0NzA2NDAwfQ.xxxxx",
  "role": "OWNER"
}
```

**JWT Token Payload:**
```json
{
  "sub": "admin",
  "role": "OWNER",
  "iat": 1704620000,
  "exp": 1704706400
}
```

**Usage Example:**
```typescript
const login = async (username: string, password: string): Promise<LoginResponse> => {
  const response = await fetch('/api/admin/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  });
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Login failed');
  }
  
  const data = await response.json();
  
  // Store token and role
  localStorage.setItem('adminToken', data.token);
  localStorage.setItem('adminRole', data.role);
  
  return data;
};
```

---

### 2. Get Orders by Status

Fetch orders filtered by status (for kitchen display/order queue).

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **Endpoint** | `/api/admin/orders?status={status}` |
| **Auth Required** | Yes (Bearer Token) |
| **Content-Type** | - |

**Query Parameters:**
| Parameter | Type | Required | Values |
|-----------|------|----------|--------|
| `status` | string | Yes | `NEW`, `PREPARING`, `READY`, `COMPLETED`, `CANCELLED` |

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Request:** No body required

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
  },
  {
    "orderId": 2,
    "orderCode": "ORD-E5F6G7H8",
    "customerName": "Jane Smith",
    "status": "NEW",
    "totalAmount": 280.00,
    "createdAt": "2025-01-07T10:35:00"
  }
]
```

**Usage Example:**
```typescript
const getOrdersByStatus = async (status: OrderStatus): Promise<AdminOrder[]> => {
  const token = localStorage.getItem('adminToken');
  
  const response = await fetch(`/api/admin/orders?status=${status}`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  
  if (response.status === 401) {
    // Token expired - redirect to login
    localStorage.removeItem('adminToken');
    window.location.href = '/login';
    throw new Error('Session expired');
  }
  
  if (!response.ok) throw new Error('Failed to fetch orders');
  return response.json();
};
```

---

### 3. Update Order Status

Update order status (follows state machine rules).

| Property | Value |
|----------|-------|
| **Method** | `PUT` |
| **Endpoint** | `/api/admin/orders/{orderId}/status` |
| **Auth Required** | Yes (Bearer Token) |
| **Content-Type** | `application/json` |

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `orderId` | number | Order ID |

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

**Valid Status Values:**
| Value | Description |
|-------|-------------|
| `NEW` | Order just placed |
| `PREPARING` | Kitchen is preparing |
| `READY` | Ready for pickup |
| `COMPLETED` | Order delivered/picked up |
| `CANCELLED` | Order cancelled |

**State Machine Rules:**
```
NEW → PREPARING → READY → COMPLETED
         ↓
      CANCELLED (from any state)
```

**Response:** `200 OK` (empty body)

**Usage Example:**
```typescript
const updateOrderStatus = async (orderId: number, status: OrderStatus): Promise<void> => {
  const token = localStorage.getItem('adminToken');
  
  const response = await fetch(`/api/admin/orders/${orderId}/status`, {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ status })
  });
  
  if (response.status === 401) {
    localStorage.removeItem('adminToken');
    window.location.href = '/login';
    throw new Error('Session expired');
  }
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Failed to update status');
  }
};
```

---

### 4. Complete Order (Shortcut)

Quick endpoint to mark order as completed.

| Property | Value |
|----------|-------|
| **Method** | `PUT` |
| **Endpoint** | `/api/admin/orders/{orderId}/complete` |
| **Auth Required** | Yes (Bearer Token) |
| **Content-Type** | - |

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `orderId` | number | Order ID |

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Request:** No body required

**Response:** `200 OK` (empty body)

**Usage Example:**
```typescript
const completeOrder = async (orderId: number): Promise<void> => {
  const token = localStorage.getItem('adminToken');
  
  const response = await fetch(`/api/admin/orders/${orderId}/complete`, {
    method: 'PUT',
    headers: { 'Authorization': `Bearer ${token}` }
  });
  
  if (response.status === 401) {
    localStorage.removeItem('adminToken');
    window.location.href = '/login';
    throw new Error('Session expired');
  }
  
  if (!response.ok) throw new Error('Failed to complete order');
};
```

---

### 5. Get Menu (for Management)

Fetches all menu items including unavailable ones.

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **Endpoint** | `/api/menu` |
| **Auth Required** | No |
| **Content-Type** | - |

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
  },
  {
    "id": 2,
    "name": "Espresso",
    "description": "Strong black coffee",
    "category": "Coffee",
    "price": 100.00,
    "available": false,
    "createdAt": "2025-01-07T10:00:00",
    "updatedAt": "2025-01-07T12:00:00"
  }
]
```

---

### 6. Toggle Menu Item Availability

Enable/disable menu items.

| Property | Value |
|----------|-------|
| **Method** | `PUT` |
| **Endpoint** | `/api/admin/menu/{id}/availability` |
| **Auth Required** | Yes (Bearer Token) |
| **Content-Type** | `application/json` |

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | number | Menu item ID |

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

**Usage Example:**
```typescript
const toggleMenuAvailability = async (menuItemId: number, available: boolean): Promise<void> => {
  const token = localStorage.getItem('adminToken');
  
  const response = await fetch(`/api/admin/menu/${menuItemId}/availability`, {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ available })
  });
  
  if (response.status === 401) {
    localStorage.removeItem('adminToken');
    window.location.href = '/login';
    throw new Error('Session expired');
  }
  
  if (!response.ok) throw new Error('Failed to update availability');
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

// Login
interface LoginRequest {
  username: string;
  password: string;
}

interface LoginResponse {
  token: string;
  role: AdminRole;
}

// Admin Order
interface AdminOrder {
  orderId: number;
  orderCode: string;
  customerName: string;
  status: OrderStatus;
  totalAmount: number;
  createdAt: string;
}

// Update Status
interface UpdateStatusRequest {
  status: OrderStatus;
}

// Menu Item
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

// Toggle Availability
interface ToggleAvailabilityRequest {
  available: boolean;
}

// Error Response
interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
}

// Auth Context
interface AuthState {
  token: string | null;
  role: AdminRole | null;
  isAuthenticated: boolean;
}
```

---

## Error Handling

**Error Response Format:**
```json
{
  "timestamp": "2025-01-07T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid credentials"
}
```

**HTTP Status Codes:**
| Code | Meaning | Action |
|------|---------|--------|
| `200` | Success | - |
| `400` | Validation error | Show error message |
| `401` | Unauthorized | Redirect to login |
| `403` | Forbidden | Show access denied |
| `404` | Not found | Show not found |
| `500` | Server error | Show generic error |

---

## Auth Service Module

```typescript
// services/authService.ts

const TOKEN_KEY = 'adminToken';
const ROLE_KEY = 'adminRole';

export const authService = {
  login: async (username: string, password: string): Promise<LoginResponse> => {
    const response = await fetch('/api/admin/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });
    
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Invalid credentials');
    }
    
    const data = await response.json();
    localStorage.setItem(TOKEN_KEY, data.token);
    localStorage.setItem(ROLE_KEY, data.role);
    return data;
  },

  logout: () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(ROLE_KEY);
    window.location.href = '/login';
  },

  getToken: (): string | null => {
    return localStorage.getItem(TOKEN_KEY);
  },

  getRole: (): AdminRole | null => {
    return localStorage.getItem(ROLE_KEY) as AdminRole | null;
  },

  isAuthenticated: (): boolean => {
    const token = localStorage.getItem(TOKEN_KEY);
    if (!token) return false;
    
    // Check if token is expired
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  }
};
```

---

## API Service Module

```typescript
// api/adminApi.ts

const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const getAuthHeaders = (): HeadersInit => {
  const token = localStorage.getItem('adminToken');
  return {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  };
};

const handleAuthError = (response: Response) => {
  if (response.status === 401) {
    localStorage.removeItem('adminToken');
    localStorage.removeItem('adminRole');
    window.location.href = '/login';
    throw new Error('Session expired');
  }
};

export const adminApi = {
  // Login
  login: async (username: string, password: string): Promise<LoginResponse> => {
    const response = await fetch(`${BASE_URL}/admin/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Login failed');
    }
    return response.json();
  },

  // Get orders by status
  getOrdersByStatus: async (status: OrderStatus): Promise<AdminOrder[]> => {
    const response = await fetch(`${BASE_URL}/admin/orders?status=${status}`, {
      headers: getAuthHeaders()
    });
    handleAuthError(response);
    if (!response.ok) throw new Error('Failed to fetch orders');
    return response.json();
  },

  // Update order status
  updateOrderStatus: async (orderId: number, status: OrderStatus): Promise<void> => {
    const response = await fetch(`${BASE_URL}/admin/orders/${orderId}/status`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify({ status })
    });
    handleAuthError(response);
    if (!response.ok) throw new Error('Failed to update status');
  },

  // Complete order
  completeOrder: async (orderId: number): Promise<void> => {
    const response = await fetch(`${BASE_URL}/admin/orders/${orderId}/complete`, {
      method: 'PUT',
      headers: getAuthHeaders()
    });
    handleAuthError(response);
    if (!response.ok) throw new Error('Failed to complete order');
  },

  // Get menu items
  getMenu: async (): Promise<MenuItem[]> => {
    const response = await fetch(`${BASE_URL}/menu`);
    if (!response.ok) throw new Error('Failed to fetch menu');
    return response.json();
  },

  // Toggle menu availability
  toggleMenuAvailability: async (menuItemId: number, available: boolean): Promise<void> => {
    const response = await fetch(`${BASE_URL}/admin/menu/${menuItemId}/availability`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify({ available })
    });
    handleAuthError(response);
    if (!response.ok) throw new Error('Failed to update availability');
  }
};
```

---

## Protected Route Component

```typescript
// components/ProtectedRoute.tsx
import { Navigate } from 'react-router-dom';
import { authService } from '../services/authService';

interface ProtectedRouteProps {
  children: React.ReactNode;
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
  if (!authService.isAuthenticated()) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
};
```

---

## Suggested Page Structure

```
src/
├── api/
│   └── adminApi.ts          # API calls
├── services/
│   └── authService.ts       # Auth management
├── types/
│   └── index.ts             # TypeScript interfaces
├── pages/
│   ├── LoginPage.tsx        # Landing page - Admin login
│   ├── DashboardPage.tsx    # Order overview
│   ├── KitchenPage.tsx      # Kitchen display (NEW/PREPARING orders)
│   ├── ReadyQueuePage.tsx   # Ready orders for pickup
│   └── MenuManagePage.tsx   # Toggle menu availability
├── components/
│   ├── ProtectedRoute.tsx   # Auth guard
│   ├── OrderCard.tsx        # Order display with actions
│   ├── StatusBadge.tsx      # Order status indicator
│   ├── MenuItemRow.tsx      # Menu item with toggle
│   └── Navbar.tsx           # Navigation with logout
├── hooks/
│   ├── useAuth.ts           # Auth state hook
│   └── useOrders.ts         # Orders polling hook
└── App.tsx
```

---

## App Routing

```typescript
// App.tsx
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ProtectedRoute } from './components/ProtectedRoute';
import { LoginPage } from './pages/LoginPage';
import { DashboardPage } from './pages/DashboardPage';
import { KitchenPage } from './pages/KitchenPage';
import { MenuManagePage } from './pages/MenuManagePage';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public - Login is landing page */}
        <Route path="/login" element={<LoginPage />} />
        
        {/* Protected Routes */}
        <Route path="/dashboard" element={
          <ProtectedRoute><DashboardPage /></ProtectedRoute>
        } />
        <Route path="/kitchen" element={
          <ProtectedRoute><KitchenPage /></ProtectedRoute>
        } />
        <Route path="/menu" element={
          <ProtectedRoute><MenuManagePage /></ProtectedRoute>
        } />
        
        {/* Redirect root to login */}
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
```

---

## User Flow

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  Login Page │ ──► │  Dashboard  │ ──► │   Kitchen   │
│  (Landing)  │     │             │     │   Display   │
│             │     │ All Orders  │     │ NEW/PREP    │
└─────────────┘     └─────────────┘     └─────────────┘
                           │
                           ▼
                    ┌─────────────┐
                    │    Menu     │
                    │  Management │
                    │ Toggle Avail│
                    └─────────────┘
```

---

## Environment Variables

```env
# .env
VITE_API_URL=http://localhost:8080/api
```

---

## API Summary

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/admin/login` | No | Admin login |
| `GET` | `/api/admin/orders?status={status}` | Yes | Get orders by status |
| `PUT` | `/api/admin/orders/{orderId}/status` | Yes | Update order status |
| `PUT` | `/api/admin/orders/{orderId}/complete` | Yes | Complete order |
| `GET` | `/api/menu` | No | Get all menu items |
| `PUT` | `/api/admin/menu/{id}/availability` | Yes | Toggle menu availability |

---

## Security Notes

1. **Token Storage:** JWT stored in localStorage. For higher security, consider httpOnly cookies.
2. **Token Expiration:** Default 24 hours. Handle 401 responses by redirecting to login.
3. **Role-Based Access:** Check `role` from login response for UI permissions.
4. **HTTPS:** Always use HTTPS in production.
