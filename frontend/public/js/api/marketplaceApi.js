/**
 * Servicios de API REST orientados a cada actor del dominio NexusMarket.
 * Todos los contratos coinciden exactamente con los controladores y DTOs del backend.
 */

import { apiRequest } from './httpClient.js';

export const authApi = {
  login(email, password, customOptions = {}) {
    return apiRequest('/auth/login', {
      method: 'POST',
      body: { email, password },
      ...customOptions
    });
  },

  logout(customOptions = {}) {
    return apiRequest('/auth/logout', {
      method: 'POST',
      ...customOptions
    });
  },

  registerBuyer(buyerPayload, customOptions = {}) {
    return apiRequest('/auth/register', {
      method: 'POST',
      body: buyerPayload,
      ...customOptions
    });
  },

  getCatalog(params = {}, customOptions = {}) {
    return apiRequest('/auth/catalog', {
      method: 'GET',
      params: {
        page: params.page ?? 0,
        size: params.size ?? 6,
        sortBy: params.sortBy,
        direction: params.direction,
        search: params.search,
        type: params.type,
        minPrice: params.minPrice,
        maxPrice: params.maxPrice
      },
      ...customOptions
    });
  },

  getProductById(productId, customOptions = {}) {
    return apiRequest(`/auth/catalog/${productId}`, {
      method: 'GET',
      ...customOptions
    });
  },

  getProductStock(productId, customOptions = {}) {
    return apiRequest(`/auth/catalog/${productId}/stock`, {
      method: 'GET',
      ...customOptions
    });
  }
};

export const buyerApi = {
  getProfile(buyerId) {
    return apiRequest(`/buyers/${buyerId}`);
  },

  addAddress(buyerId, addressDto) {
    return apiRequest(`/buyers/${buyerId}/addresses`, {
      method: 'POST',
      params: { authenticatedBuyerId: buyerId },
      body: addressDto
    });
  },

  updateAddress(buyerId, index, addressDto) {
    return apiRequest(`/buyers/${buyerId}/addresses/${index}`, {
      method: 'PUT',
      params: { authenticatedBuyerId: buyerId },
      body: addressDto
    });
  },

  removeAddress(buyerId, index) {
    return apiRequest(`/buyers/${buyerId}/addresses/${index}`, {
      method: 'DELETE',
      params: { authenticatedBuyerId: buyerId }
    });
  },

  updateAllAddresses(buyerId, primaryAddress, additionalAddresses) {
    return apiRequest(`/buyers/${buyerId}/addresses`, {
      method: 'PUT',
      params: { authenticatedBuyerId: buyerId },
      body: { primaryAddress, additionalAddresses }
    });
  },

  getOrCreateCart(buyerId) {
    return apiRequest(`/buyers/${buyerId}/cart`);
  },

  createCart(buyerId) {
    return apiRequest(`/buyers/${buyerId}/cart`, { method: 'POST' });
  },

  getCartById(cartId) {
    return apiRequest(`/buyers/carts/${cartId}`);
  },

  addCartItem(cartId, { productId, quantity, unitPrice, currency }) {
    return apiRequest(`/buyers/carts/${cartId}/items`, {
      method: 'POST',
      body: { productId, quantity, unitPrice, currency }
    });
  },

  updateCartItemQuantity(cartId, productId, quantity) {
    return apiRequest(`/buyers/carts/${cartId}/items/${productId}`, {
      method: 'PUT',
      body: { quantity }
    });
  },

  removeCartItem(cartId, productId) {
    return apiRequest(`/buyers/carts/${cartId}/items/${productId}`, {
      method: 'DELETE'
    });
  },

  clearCart(cartId) {
    return apiRequest(`/buyers/carts/${cartId}`, {
      method: 'DELETE'
    });
  },

  createOrder(cartId, deliveryAddress) {
    return apiRequest('/buyers/orders', {
      method: 'POST',
      body: { cartId, deliveryAddress }
    });
  },

  getOrderById(orderId) {
    return apiRequest(`/buyers/orders/${orderId}`);
  },

  getOrders(buyerId, { page = 0, size = 10, sortBy = 'orderDate', direction = 'DESC' } = {}) {
    return apiRequest(`/buyers/${buyerId}/orders`, {
      params: { page, size, sortBy, direction }
    });
  },

  updateOrderDeliveryAddress(orderId, addressDto) {
    return apiRequest(`/buyers/orders/${orderId}/delivery-address`, {
      method: 'PUT',
      body: addressDto
    });
  },

  payOrder(orderId, { amount, currency = 'COP', paymentMethod = 'CREDIT_CARD' }) {
    return apiRequest(`/buyers/orders/${orderId}/pay`, {
      method: 'POST',
      body: { orderId, amount, currency, paymentMethod }
    });
  },

  cancelOrder(orderId) {
    return apiRequest(`/buyers/orders/${orderId}/cancel`, {
      method: 'POST'
    });
  },

  requestReturn(orderId, reason) {
    return apiRequest(`/buyers/orders/${orderId}/return`, {
      method: 'POST',
      body: { reason }
    });
  },

  getReturns(buyerId, { page = 0, size = 10 } = {}) {
    return apiRequest(`/buyers/${buyerId}/returns`, {
      params: { page, size }
    });
  }
};

export const sellerApi = {
  getProfile(sellerId) {
    return apiRequest(`/sellers/${sellerId}`);
  },

  getProducts({ page = 0, size = 10, sortBy = 'name', direction = 'ASC' } = {}) {
    return apiRequest('/sellers/products', {
      params: { page, size, sortBy, direction }
    });
  },

  getProductById(productId) {
    return apiRequest(`/sellers/products/${productId}`);
  },

  createProduct(productPayload) {
    return apiRequest('/sellers/products', {
      method: 'POST',
      body: productPayload
    });
  },

  updateProductStatus(productId, status) {
    return apiRequest(`/sellers/products/${productId}/status`, {
      method: 'PATCH',
      body: { status }
    });
  },

  updateProductPrice(productId, price, currency = 'COP') {
    return apiRequest(`/sellers/products/${productId}/price`, {
      method: 'PATCH',
      body: { price, currency }
    });
  },

  getOrders(sellerId, { page = 0, size = 10, sortBy = 'orderDate', direction = 'DESC' } = {}) {
    return apiRequest(`/sellers/${sellerId}/orders`, {
      params: { page, size, sortBy, direction }
    });
  },

  getWarehouses(sellerId, { page = 0, size = 20 } = {}) {
    return apiRequest(`/sellers/${sellerId}/warehouses`, {
      params: { page, size }
    });
  },

  getInventory(sellerId, { page = 0, size = 20 } = {}) {
    return apiRequest(`/sellers/${sellerId}/inventory`, {
      params: { page, size }
    });
  },

  registerInitialStock(sellerId, { productId, warehouseId, quantity }) {
    return apiRequest(`/sellers/${sellerId}/inventory/initial`, {
      method: 'POST',
      body: { productId, warehouseId, quantity }
    });
  },

  adjustStock(sellerId, inventoryId, { quantity, reason }) {
    return apiRequest(`/sellers/${sellerId}/inventory/${inventoryId}/adjust`, {
      method: 'PATCH',
      body: { quantity, reason }
    });
  }
};

export const logisticsApi = {
  listShipments() {
    return apiRequest('/logistics/shipments');
  },

  dispatchOrder(orderId, logisticsOperatorId) {
    return apiRequest('/logistics/shipments/dispatch', {
      method: 'POST',
      body: { orderId, logisticsOperatorId }
    });
  },

  confirmDelivery(shipmentId) {
    return apiRequest(`/logistics/shipments/${shipmentId}/deliver`, {
      method: 'PATCH'
    });
  },

  registerInventoryMovement(inventoryId, { quantity, type, reference }) {
    return apiRequest(`/logistics/inventory/${inventoryId}/movement`, {
      method: 'POST',
      body: { quantity, type, reference }
    });
  },

  getInventoryMovements(inventoryId) {
    return apiRequest(`/logistics/inventory/${inventoryId}/movements`);
  }
};

export const supervisorApi = {
  getConsolidatedReport() {
    return apiRequest('/supervisors/reports/consolidated');
  },

  getOrders() {
    return apiRequest('/supervisors/orders');
  },

  getWarehouses() {
    return apiRequest('/supervisors/warehouses');
  },

  getInventories() {
    return apiRequest('/supervisors/inventories');
  },

  getShipments() {
    return apiRequest('/supervisors/shipments');
  },

  getReturns() {
    return apiRequest('/supervisors/returns');
  },

  getRefunds() {
    return apiRequest('/supervisors/refunds');
  },

  approveReturn(returnId, notes = 'Aprobado en revisión de supervisión') {
    return apiRequest(`/supervisors/returns/${returnId}/approve`, {
      method: 'POST',
      body: { notes }
    });
  },

  rejectReturn(returnId, notes = 'Rechazado en revisión de supervisión') {
    return apiRequest(`/supervisors/returns/${returnId}/reject`, {
      method: 'POST',
      body: { notes }
    });
  },

  processRefund(returnId, amount, reason = 'Reembolso autorizado por supervisor') {
    return apiRequest(`/supervisors/returns/${returnId}/refund`, {
      method: 'POST',
      body: { amount, reason }
    });
  },

  reviewDispute(disputeId, decision, notes) {
    return apiRequest(`/supervisors/disputes/${disputeId}/review`, {
      method: 'POST',
      params: { decision },
      body: { notes }
    });
  },

  getAuditOperations() {
    return apiRequest('/supervisors/audit/operations');
  }
};

export const adminApi = {
  getConsolidatedReport() {
    return apiRequest('/admin/reports/consolidated');
  },

  getUsers({ page = 0, size = 20 } = {}) {
    return apiRequest('/admin/users', { params: { page, size } });
  },

  registerStaff(staffPayload) {
    return apiRequest('/admin/users/staff', {
      method: 'POST',
      body: staffPayload
    });
  },

  registerSeller(sellerPayload, adminUserId) {
    return apiRequest('/admin/users/seller', {
      method: 'POST',
      params: adminUserId ? { adminUserId } : undefined,
      body: sellerPayload
    });
  },

  updateUserStatus(userId, status) {
    return apiRequest(`/admin/users/${userId}/status`, {
      method: 'PATCH',
      body: { status }
    });
  },

  updateBuyerCommercialStatus(buyerId, commercialStatus) {
    return apiRequest(`/admin/users/buyer/${buyerId}/commercial-status`, {
      method: 'PATCH',
      body: { commercialStatus }
    });
  },

  getWarehouses({ page = 0, size = 20, type, sellerId } = {}) {
    return apiRequest('/admin/warehouses', {
      params: { page, size, type, sellerId }
    });
  },

  createWarehouse(warehousePayload) {
    return apiRequest('/admin/warehouses', {
      method: 'POST',
      body: warehousePayload
    });
  },

  updateWarehouseCapacity(warehouseId, capacity) {
    return apiRequest(`/admin/warehouses/${warehouseId}/capacity`, {
      method: 'PATCH',
      body: { capacity }
    });
  },

  getOrders({ page = 0, size = 20, sortBy = 'orderDate', direction = 'DESC' } = {}) {
    return apiRequest('/admin/orders', {
      params: { page, size, sortBy, direction }
    });
  },

  getProducts({ page = 0, size = 20, sortBy = 'name', direction = 'ASC' } = {}) {
    return apiRequest('/admin/products', {
      params: { page, size, sortBy, direction }
    });
  },

  getReturns({ page = 0, size = 20 } = {}) {
    return apiRequest('/admin/returns', { params: { page, size } });
  },

  approveReturn(requestId) {
    return apiRequest(`/admin/returns/${requestId}/approve`, { method: 'PATCH' });
  },

  rejectReturn(requestId) {
    return apiRequest(`/admin/returns/${requestId}/reject`, { method: 'PATCH' });
  },

  getRefunds({ page = 0, size = 20 } = {}) {
    return apiRequest('/admin/refunds', { params: { page, size } });
  },

  processRefund(returnRequestId, amount, currencyCode = 'COP') {
    return apiRequest('/admin/refunds', {
      method: 'POST',
      body: { returnRequestId, amount, currencyCode }
    });
  },

  getInvoices({ page = 0, size = 20 } = {}) {
    return apiRequest('/admin/invoices', { params: { page, size } });
  },

  generateInvoice(orderId, taxAmount, currencyCode = 'COP') {
    return apiRequest('/admin/invoices', {
      method: 'POST',
      body: { orderId, taxAmount, currencyCode }
    });
  },

  getOperations({ page = 0, size = 20 } = {}) {
    return apiRequest('/admin/operations', { params: { page, size } });
  },

  recordOperation(operationPayload) {
    return apiRequest('/admin/operations', {
      method: 'POST',
      body: operationPayload
    });
  }
};
