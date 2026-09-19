import { api } from "./api";

export interface OrderItemDto {
  id: number;
  menuItemId: number;
  name: string;
  price: number;
  quantity: number;
}

export interface OrderDto {
  id: number;
  billNo: string;
  customerName: string | null;
  tableNo: string | null;
  discount: number | null;
  serviceCharge: number | null;
  paymentMethod: string | null;
  gstRate: number;
  subtotal: number;
  totalAmount: number;
  status: string;
  createdAt: string;
  items: OrderItemDto[];
}

export interface PaginatedOrderHistoryDto {
  content: OrderDto[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export const orderService = {
  // Get active order or create new one
  getActiveOrder: async (): Promise<OrderDto> => {
    const response = await api.get("/api/orders/active");
    return response.data;
  },

  // Get settled orders history
  getHistory: async (): Promise<OrderDto[]> => {
    const response = await api.get("/api/orders/history");
    return response.data;
  },

  // Get paginated orders history
  getHistoryPaginated: async (params: { page?: number; size?: number; search?: string; paymentMethod?: string; fromDate?: string; toDate?: string }): Promise<PaginatedOrderHistoryDto> => {
    const query = new URLSearchParams();
    if (params.page !== undefined) query.set("page", params.page.toString());
    if (params.size !== undefined) query.set("size", params.size.toString());
    if (params.search) query.set("search", params.search);
    if (params.paymentMethod && params.paymentMethod !== "all") query.set("paymentMethod", params.paymentMethod);
    if (params.fromDate) query.set("fromDate", params.fromDate);
    if (params.toDate) query.set("toDate", params.toDate);
    const response = await api.get(`/api/orders/history/paginated?${query.toString()}`);
    return response.data;
  },

  // Cancel active order
  cancelActiveOrder: async (): Promise<void> => {
    await api.delete("/api/orders/active");
  },

  // Reopen/Edit a settled order
  reopenOrder: async (orderId: number): Promise<OrderDto> => {
    const response = await api.post(`/api/orders/${orderId}/reopen`);
    return response.data;
  },

  // Add item to order
  addItem: async (orderId: number, item: { menuItemId: number, name: string, price: number, quantity: number }): Promise<OrderDto> => {
    const response = await api.post(`/api/orders/${orderId}/items`, item);
    return response.data;
  },

  // Update item quantity
  updateItemQuantity: async (orderId: number, itemId: number, delta: number): Promise<OrderDto> => {
    const response = await api.put(`/api/orders/${orderId}/items/${itemId}?delta=${delta}`);
    return response.data;
  },

  // Remove item
  removeItem: async (orderId: number, itemId: number): Promise<OrderDto> => {
    const response = await api.delete(`/api/orders/${orderId}/items/${itemId}`);
    return response.data;
  },

  // Update GST
  updateGstRate: async (orderId: number, rate: number): Promise<OrderDto> => {
    const response = await api.put(`/api/orders/${orderId}/gst?rate=${rate}`);
    return response.data;
  },

  // Settle bill
  settleOrder: async (orderId: number, details: any): Promise<OrderDto> => {
    const response = await api.post(`/api/orders/${orderId}/settle`, details);
    return response.data;
  },

  // Checkout Direct (for high-speed POS checkouts)
  checkoutDirect: async (payload: any): Promise<OrderDto> => {
    const response = await api.post("/api/orders/checkout-direct", payload);
    return response.data;
  }
};
