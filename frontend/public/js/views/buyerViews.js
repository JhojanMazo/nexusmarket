/**
 * Vistas privadas del Comprador (BUYER):
 * - Carrito de Compras (#/cart)
 * - Checkout (#/checkout)
 * - Historial y Detalle de Pedidos + Devoluciones (#/orders)
 * - Perfil y Gestión de Direcciones (#/profile)
 */

import { buyerApi } from '../api/marketplaceApi.js';
import { sessionStore } from '../state/sessionStore.js';
import { formatMoney } from '../config/env.js';
import {
  escapeHtml,
  showToast,
  confirmDialog,
  renderLoadingState,
  renderErrorState,
  renderEmptyState,
  renderOrderStatusBadge,
  renderPagination
} from '../components/uiComponents.js';

export async function renderCartView(container) {
  const session = sessionStore.getSession();
  container.innerHTML = `
    <section class="page-header">
      <div>
        <h1>Mi Carrito de Compras</h1>
        <p class="text-muted">Revise sus productos seleccionados, ajuste cantidades o continúe al pago seguro.</p>
      </div>
      <a href="#/catalog" class="btn btn-outline">&larr; Seguir Comprando</a>
    </section>
    <div id="cart-content-box">
      ${renderLoadingState('Recuperando su carrito activo...')}
    </div>
  `;

  const contentBox = container.querySelector('#cart-content-box');

  async function loadCart() {
    try {
      const cart = await buyerApi.getOrCreateCart(session.userId);
      sessionStore.setActiveCartId(cart.cartId);
      const items = cart.items || [];
      const totalCount = items.reduce((acc, item) => acc + Number(item.quantity || 0), 0);
      sessionStore.setCartCount(totalCount);

      if (items.length === 0) {
        contentBox.innerHTML = renderEmptyState(
          'Su carrito está vacío',
          'Explore el catálogo de NexusMarket y agregue productos físicos o licencias digitales.',
          `<a href="#/catalog" class="btn btn-primary">Explorar Catálogo</a>`
        );
        return;
      }

      contentBox.innerHTML = `
        <div class="cart-layout">
          <div class="cart-items-panel">
            <table class="data-table" aria-label="Productos en el carrito">
              <thead>
                <tr>
                  <th>Producto</th>
                  <th>Precio Unitario</th>
                  <th>Cantidad</th>
                  <th>Subtotal</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                ${items
                  .map(
                    (item) => `
                  <tr data-product-id="${escapeHtml(item.productId)}">
                    <td>
                      <a href="#/product/${escapeHtml(item.productId)}" class="table-product-link">
                        <strong>${escapeHtml(item.productName)}</strong>
                      </a>
                    </td>
                    <td>${escapeHtml(formatMoney(item.unitPrice, item.currency))}</td>
                    <td>
                      <div class="qty-stepper">
                        <button type="button" class="btn btn-secondary btn-xs js-qty-change"
                          data-product-id="${escapeHtml(item.productId)}"
                          data-qty="${Number(item.quantity) - 1}"
                          aria-label="Disminuir cantidad de ${escapeHtml(item.productName)}">&minus;</button>
                        <span class="qty-value">${escapeHtml(item.quantity)}</span>
                        <button type="button" class="btn btn-secondary btn-xs js-qty-change"
                          data-product-id="${escapeHtml(item.productId)}"
                          data-qty="${Number(item.quantity) + 1}"
                          aria-label="Aumentar cantidad de ${escapeHtml(item.productName)}">+</button>
                      </div>
                    </td>
                    <td><strong>${escapeHtml(formatMoney(item.subtotal, item.currency))}</strong></td>
                    <td>
                      <button type="button" class="btn btn-danger btn-xs js-remove-item"
                        data-product-id="${escapeHtml(item.productId)}"
                        data-product-name="${escapeHtml(item.productName)}">
                        Eliminar
                      </button>
                    </td>
                  </tr>
                `
                  )
                  .join('')}
              </tbody>
            </table>
            <div class="cart-footer-actions">
              <button type="button" id="clear-cart-btn" class="btn btn-outline-danger btn-sm">
                Vaciar Carrito
              </button>
            </div>
          </div>

          <aside class="cart-summary-card" aria-label="Resumen del carrito">
            <h2>Resumen de Compra</h2>
            <div class="summary-row">
              <span>Unidades totales</span>
              <span>${totalCount}</span>
            </div>
            <div class="summary-row summary-total">
              <span>Subtotal Estimado</span>
              <strong>${escapeHtml(formatMoney(cart.subtotal, cart.currency))}</strong>
            </div>
            <p class="text-small text-muted">
              Los precios y la disponibilidad de inventario son validados transaccionalmente en el backend al confirmar el pedido.
            </p>
            <a href="#/checkout" class="btn btn-primary btn-block btn-lg">
              Continuar al Checkout &rarr;
            </a>
          </aside>
        </div>
      `;

      contentBox.querySelectorAll('.js-qty-change').forEach((btn) => {
        btn.addEventListener('click', async () => {
          const productId = btn.dataset.productId;
          const nextQty = Number(btn.dataset.qty);
          try {
            await buyerApi.updateCartItemQuantity(cart.cartId, productId, nextQty);
            await loadCart();
          } catch (err) {
            showToast(err.message || 'No se pudo actualizar la cantidad.', 'error');
          }
        });
      });

      contentBox.querySelectorAll('.js-remove-item').forEach((btn) => {
        btn.addEventListener('click', async () => {
          const ok = await confirmDialog({
            title: 'Eliminar producto del carrito',
            message: `¿Desea eliminar "${btn.dataset.productName}" de su carrito?`,
            confirmText: 'Sí, eliminar',
            danger: true
          });
          if (!ok) return;
          try {
            await buyerApi.removeCartItem(cart.cartId, btn.dataset.productId);
            showToast('Producto eliminado del carrito.', 'info');
            await loadCart();
          } catch (err) {
            showToast(err.message, 'error');
          }
        });
      });

      contentBox.querySelector('#clear-cart-btn')?.addEventListener('click', async () => {
        const ok = await confirmDialog({
          title: 'Vaciar carrito de compras',
          message: '¿Está seguro de eliminar todos los productos del carrito?',
          confirmText: 'Vaciar carrito',
          danger: true
        });
        if (!ok) return;
        try {
          await buyerApi.clearCart(cart.cartId);
          sessionStore.setActiveCartId(null);
          sessionStore.setCartCount(0);
          showToast('El carrito ha sido vaciado.', 'info');
          await loadCart();
        } catch (err) {
          showToast(err.message, 'error');
        }
      });
    } catch (err) {
      contentBox.innerHTML = renderErrorState(err.message, 'cart-retry-btn');
      contentBox.querySelector('#cart-retry-btn')?.addEventListener('click', loadCart);
    }
  }

  await loadCart();
}

export async function renderCheckoutView(container) {
  const session = sessionStore.getSession();
  container.innerHTML = renderLoadingState('Preparando checkout y verificando direcciones...');

  try {
    const [cart, profile] = await Promise.all([
      buyerApi.getOrCreateCart(session.userId),
      buyerApi.getProfile(session.userId)
    ]);

    const items = cart.items || [];
    if (items.length === 0) {
      container.innerHTML = renderEmptyState(
        'No hay productos para procesar en Checkout',
        'Agregue al menos un producto publicado a su carrito antes de confirmar un pedido.',
        `<a href="#/catalog" class="btn btn-primary">Ir al Catálogo</a>`
      );
      return;
    }

    const savedAddresses = [];
    if (profile.primaryAddress) {
      savedAddresses.push({ label: 'Dirección Principal', address: profile.primaryAddress });
    }
    (profile.additionalAddresses || []).forEach((addr, idx) => {
      savedAddresses.push({ label: `Dirección Secundaria #${idx + 1}`, address: addr });
    });

    const defaultAddr = profile.primaryAddress || {
      street: '',
      city: '',
      stateOrProvince: '',
      country: 'Colombia',
      postalCode: '',
      reference: ''
    };

    container.innerHTML = `
      <section class="page-header">
        <div>
          <h1>Confirmación de Pedido y Pago (Checkout)</h1>
          <p class="text-muted">Verifique sus productos, dirección de entrega y método de pago.</p>
        </div>
        <a href="#/cart" class="btn btn-outline">&larr; Volver al Carrito</a>
      </section>

      <div class="checkout-layout">
        <form id="checkout-form" class="checkout-form-panel">
          <div class="card-panel">
            <h2>1. Dirección de Entrega</h2>
            ${
              savedAddresses.length > 0
                ? `
              <div class="form-group">
                <label for="checkout-saved-address">Seleccionar dirección guardada</label>
                <select id="checkout-saved-address" class="form-control">
                  ${savedAddresses
                    .map(
                      (entry, idx) => `
                    <option value="${idx}">
                      ${escapeHtml(entry.label)}: ${escapeHtml(entry.address.street)}, ${escapeHtml(entry.address.city)}
                    </option>
                  `
                    )
                    .join('')}
                  <option value="custom">Ingresar otra dirección de entrega...</option>
                </select>
              </div>
            `
                : ''
            }
            <div class="form-grid-2">
              <div class="form-group">
                <label for="chk-street">Dirección / Calle *</label>
                <input type="text" id="chk-street" name="street" class="form-control"
                  value="${escapeHtml(defaultAddr.street)}" required />
              </div>
              <div class="form-group">
                <label for="chk-city">Ciudad *</label>
                <input type="text" id="chk-city" name="city" class="form-control"
                  value="${escapeHtml(defaultAddr.city)}" required />
              </div>
              <div class="form-group">
                <label for="chk-state">Departamento / Estado *</label>
                <input type="text" id="chk-state" name="stateOrProvince" class="form-control"
                  value="${escapeHtml(defaultAddr.stateOrProvince)}" required />
              </div>
              <div class="form-group">
                <label for="chk-country">País *</label>
                <input type="text" id="chk-country" name="country" class="form-control"
                  value="${escapeHtml(defaultAddr.country || 'Colombia')}" required />
              </div>
              <div class="form-group">
                <label for="chk-postal">Código Postal *</label>
                <input type="text" id="chk-postal" name="postalCode" class="form-control"
                  value="${escapeHtml(defaultAddr.postalCode)}" required />
              </div>
              <div class="form-group">
                <label for="chk-ref">Referencia</label>
                <input type="text" id="chk-ref" name="reference" class="form-control"
                  value="${escapeHtml(defaultAddr.reference || '')}" />
              </div>
            </div>
          </div>

          <div class="card-panel">
            <h2>2. Método de Pago y Modalidad</h2>
            <div class="form-grid-2">
              <div class="form-group">
                <label for="chk-payment-method">Método de Pago Disponible *</label>
                <select id="chk-payment-method" name="paymentMethod" class="form-control" required>
                  <option value="CREDIT_CARD">Tarjeta de Crédito Corporativa / Personal</option>
                  <option value="DEBIT_CARD">Débito Bancario / PSE</option>
                  <option value="BANK_TRANSFER">Transferencia Electrónica Empresarial</option>
                </select>
              </div>
              <div class="form-group">
                <label for="chk-payment-timing">Momento del Pago</label>
                <select id="chk-payment-timing" name="payImmediately" class="form-control">
                  <option value="true" selected>Pagar ahora y reservar inventario inmediatamente</option>
                  <option value="false">Solo crear orden (queda pendiente de pago con TTL)</option>
                </select>
              </div>
            </div>
          </div>

          <div id="checkout-error-box" class="form-error hidden" role="alert"></div>

          <button type="submit" id="checkout-submit-btn" class="btn btn-primary btn-block btn-lg">
            Confirmar Pedido por ${escapeHtml(formatMoney(cart.subtotal, cart.currency))}
          </button>
        </form>

        <aside class="cart-summary-card">
          <h2>Resumen de Productos (${items.length})</h2>
          <ul class="checkout-item-list">
            ${items
              .map(
                (i) => `
              <li>
                <div>
                  <strong>${escapeHtml(i.productName)}</strong>
                  <span class="text-muted">x ${escapeHtml(i.quantity)}</span>
                </div>
                <span>${escapeHtml(formatMoney(i.subtotal, i.currency))}</span>
              </li>
            `
              )
              .join('')}
          </ul>
          <div class="summary-row summary-total">
            <span>Total a Confirmar</span>
            <strong>${escapeHtml(formatMoney(cart.subtotal, cart.currency))}</strong>
          </div>
        </aside>
      </div>
    `;

    const form = container.querySelector('#checkout-form');
    const addrSelect = container.querySelector('#checkout-saved-address');
    const submitBtn = container.querySelector('#checkout-submit-btn');
    const errorBox = container.querySelector('#checkout-error-box');

    addrSelect?.addEventListener('change', () => {
      const val = addrSelect.value;
      if (val === 'custom') {
        form.street.value = '';
        form.city.value = '';
        form.stateOrProvince.value = '';
        form.postalCode.value = '';
        form.reference.value = '';
      } else {
        const chosen = savedAddresses[Number(val)]?.address;
        if (chosen) {
          form.street.value = chosen.street || '';
          form.city.value = chosen.city || '';
          form.stateOrProvince.value = chosen.stateOrProvince || '';
          form.country.value = chosen.country || 'Colombia';
          form.postalCode.value = chosen.postalCode || '';
          form.reference.value = chosen.reference || '';
        }
      }
    });

    let isSubmitting = false;
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      if (isSubmitting) return;
      errorBox.classList.add('hidden');

      const deliveryAddress = {
        street: form.street.value.trim(),
        city: form.city.value.trim(),
        stateOrProvince: form.stateOrProvince.value.trim(),
        country: form.country.value.trim(),
        postalCode: form.postalCode.value.trim(),
        reference: form.reference.value.trim() || null
      };

      if (!deliveryAddress.street || !deliveryAddress.city || !deliveryAddress.stateOrProvince || !deliveryAddress.postalCode) {
        errorBox.textContent = 'Complete todos los campos obligatorios de la dirección de entrega.';
        errorBox.classList.remove('hidden');
        return;
      }

      isSubmitting = true;
      submitBtn.disabled = true;
      submitBtn.textContent = 'Procesando orden en NexusMarket...';

      try {
        const createdOrder = await buyerApi.createOrder(cart.cartId, deliveryAddress);
        sessionStore.setActiveCartId(null);
        sessionStore.setCartCount(0);

        let paymentResult = null;
        if (form.payImmediately.value === 'true') {
          paymentResult = await buyerApi.payOrder(createdOrder.orderId, {
            amount: createdOrder.total,
            currency: createdOrder.currency,
            paymentMethod: form.paymentMethod.value
          });
        }

        const finalOrder = await buyerApi.getOrderById(createdOrder.orderId);

        container.innerHTML = `
          <div class="order-confirmation-card" role="region" aria-label="Confirmación de pedido">
            <span class="badge badge-status-published">PEDIDO REGISTRADO EXITOSAMENTE</span>
            <h1>¡Gracias por su compra en NexusMarket!</h1>
            <p>Su pedido ha sido persistido en la base de datos relacional con todos sus ítems.</p>
            <dl class="confirmation-details">
              <div>
                <dt>ID del Pedido</dt>
                <dd><code>${escapeHtml(finalOrder.orderId)}</code></dd>
              </div>
              <div>
                <dt>Estado Actual</dt>
                <dd>${renderOrderStatusBadge(finalOrder.status)}</dd>
              </div>
              <div>
                <dt>Total del Pedido</dt>
                <dd><strong>${escapeHtml(formatMoney(finalOrder.total, finalOrder.currency))}</strong></dd>
              </div>
              ${
                paymentResult
                  ? `
                <div>
                  <dt>Referencia de Transacción</dt>
                  <dd><code>${escapeHtml(paymentResult.transactionReference)}</code> (${escapeHtml(paymentResult.paymentMethod)})</dd>
                </div>
              `
                  : ''
              }
              <div>
                <dt>Dirección de Entrega</dt>
                <dd>${escapeHtml(deliveryAddress.street)}, ${escapeHtml(deliveryAddress.city)} (${escapeHtml(deliveryAddress.country)})</dd>
              </div>
            </dl>
            <div class="confirmation-actions">
              <a href="#/orders" class="btn btn-primary">Ver Mis Pedidos y Estado</a>
              <a href="#/catalog" class="btn btn-secondary">Volver al Catálogo</a>
            </div>
          </div>
        `;
        showToast('Pedido confirmado correctamente.', 'success');
      } catch (err) {
        errorBox.textContent = err.message || 'No se pudo completar el pedido.';
        errorBox.classList.remove('hidden');
        isSubmitting = false;
        submitBtn.disabled = false;
        submitBtn.textContent = `Confirmar Pedido por ${formatMoney(cart.subtotal, cart.currency)}`;
      }
    });
  } catch (err) {
    container.innerHTML = renderErrorState(err.message);
  }
}

export async function renderBuyerOrdersView(container) {
  const session = sessionStore.getSession();
  let currentPage = 0;

  container.innerHTML = `
    <section class="page-header">
      <div>
        <h1>Mis Pedidos y Devoluciones</h1>
        <p class="text-muted">Consulte el estado de sus compras, pague órdenes pendientes, cancele pedidos no enviados o solicite devoluciones.</p>
      </div>
      <a href="#/catalog" class="btn btn-primary">Nueva Compra</a>
    </section>
    <div id="buyer-orders-box">
      ${renderLoadingState('Consultando su historial de pedidos...')}
    </div>
    <section class="section-block">
      <h2>Solicitudes de Devolución Registradas</h2>
      <div id="buyer-returns-box">
        ${renderLoadingState('Cargando devoluciones...')}
      </div>
    </section>
  `;

  const ordersBox = container.querySelector('#buyer-orders-box');
  const returnsBox = container.querySelector('#buyer-returns-box');

  async function loadOrdersAndReturns() {
    try {
      const [ordersPage, returnsPage] = await Promise.all([
        buyerApi.getOrders(session.userId, { page: currentPage, size: 8, sortBy: 'orderDate', direction: 'DESC' }),
        buyerApi.getReturns(session.userId, { page: 0, size: 10 })
      ]);

      const orders = ordersPage?.content || [];
      if (orders.length === 0) {
        ordersBox.innerHTML = renderEmptyState(
          'Aún no registra pedidos',
          'Sus órdenes confirmadas aparecerán aquí con su estado e historial completo.',
          `<a href="#/catalog" class="btn btn-primary">Explorar Productos</a>`
        );
      } else {
        ordersBox.innerHTML = `
          <div class="orders-list">
            ${orders
              .map((order) => {
                const canPay = order.status === 'CART' || order.status === 'PENDING_PAYMENT';
                const canCancel = order.status === 'CART' || order.status === 'PENDING_PAYMENT';
                const canReturn = order.status === 'DELIVERED_COMPLETED';
                const addr = order.deliveryAddress
                  ? `${order.deliveryAddress.street}, ${order.deliveryAddress.city}`
                  : 'Sin dirección';

                return `
                  <article class="order-card" data-order-id="${escapeHtml(order.orderId)}">
                    <header class="order-card-header">
                      <div>
                        <span class="text-small text-muted">Pedido #<code>${escapeHtml(order.orderId)}</code></span>
                        <h3>Fecha: ${escapeHtml(order.creationDate || 'Hoy')} &bull; Entrega: ${escapeHtml(addr)}</h3>
                      </div>
                      <div class="order-card-header-right">
                        ${renderOrderStatusBadge(order.status)}
                        <strong class="order-total-amount">${escapeHtml(formatMoney(order.total, order.currency))}</strong>
                      </div>
                    </header>

                    <div class="order-items-mini">
                      <table class="data-table data-table-compact">
                        <thead>
                          <tr>
                            <th>Producto</th>
                            <th>Cantidad</th>
                            <th>Precio Unitario</th>
                            <th>Subtotal</th>
                          </tr>
                        </thead>
                        <tbody>
                          ${(order.items || [])
                            .map(
                              (item) => `
                            <tr>
                              <td>${escapeHtml(item.productName)}</td>
                              <td>${escapeHtml(item.quantity)}</td>
                              <td>${escapeHtml(formatMoney(item.unitPrice, item.currency))}</td>
                              <td>${escapeHtml(formatMoney(item.subtotal, item.currency))}</td>
                            </tr>
                          `
                            )
                            .join('')}
                        </tbody>
                      </table>
                    </div>

                    <footer class="order-card-actions">
                      ${
                        canPay
                          ? `<button type="button" class="btn btn-primary btn-sm js-pay-order"
                              data-order-id="${escapeHtml(order.orderId)}"
                              data-amount="${escapeHtml(order.total)}"
                              data-currency="${escapeHtml(order.currency)}">
                              Pagar Ahora
                            </button>`
                          : ''
                      }
                      ${
                        canCancel
                          ? `<button type="button" class="btn btn-outline-danger btn-sm js-cancel-order"
                              data-order-id="${escapeHtml(order.orderId)}">
                              Cancelar Pedido
                            </button>`
                          : ''
                      }
                      ${
                        canReturn
                          ? `<button type="button" class="btn btn-secondary btn-sm js-return-order"
                              data-order-id="${escapeHtml(order.orderId)}">
                              Solicitar Devolución
                            </button>`
                          : ''
                      }
                    </footer>
                  </article>
                `;
              })
              .join('')}
          </div>
          ${renderPagination(ordersPage, 'buyer-orders')}
        `;

        ordersBox.querySelectorAll('.js-pay-order').forEach((btn) => {
          btn.addEventListener('click', async () => {
            btn.disabled = true;
            try {
              await buyerApi.payOrder(btn.dataset.orderId, {
                amount: Number(btn.dataset.amount),
                currency: btn.dataset.currency || 'COP',
                paymentMethod: 'CREDIT_CARD'
              });
              showToast('Pago procesado exitosamente y stock reservado.', 'success');
              await loadOrdersAndReturns();
            } catch (err) {
              showToast(err.message, 'error');
              btn.disabled = false;
            }
          });
        });

        ordersBox.querySelectorAll('.js-cancel-order').forEach((btn) => {
          btn.addEventListener('click', async () => {
            const ok = await confirmDialog({
              title: 'Cancelar pedido',
              message: '¿Confirma que desea cancelar este pedido? El stock reservado será liberado.',
              confirmText: 'Sí, cancelar pedido',
              danger: true
            });
            if (!ok) return;
            try {
              await buyerApi.cancelOrder(btn.dataset.orderId);
              showToast('Pedido cancelado correctamente.', 'info');
              await loadOrdersAndReturns();
            } catch (err) {
              showToast(err.message, 'error');
            }
          });
        });

        ordersBox.querySelectorAll('.js-return-order').forEach((btn) => {
          btn.addEventListener('click', async () => {
            const ok = await confirmDialog({
              title: 'Solicitar devolución de pedido entregado',
              message: '¿Desea radicar una solicitud formal de devolución para esta orden?',
              confirmText: 'Radicar Devolución'
            });
            if (!ok) return;
            try {
              await buyerApi.requestReturn(btn.dataset.orderId, 'Solicitud de devolución iniciada por el comprador desde su historial');
              showToast('Solicitud de devolución registrada exitosamente.', 'success');
              await loadOrdersAndReturns();
            } catch (err) {
              showToast(err.message, 'error');
            }
          });
        });

        ordersBox.querySelector('#buyer-orders-prev-page')?.addEventListener('click', () => {
          if (currentPage > 0) {
            currentPage -= 1;
            loadOrdersAndReturns();
          }
        });
        ordersBox.querySelector('#buyer-orders-next-page')?.addEventListener('click', () => {
          if (currentPage + 1 < (ordersPage.totalPages || 1)) {
            currentPage += 1;
            loadOrdersAndReturns();
          }
        });
      }

      const returnsList = returnsPage?.content || [];
      if (returnsList.length === 0) {
        returnsBox.innerHTML = `<p class="text-muted">No tiene solicitudes de devolución activas.</p>`;
      } else {
        returnsBox.innerHTML = `
          <table class="data-table">
            <thead>
              <tr>
                <th>ID Solicitud</th>
                <th>ID Orden</th>
                <th>Estado de Devolución</th>
              </tr>
            </thead>
            <tbody>
              ${returnsList
                .map(
                  (r) => `
                <tr>
                  <td><code>${escapeHtml(r.requestId)}</code></td>
                  <td><code>${escapeHtml(r.orderId)}</code></td>
                  <td><span class="badge badge-status-published">${escapeHtml(r.status)}</span></td>
                </tr>
              `
                )
                .join('')}
            </tbody>
          </table>
        `;
      }
    } catch (err) {
      ordersBox.innerHTML = renderErrorState(err.message);
    }
  }

  await loadOrdersAndReturns();
}

export async function renderBuyerProfileView(container) {
  const session = sessionStore.getSession();
  container.innerHTML = renderLoadingState('Cargando perfil y libreta de direcciones...');

  async function loadProfile() {
    try {
      const profile = await buyerApi.getProfile(session.userId);
      const primary = profile.primaryAddress || {};
      const secondaryList = profile.additionalAddresses || [];

      container.innerHTML = `
        <section class="page-header">
          <div>
            <h1>Mi Perfil de Comprador</h1>
            <p class="text-muted">${escapeHtml(profile.fullName)} &bull; ${escapeHtml(profile.email)}</p>
          </div>
          <span class="badge badge-status-published">Estado Comercial: ${escapeHtml(profile.commercialStatus)}</span>
        </section>

        <div class="dashboard-grid-2">
          <div class="card-panel">
            <h2>Datos de Identificación</h2>
            <dl class="product-specs">
              <div><dt>ID de Usuario</dt><dd><code>${escapeHtml(profile.userId)}</code></dd></div>
              <div><dt>Nombre Completo</dt><dd>${escapeHtml(profile.fullName)}</dd></div>
              <div><dt>Correo Electrónico</dt><dd>${escapeHtml(profile.email)}</dd></div>
              <div><dt>Documento</dt><dd>${escapeHtml(profile.documentType)} ${escapeHtml(profile.documentNumber)}</dd></div>
              <div><dt>Dirección Principal</dt><dd>${escapeHtml(primary.street || '')}, ${escapeHtml(primary.city || '')}, ${escapeHtml(primary.stateOrProvince || '')} (${escapeHtml(primary.postalCode || '')})</dd></div>
            </dl>
          </div>

          <div class="card-panel">
            <h2>Agregar Dirección Secundaria</h2>
            <form id="add-secondary-address-form">
              <div class="form-grid-2">
                <div class="form-group">
                  <label for="sec-street">Calle / Carrera *</label>
                  <input type="text" id="sec-street" name="street" class="form-control" required />
                </div>
                <div class="form-group">
                  <label for="sec-city">Ciudad *</label>
                  <input type="text" id="sec-city" name="city" class="form-control" required />
                </div>
                <div class="form-group">
                  <label for="sec-state">Departamento *</label>
                  <input type="text" id="sec-state" name="stateOrProvince" class="form-control" required />
                </div>
                <div class="form-group">
                  <label for="sec-country">País *</label>
                  <input type="text" id="sec-country" name="country" class="form-control" value="Colombia" required />
                </div>
                <div class="form-group">
                  <label for="sec-postal">Código Postal *</label>
                  <input type="text" id="sec-postal" name="postalCode" class="form-control" required />
                </div>
                <div class="form-group">
                  <label for="sec-ref">Referencia</label>
                  <input type="text" id="sec-ref" name="reference" class="form-control" />
                </div>
              </div>
              <button type="submit" class="btn btn-primary">Guardar Dirección Secundaria</button>
            </form>
          </div>
        </div>

        <section class="section-block card-panel">
          <h2>Direcciones Secundarias Guardadas (${secondaryList.length})</h2>
          ${
            secondaryList.length === 0
              ? `<p class="text-muted">No tiene direcciones secundarias registradas.</p>`
              : `
            <table class="data-table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Dirección</th>
                  <th>Ciudad / Departamento</th>
                  <th>Código Postal</th>
                  <th>Referencia</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                ${secondaryList
                  .map(
                    (addr, idx) => `
                  <tr>
                    <td>${idx + 1}</td>
                    <td>${escapeHtml(addr.street)}</td>
                    <td>${escapeHtml(addr.city)}, ${escapeHtml(addr.stateOrProvince)} (${escapeHtml(addr.country)})</td>
                    <td>${escapeHtml(addr.postalCode)}</td>
                    <td>${escapeHtml(addr.reference || '-')}</td>
                    <td>
                      <button type="button" class="btn btn-danger btn-xs js-delete-sec-addr" data-index="${idx}">
                        Eliminar
                      </button>
                    </td>
                  </tr>
                `
                  )
                  .join('')}
              </tbody>
            </table>
          `
          }
        </section>
      `;

      const addForm = container.querySelector('#add-secondary-address-form');
      addForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
          await buyerApi.addAddress(session.userId, {
            street: addForm.street.value.trim(),
            city: addForm.city.value.trim(),
            stateOrProvince: addForm.stateOrProvince.value.trim(),
            country: addForm.country.value.trim(),
            postalCode: addForm.postalCode.value.trim(),
            reference: addForm.reference.value.trim() || null
          });
          showToast('Dirección secundaria agregada.', 'success');
          await loadProfile();
        } catch (err) {
          showToast(err.message, 'error');
        }
      });

      container.querySelectorAll('.js-delete-sec-addr').forEach((btn) => {
        btn.addEventListener('click', async () => {
          try {
            await buyerApi.removeAddress(session.userId, Number(btn.dataset.index));
            showToast('Dirección secundaria eliminada.', 'info');
            await loadProfile();
          } catch (err) {
            showToast(err.message, 'error');
          }
        });
      });
    } catch (err) {
      container.innerHTML = renderErrorState(err.message);
    }
  }

  await loadProfile();
}
