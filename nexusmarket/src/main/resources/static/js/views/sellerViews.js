/**
 * Panel Privado del Vendedor (SELLER - #/seller):
 * - Resumen de métricas comerciales
 * - Creación de productos, actualización de precios y cambio de estado (PUBLISHED / DRAFT / SUSPENDED / RETIRED)
 * - Gestión de bodegas del vendedor, registro de lote inicial de stock y ajuste manual de inventario
 * - Consulta de órdenes asociadas
 */

import { sellerApi } from '../api/marketplaceApi.js';
import { sessionStore } from '../state/sessionStore.js';
import { formatMoney, PRODUCT_TYPE_LABELS } from '../config/env.js';
import {
  escapeHtml,
  showToast,
  renderLoadingState,
  renderErrorState,
  renderProductStatusBadge,
  renderOrderStatusBadge
} from '../components/uiComponents.js';

export async function renderSellerDashboardView(container) {
  const session = sessionStore.getSession();
  container.innerHTML = renderLoadingState('Cargando panel comercial del vendedor...');

  async function loadDashboard() {
    try {
      const [productsPage, warehousesPage, inventoryPage, ordersPage] = await Promise.all([
        sellerApi.getProducts({ page: 0, size: 25 }),
        sellerApi.getWarehouses(session.userId, { page: 0, size: 20 }).catch(() => ({ content: [] })),
        sellerApi.getInventory(session.userId, { page: 0, size: 20 }).catch(() => ({ content: [] })),
        sellerApi.getOrders(session.userId, { page: 0, size: 15 }).catch(() => ({ content: [] }))
      ]);

      const products = productsPage?.content || [];
      const warehouses = warehousesPage?.content || [];
      const inventories = inventoryPage?.content || [];
      const orders = ordersPage?.content || [];

      const publishedCount = products.filter((p) => p.status === 'PUBLISHED').length;
      const totalStockUnits = inventories.reduce((acc, inv) => acc + Number(inv.availableQuantity || 0), 0);

      container.innerHTML = `
        <section class="page-header">
          <div>
            <h1>Panel Comercial del Vendedor</h1>
            <p class="text-muted">Administre su catálogo, existencias por bodega, precios y órdenes (${escapeHtml(session.email)}).</p>
          </div>
        </section>

        <section class="kpi-grid" aria-label="Indicadores del vendedor">
          <div class="kpi-card">
            <span class="kpi-label">Productos en Catálogo</span>
            <strong class="kpi-value">${products.length}</strong>
            <span class="kpi-sub">${publishedCount} publicados activos</span>
          </div>
          <div class="kpi-card">
            <span class="kpi-label">Bodegas Asignadas</span>
            <strong class="kpi-value">${warehouses.length}</strong>
            <span class="kpi-sub">Control de propiedad verificado</span>
          </div>
          <div class="kpi-card">
            <span class="kpi-label">Unidades Disponibles</span>
            <strong class="kpi-value">${totalStockUnits}</strong>
            <span class="kpi-sub">${inventories.length} lotes activos</span>
          </div>
          <div class="kpi-card">
            <span class="kpi-label">Órdenes Registradas</span>
            <strong class="kpi-value">${orders.length}</strong>
            <span class="kpi-sub">Trazabilidad en tiempo real</span>
          </div>
        </section>

        <div class="dashboard-grid-2">
          <section class="card-panel" aria-labelledby="seller-create-product-heading">
            <h2 id="seller-create-product-heading">Publicar Nuevo Producto</h2>
            <form id="seller-create-product-form">
              <div class="form-grid-2">
                <div class="form-group">
                  <label for="sp-name">Nombre del Producto *</label>
                  <input type="text" id="sp-name" name="name" class="form-control"
                    placeholder="Ej. Servidor Rack Xeon Gold" required maxlength="150" />
                </div>
                <div class="form-group">
                  <label for="sp-type">Tipo de Producto *</label>
                  <select id="sp-type" name="type" class="form-control" required>
                    <option value="PHYSICAL">Físico (Requiere Stock y Envío)</option>
                    <option value="DIGITAL">Digital (Entrega Inmediata)</option>
                  </select>
                </div>
                <div class="form-group">
                  <label for="sp-attr">Atributo de Variante *</label>
                  <input type="text" id="sp-attr" name="variantAttribute" class="form-control"
                    placeholder="Ej. Capacidad / Edición" required />
                </div>
                <div class="form-group">
                  <label for="sp-val">Valor de Variante *</label>
                  <input type="text" id="sp-val" name="variantValue" class="form-control"
                    placeholder="Ej. 64GB ECC / Enterprise" required />
                </div>
                <div class="form-group">
                  <label for="sp-price">Precio Unitario *</label>
                  <input type="number" id="sp-price" name="price" class="form-control"
                    min="1" step="0.01" placeholder="1250000" required />
                </div>
                <div class="form-group">
                  <label for="sp-currency">Moneda *</label>
                  <select id="sp-currency" name="currency" class="form-control">
                    <option value="COP" selected>COP (Peso Colombiano)</option>
                    <option value="USD">USD (Dólar Estadounidense)</option>
                  </select>
                </div>
                <div class="form-group">
                  <label for="sp-status">Estado Inicial *</label>
                  <select id="sp-status" name="status" class="form-control">
                    <option value="PUBLISHED" selected>Publicado (Visible en Catálogo)</option>
                    <option value="DRAFT">Borrador</option>
                  </select>
                </div>
              </div>
              <button type="submit" class="btn btn-primary">Crear Producto</button>
            </form>
          </section>

          <section class="card-panel" aria-labelledby="seller-stock-init-heading">
            <h2 id="seller-stock-init-heading">Registrar Lote Inicial de Inventario</h2>
            ${
              warehouses.length === 0
                ? `<p class="text-muted">No tiene bodegas propias asignadas aún. Solicite al Administrador la asignación de una bodega de vendedor.</p>`
                : `
              <form id="seller-initial-stock-form">
                <div class="form-group">
                  <label for="inv-product-id">Producto *</label>
                  <select id="inv-product-id" name="productId" class="form-control" required>
                    ${products
                      .filter((p) => p.type === 'PHYSICAL')
                      .map((p) => `<option value="${escapeHtml(p.id)}">${escapeHtml(p.name)} (${escapeHtml(p.variantValue)})</option>`)
                      .join('')}
                  </select>
                </div>
                <div class="form-group">
                  <label for="inv-warehouse-id">Bodega del Vendedor *</label>
                  <select id="inv-warehouse-id" name="warehouseId" class="form-control" required>
                    ${warehouses
                      .map((w) => `<option value="${escapeHtml(w.warehouseId)}">${escapeHtml(w.name)} — Cap: ${escapeHtml(w.capacity)}</option>`)
                      .join('')}
                  </select>
                </div>
                <div class="form-group">
                  <label for="inv-qty">Cantidad de Entrada *</label>
                  <input type="number" id="inv-qty" name="quantity" class="form-control" min="1" value="25" required />
                </div>
                <button type="submit" class="btn btn-primary">Registrar Entrada de Stock</button>
              </form>
            `
            }
          </section>
        </div>

        <section class="section-block card-panel">
          <h2>Administración de Productos y Precios</h2>
          <table class="data-table">
            <thead>
              <tr>
                <th>Producto</th>
                <th>Variante</th>
                <th>Tipo</th>
                <th>Precio Actual</th>
                <th>Estado</th>
                <th>Actualizar Precio / Estado</th>
              </tr>
            </thead>
            <tbody>
              ${products
                .map(
                  (p) => `
                <tr>
                  <td><strong>${escapeHtml(p.name)}</strong></td>
                  <td>${escapeHtml(p.variantAttribute)}: ${escapeHtml(p.variantValue)}</td>
                  <td>${escapeHtml(PRODUCT_TYPE_LABELS[p.type] || p.type)}</td>
                  <td><strong>${escapeHtml(formatMoney(p.price, p.currency))}</strong></td>
                  <td>${renderProductStatusBadge(p.status)}</td>
                  <td>
                    <div class="inline-actions">
                      <input type="number" class="form-control form-control-sm js-price-input"
                        data-product-id="${escapeHtml(p.id)}" value="${escapeHtml(p.price)}" min="1" step="100"
                        aria-label="Nuevo precio para ${escapeHtml(p.name)}" />
                      <button type="button" class="btn btn-secondary btn-xs js-update-price-btn"
                        data-product-id="${escapeHtml(p.id)}" data-currency="${escapeHtml(p.currency)}">
                        Guardar Precio
                      </button>
                      <select class="form-control form-control-sm js-status-select"
                        data-product-id="${escapeHtml(p.id)}" aria-label="Cambiar estado de ${escapeHtml(p.name)}">
                        <option value="PUBLISHED" ${p.status === 'PUBLISHED' ? 'selected' : ''}>Publicado</option>
                        <option value="DRAFT" ${p.status === 'DRAFT' ? 'selected' : ''}>Borrador</option>
                        <option value="SUSPENDED" ${p.status === 'SUSPENDED' ? 'selected' : ''}>Suspendido</option>
                        <option value="RETIRED" ${p.status === 'RETIRED' ? 'selected' : ''}>Retirado</option>
                      </select>
                    </div>
                  </td>
                </tr>
              `
                )
                .join('')}
            </tbody>
          </table>
        </section>

        <section class="section-block card-panel">
          <h2>Inventario Distribuido en mis Bodegas</h2>
          ${
            inventories.length === 0
              ? `<p class="text-muted">No hay lotes de inventario registrados para sus bodegas.</p>`
              : `
            <table class="data-table">
              <thead>
                <tr>
                  <th>Producto</th>
                  <th>Bodega</th>
                  <th>Disponible</th>
                  <th>Reservado</th>
                  <th>Estado</th>
                  <th>Ajuste de Existencias</th>
                </tr>
              </thead>
              <tbody>
                ${inventories
                  .map(
                    (inv) => `
                  <tr>
                    <td><strong>${escapeHtml(inv.productName)}</strong></td>
                    <td>${escapeHtml(inv.warehouseName)}</td>
                    <td><strong>${escapeHtml(inv.availableQuantity)}</strong></td>
                    <td>${escapeHtml(inv.reservedQuantity)}</td>
                    <td><span class="badge badge-status-published">${escapeHtml(inv.status)}</span></td>
                    <td>
                      <div class="inline-actions">
                        <input type="number" class="form-control form-control-sm js-adjust-qty-input"
                          data-inventory-id="${escapeHtml(inv.inventoryId)}" value="${escapeHtml(inv.availableQuantity)}" min="0"
                          aria-label="Nueva cantidad para ${escapeHtml(inv.productName)}" />
                        <button type="button" class="btn btn-secondary btn-xs js-adjust-stock-btn"
                          data-inventory-id="${escapeHtml(inv.inventoryId)}">
                          Ajustar Stock
                        </button>
                      </div>
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

        <section class="section-block card-panel">
          <h2>Pedidos Asociados (${orders.length})</h2>
          ${
            orders.length === 0
              ? `<p class="text-muted">Aún no se registran pedidos.</p>`
              : `
            <table class="data-table">
              <thead>
                <tr>
                  <th>ID Pedido</th>
                  <th>Comprador</th>
                  <th>Fecha</th>
                  <th>Estado</th>
                  <th>Total</th>
                </tr>
              </thead>
              <tbody>
                ${orders
                  .map(
                    (o) => `
                  <tr>
                    <td><code>${escapeHtml(o.orderId)}</code></td>
                    <td>${escapeHtml(o.buyerName || o.buyerId)}</td>
                    <td>${escapeHtml(o.creationDate)}</td>
                    <td>${renderOrderStatusBadge(o.status)}</td>
                    <td><strong>${escapeHtml(formatMoney(o.total, o.currency))}</strong></td>
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

      const createForm = container.querySelector('#seller-create-product-form');
      createForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
          await sellerApi.createProduct({
            name: createForm.name.value.trim(),
            variantAttribute: createForm.variantAttribute.value.trim(),
            variantValue: createForm.variantValue.value.trim(),
            price: Number(createForm.price.value),
            currency: createForm.currency.value,
            status: createForm.status.value,
            type: createForm.type.value
          });
          showToast('Producto creado y caché de catálogo actualizada.', 'success');
          await loadDashboard();
        } catch (err) {
          showToast(err.message, 'error');
        }
      });

      const stockForm = container.querySelector('#seller-initial-stock-form');
      stockForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
          await sellerApi.registerInitialStock(session.userId, {
            productId: stockForm.productId.value,
            warehouseId: stockForm.warehouseId.value,
            quantity: Number(stockForm.quantity.value)
          });
          showToast('Stock inicial registrado correctamente.', 'success');
          await loadDashboard();
        } catch (err) {
          showToast(err.message, 'error');
        }
      });

      container.querySelectorAll('.js-update-price-btn').forEach((btn) => {
        btn.addEventListener('click', async () => {
          const productId = btn.dataset.productId;
          const currency = btn.dataset.currency || 'COP';
          const input = container.querySelector(`.js-price-input[data-product-id="${productId}"]`);
          const newPrice = Number(input?.value);
          if (!newPrice || newPrice <= 0) {
            showToast('Ingrese un precio válido mayor a 0.', 'error');
            return;
          }
          try {
            await sellerApi.updateProductPrice(productId, newPrice, currency);
            showToast('Precio actualizado exitosamente.', 'success');
            await loadDashboard();
          } catch (err) {
            showToast(err.message, 'error');
          }
        });
      });

      container.querySelectorAll('.js-status-select').forEach((sel) => {
        sel.addEventListener('change', async () => {
          try {
            await sellerApi.updateProductStatus(sel.dataset.productId, sel.value);
            showToast(`Estado del producto actualizado a ${sel.value}.`, 'success');
            await loadDashboard();
          } catch (err) {
            showToast(err.message, 'error');
          }
        });
      });

      container.querySelectorAll('.js-adjust-stock-btn').forEach((btn) => {
        btn.addEventListener('click', async () => {
          const invId = btn.dataset.inventoryId;
          const input = container.querySelector(`.js-adjust-qty-input[data-inventory-id="${invId}"]`);
          const newQty = Number(input?.value ?? 0);
          try {
            await sellerApi.adjustStock(session.userId, invId, {
              quantity: newQty,
              reason: 'Ajuste operativo desde Panel del Vendedor'
            });
            showToast('Existencias de inventario actualizadas.', 'success');
            await loadDashboard();
          } catch (err) {
            showToast(err.message, 'error');
          }
        });
      });
    } catch (err) {
      container.innerHTML = renderErrorState(err.message);
    }
  }

  await loadDashboard();
}
