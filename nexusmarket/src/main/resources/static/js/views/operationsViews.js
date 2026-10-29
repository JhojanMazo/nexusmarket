/**
 * Paneles Operativos y de Gobierno:
 * - Panel de Operador Logístico (#/logistics)
 * - Panel de Supervisor (#/supervisor)
 * - Panel Administrativo (#/admin)
 */

import { logisticsApi, supervisorApi, adminApi } from '../api/marketplaceApi.js';
import { sessionStore } from '../state/sessionStore.js';
import { formatMoney, ROLE_LABELS } from '../config/env.js';
import {
  escapeHtml,
  showToast,
  renderLoadingState,
  renderErrorState,
  renderOrderStatusBadge
} from '../components/uiComponents.js';

export async function renderLogisticsDashboardView(container) {
  const session = sessionStore.getSession();
  container.innerHTML = renderLoadingState('Cargando panel de operaciones logísticas...');

  async function loadLogistics() {
    try {
      const shipments = await logisticsApi.listShipments();

      container.innerHTML = `
        <section class="page-header">
          <div>
            <h1>Panel de Operador Logístico</h1>
            <p class="text-muted">Coordinación de despachos, confirmación de entregas físicas y movimientos de kardex.</p>
          </div>
        </section>

        <div class="dashboard-grid-2">
          <section class="card-panel">
            <h2>Despachar Orden Pagada</h2>
            <p class="text-small text-muted">Transiciona una orden en estado <code>PAID</code> a <code>SHIPPED</code> y genera su guía de envío.</p>
            <form id="logistics-dispatch-form">
              <div class="form-group">
                <label for="log-order-id">UUID de la Orden Pagada *</label>
                <input type="text" id="log-order-id" name="orderId" class="form-control"
                  placeholder="Ej. 550e8400-e29b-41d4-a716-446655440000" required />
              </div>
              <button type="submit" class="btn btn-primary">Generar Despacho</button>
            </form>
          </section>

          <section class="card-panel">
            <h2>Registrar Movimiento de Inventario (Kardex)</h2>
            <form id="logistics-movement-form">
              <div class="form-grid-2">
                <div class="form-group">
                  <label for="mov-inv-id">UUID del Lote de Inventario *</label>
                  <input type="text" id="mov-inv-id" name="inventoryId" class="form-control" required />
                </div>
                <div class="form-group">
                  <label for="mov-type">Tipo de Movimiento *</label>
                  <select id="mov-type" name="type" class="form-control">
                    <option value="INBOUND">INBOUND (Entrada)</option>
                    <option value="ADJUSTMENT">ADJUSTMENT (Ajuste)</option>
                    <option value="RETURN">RETURN (Retorno)</option>
                  </select>
                </div>
                <div class="form-group">
                  <label for="mov-qty">Cantidad *</label>
                  <input type="number" id="mov-qty" name="quantity" class="form-control" min="1" value="10" required />
                </div>
                <div class="form-group">
                  <label for="mov-ref">Referencia / Guía *</label>
                  <input type="text" id="mov-ref" name="reference" class="form-control" value="KARDEX-OP" required />
                </div>
              </div>
              <button type="submit" class="btn btn-secondary">Registrar Movimiento</button>
            </form>
          </section>
        </div>

        <section class="section-block card-panel">
          <h2>Envíos y Despachos Activos (${(shipments || []).length})</h2>
          ${
            !shipments || shipments.length === 0
              ? `<p class="text-muted">No hay envíos registrados actualmente.</p>`
              : `
            <table class="data-table">
              <thead>
                <tr>
                  <th>ID Envío</th>
                  <th>ID Orden</th>
                  <th>Operador Logístico</th>
                  <th>Estado de Envío</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                ${shipments
                  .map(
                    (s) => `
                  <tr>
                    <td><code>${escapeHtml(s.shipmentId)}</code></td>
                    <td><code>${escapeHtml(s.orderId)}</code></td>
                    <td><code>${escapeHtml(s.logisticsOperatorId)}</code></td>
                    <td><span class="badge badge-status-published">${escapeHtml(s.status)}</span></td>
                    <td>
                      ${
                        s.status !== 'DELIVERED'
                          ? `<button type="button" class="btn btn-primary btn-xs js-deliver-shipment"
                              data-shipment-id="${escapeHtml(s.shipmentId)}">
                              Confirmar Entrega Física
                            </button>`
                          : `<span class="text-muted">Entregado</span>`
                      }
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

      const dispatchForm = container.querySelector('#logistics-dispatch-form');
      dispatchForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
          await logisticsApi.dispatchOrder(dispatchForm.orderId.value.trim(), session.userId);
          showToast('Orden despachada exitosamente.', 'success');
          await loadLogistics();
        } catch (err) {
          showToast(err.message, 'error');
        }
      });

      const movForm = container.querySelector('#logistics-movement-form');
      movForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
          await logisticsApi.registerInventoryMovement(movForm.inventoryId.value.trim(), {
            quantity: Number(movForm.quantity.value),
            type: movForm.type.value,
            reference: movForm.reference.value.trim()
          });
          showToast('Movimiento de kardex registrado.', 'success');
          movForm.reset();
        } catch (err) {
          showToast(err.message, 'error');
        }
      });

      container.querySelectorAll('.js-deliver-shipment').forEach((btn) => {
        btn.addEventListener('click', async () => {
          try {
            await logisticsApi.confirmDelivery(btn.dataset.shipmentId);
            showToast('Entrega física confirmada (DELIVERED).', 'success');
            await loadLogistics();
          } catch (err) {
            showToast(err.message, 'error');
          }
        });
      });
    } catch (err) {
      container.innerHTML = renderErrorState(err.message);
    }
  }

  await loadLogistics();
}

export async function renderSupervisorDashboardView(container) {
  container.innerHTML = renderLoadingState('Cargando panel de supervisión y auditoría...');

  async function loadSupervisor() {
    try {
      const [report, returnsList, ordersList, operationsList] = await Promise.all([
        supervisorApi.getConsolidatedReport(),
        supervisorApi.getReturns(),
        supervisorApi.getOrders(),
        supervisorApi.getAuditOperations()
      ]);

      container.innerHTML = `
        <section class="page-header">
          <div>
            <h1>Panel de Supervisión y Calidad</h1>
            <p class="text-muted">Monitoreo consolidado de plataforma, resolución de devoluciones, disputas y bitácora operativa.</p>
          </div>
        </section>

        <section class="kpi-grid">
          <div class="kpi-card">
            <span class="kpi-label">Total Usuarios</span>
            <strong class="kpi-value">${report?.totalUsers ?? 0}</strong>
            <span class="kpi-sub">${report?.totalProducts ?? 0} productos</span>
          </div>
          <div class="kpi-card">
            <span class="kpi-label">Total Órdenes</span>
            <strong class="kpi-value">${report?.totalOrders ?? 0}</strong>
            <span class="kpi-sub">Ingresos: ${escapeHtml(formatMoney(report?.totalRevenue ?? 0, 'COP'))}</span>
          </div>
          <div class="kpi-card">
            <span class="kpi-label">Devoluciones</span>
            <strong class="kpi-value">${report?.totalReturns ?? 0}</strong>
            <span class="kpi-sub">${report?.totalRefunds ?? 0} reembolsos</span>
          </div>
          <div class="kpi-card">
            <span class="kpi-label">Unidades en Stock</span>
            <strong class="kpi-value">${report?.totalInventoryAvailableUnits ?? 0}</strong>
            <span class="kpi-sub">${report?.totalInventoryReservedUnits ?? 0} reservadas</span>
          </div>
        </section>

        <section class="section-block card-panel">
          <h2>Auditoría de Solicitudes de Devolución (${(returnsList || []).length})</h2>
          ${
            !returnsList || returnsList.length === 0
              ? `<p class="text-muted">No existen solicitudes de devolución pendientes.</p>`
              : `
            <table class="data-table">
              <thead>
                <tr>
                  <th>ID Solicitud</th>
                  <th>ID Orden</th>
                  <th>Estado</th>
                  <th>Decisiones de Supervisión</th>
                </tr>
              </thead>
              <tbody>
                ${returnsList
                  .map(
                    (ret) => `
                  <tr>
                    <td><code>${escapeHtml(ret.requestId)}</code></td>
                    <td><code>${escapeHtml(ret.orderId)}</code></td>
                    <td><span class="badge badge-status-published">${escapeHtml(ret.status)}</span></td>
                    <td>
                      <div class="inline-actions">
                        ${
                          ret.status === 'REQUESTED'
                            ? `
                          <button type="button" class="btn btn-primary btn-xs js-sup-approve-ret" data-id="${escapeHtml(ret.requestId)}">Aprobar</button>
                          <button type="button" class="btn btn-danger btn-xs js-sup-reject-ret" data-id="${escapeHtml(ret.requestId)}">Rechazar</button>
                        `
                            : ''
                        }
                        ${
                          ret.status === 'APPROVED'
                            ? `<button type="button" class="btn btn-secondary btn-xs js-sup-refund-ret" data-id="${escapeHtml(ret.requestId)}">Procesar Reembolso</button>`
                            : ''
                        }
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
          <h2>Supervisión de Órdenes del Sistema (${(ordersList || []).length})</h2>
          <table class="data-table">
            <thead>
              <tr>
                <th>ID Orden</th>
                <th>Comprador</th>
                <th>Fecha</th>
                <th>Estado</th>
                <th>Total</th>
              </tr>
            </thead>
            <tbody>
              ${(ordersList || [])
                .slice(0, 10)
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
        </section>

        <section class="section-block card-panel">
          <h2>Bitácora de Operaciones Auditadas (${(operationsList || []).length})</h2>
          ${
            !operationsList || operationsList.length === 0
              ? `<p class="text-muted">Sin operaciones registradas en la bitácora relacional.</p>`
              : `
            <table class="data-table">
              <thead>
                <tr>
                  <th>Tipo</th>
                  <th>Descripción</th>
                  <th>Estado</th>
                  <th>Fecha</th>
                </tr>
              </thead>
              <tbody>
                ${operationsList
                  .map(
                    (op) => `
                  <tr>
                    <td><code>${escapeHtml(op.operationType)}</code></td>
                    <td>${escapeHtml(op.description)}</td>
                    <td><span class="badge badge-status-published">${escapeHtml(op.status)}</span></td>
                    <td>${escapeHtml(op.createdAt)}</td>
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

      container.querySelectorAll('.js-sup-approve-ret').forEach((btn) => {
        btn.addEventListener('click', async () => {
          try {
            await supervisorApi.approveReturn(btn.dataset.id);
            showToast('Devolución aprobada.', 'success');
            await loadSupervisor();
          } catch (err) {
            showToast(err.message, 'error');
          }
        });
      });

      container.querySelectorAll('.js-sup-reject-ret').forEach((btn) => {
        btn.addEventListener('click', async () => {
          try {
            await supervisorApi.rejectReturn(btn.dataset.id);
            showToast('Devolución rechazada.', 'info');
            await loadSupervisor();
          } catch (err) {
            showToast(err.message, 'error');
          }
        });
      });

      container.querySelectorAll('.js-sup-refund-ret').forEach((btn) => {
        btn.addEventListener('click', async () => {
          try {
            await supervisorApi.processRefund(btn.dataset.id, 100000);
            showToast('Reembolso procesado exitosamente.', 'success');
            await loadSupervisor();
          } catch (err) {
            showToast(err.message, 'error');
          }
        });
      });
    } catch (err) {
      container.innerHTML = renderErrorState(err.message);
    }
  }

  await loadSupervisor();
}

export async function renderAdminDashboardView(container) {
  const session = sessionStore.getSession();
  container.innerHTML = renderLoadingState('Cargando consola de gobierno y administración...');

  async function loadAdmin() {
    try {
      const [report, usersPage, warehousesPage, invoicesPage, returnsPage, ordersPage] = await Promise.all([
        adminApi.getConsolidatedReport(),
        adminApi.getUsers({ page: 0, size: 25 }),
        adminApi.getWarehouses({ page: 0, size: 20 }),
        adminApi.getInvoices({ page: 0, size: 20 }),
        adminApi.getReturns({ page: 0, size: 20 }),
        adminApi.getOrders({ page: 0, size: 15 })
      ]);

      const users = usersPage?.content || [];
      const warehouses = warehousesPage?.content || [];
      const invoices = invoicesPage?.content || [];
      const returnsList = returnsPage?.content || [];
      const orders = ordersPage?.content || [];

      container.innerHTML = `
        <section class="page-header">
          <div>
            <h1>Panel Administrativo de Plataforma</h1>
            <p class="text-muted">Gobierno de usuarios, alta de vendedores y personal, bodegas, facturación fiscal y reportes consolidados.</p>
          </div>
        </section>

        <section class="kpi-grid" aria-label="Indicadores globales de NexusMarket">
          <div class="kpi-card">
            <span class="kpi-label">Usuarios Registrados</span>
            <strong class="kpi-value">${report?.totalUsers ?? users.length}</strong>
            <span class="kpi-sub">Control de acceso RBAC activo</span>
          </div>
          <div class="kpi-card">
            <span class="kpi-label">Catálogo y Bodegas</span>
            <strong class="kpi-value">${report?.totalProducts ?? 0} prod.</strong>
            <span class="kpi-sub">${report?.totalWarehouses ?? warehouses.length} bodegas operativas</span>
          </div>
          <div class="kpi-card">
            <span class="kpi-label">Órdenes y Facturas</span>
            <strong class="kpi-value">${report?.totalOrders ?? orders.length} órd.</strong>
            <span class="kpi-sub">${report?.totalInvoices ?? invoices.length} facturas emitidas</span>
          </div>
          <div class="kpi-card">
            <span class="kpi-label">Ingresos Consolidados</span>
            <strong class="kpi-value">${escapeHtml(formatMoney(report?.totalRevenue ?? 0, 'COP'))}</strong>
            <span class="kpi-sub">Stock disponible: ${report?.totalInventoryAvailableUnits ?? 0} u.</span>
          </div>
        </section>

        <div class="dashboard-grid-2">
          <section class="card-panel">
            <h2>Dar de Alta Vendedor con Bodega</h2>
            <form id="admin-create-seller-form">
              <div class="form-grid-2">
                <div class="form-group">
                  <label for="as-name">Razón Social / Nombre *</label>
                  <input type="text" id="as-name" name="fullName" class="form-control" required />
                </div>
                <div class="form-group">
                  <label for="as-email">Correo Comercial *</label>
                  <input type="email" id="as-email" name="email" class="form-control" required />
                </div>
                <div class="form-group">
                  <label for="as-doctype">Tipo Doc. *</label>
                  <select id="as-doctype" name="documentType" class="form-control">
                    <option value="NIT">NIT</option>
                    <option value="CC">CC</option>
                  </select>
                </div>
                <div class="form-group">
                  <label for="as-docnum">Número Doc. *</label>
                  <input type="text" id="as-docnum" name="documentNumber" class="form-control" required />
                </div>
                <div class="form-group">
                  <label for="as-whname">Nombre de Primera Bodega *</label>
                  <input type="text" id="as-whname" name="warehouseName" class="form-control"
                    placeholder="Ej. Bodega Norte Comercio" required />
                </div>
                <div class="form-group">
                  <label for="as-whcap">Capacidad de Bodega *</label>
                  <input type="number" id="as-whcap" name="warehouseCapacity" class="form-control" value="10000" min="100" required />
                </div>
              </div>
              <button type="submit" class="btn btn-primary">Registrar Vendedor + Bodega</button>
            </form>
          </section>

          <section class="card-panel">
            <h2>Registrar Personal Interno (Staff)</h2>
            <form id="admin-create-staff-form">
              <div class="form-grid-2">
                <div class="form-group">
                  <label for="st-name">Nombre Completo *</label>
                  <input type="text" id="st-name" name="fullName" class="form-control" required />
                </div>
                <div class="form-group">
                  <label for="st-email">Correo Institucional *</label>
                  <input type="email" id="st-email" name="email" class="form-control" required />
                </div>
                <div class="form-group">
                  <label for="st-docnum">Documento (CC) *</label>
                  <input type="text" id="st-docnum" name="documentNumber" class="form-control" required />
                </div>
                <div class="form-group">
                  <label for="st-role">Rol Asignado *</label>
                  <select id="st-role" name="role" class="form-control">
                    <option value="LOGISTICS_OPERATOR">Operador Logístico</option>
                    <option value="SUPERVISOR">Supervisor</option>
                    <option value="ADMINISTRATOR">Administrador</option>
                  </select>
                </div>
              </div>
              <button type="submit" class="btn btn-primary">Registrar Personal</button>
            </form>
          </section>
        </div>

        <section class="section-block card-panel">
          <h2>Gestión de Usuarios y Estado Operativo (${users.length})</h2>
          <table class="data-table">
            <thead>
              <tr>
                <th>Nombre</th>
                <th>Correo</th>
                <th>Documento</th>
                <th>Rol</th>
                <th>Estado</th>
                <th>Control de Cuenta</th>
              </tr>
            </thead>
            <tbody>
              ${users
                .map(
                  (u) => `
                <tr>
                  <td><strong>${escapeHtml(u.fullName)}</strong></td>
                  <td>${escapeHtml(u.email)}</td>
                  <td>${escapeHtml(u.documentType)} ${escapeHtml(u.documentNumber)}</td>
                  <td><span class="badge badge-type-physical">${escapeHtml(ROLE_LABELS[u.role] || u.role)}</span></td>
                  <td><span class="badge badge-status-${u.status === 'ACTIVE' ? 'published' : 'suspended'}">${escapeHtml(u.status)}</span></td>
                  <td>
                    <select class="form-control form-control-sm js-admin-user-status"
                      data-user-id="${escapeHtml(u.userId)}" aria-label="Estado de ${escapeHtml(u.fullName)}">
                      <option value="ACTIVE" ${u.status === 'ACTIVE' ? 'selected' : ''}>ACTIVE (Activo)</option>
                      <option value="BLOCKED" ${u.status === 'BLOCKED' ? 'selected' : ''}>BLOCKED (Bloqueado)</option>
                      <option value="INACTIVE" ${u.status === 'INACTIVE' ? 'selected' : ''}>INACTIVE (Inactivo)</option>
                    </select>
                  </td>
                </tr>
              `
                )
                .join('')}
            </tbody>
          </table>
        </section>

        <div class="dashboard-grid-2">
          <section class="card-panel">
            <h2>Red de Bodegas (${warehouses.length})</h2>
            <table class="data-table data-table-compact">
              <thead>
                <tr>
                  <th>Nombre</th>
                  <th>Tipo</th>
                  <th>Capacidad</th>
                </tr>
              </thead>
              <tbody>
                ${warehouses
                  .map(
                    (w) => `
                  <tr>
                    <td><strong>${escapeHtml(w.name)}</strong></td>
                    <td>${escapeHtml(w.type)}</td>
                    <td>${escapeHtml(w.capacity)}</td>
                  </tr>
                `
                  )
                  .join('')}
              </tbody>
            </table>
          </section>

          <section class="card-panel">
            <h2>Facturas Fiscales Emitidas (${invoices.length})</h2>
            ${
              invoices.length === 0
                ? `<p class="text-muted">Las facturas se generan automáticamente al pagarse una orden o manualmente desde administración.</p>`
                : `
              <table class="data-table data-table-compact">
                <thead>
                  <tr>
                    <th>ID Factura</th>
                    <th>ID Orden</th>
                    <th>Fecha</th>
                    <th>Total</th>
                  </tr>
                </thead>
                <tbody>
                  ${invoices
                    .map(
                      (inv) => `
                    <tr>
                      <td><code>${escapeHtml(inv.invoiceId)}</code></td>
                      <td><code>${escapeHtml(inv.orderId)}</code></td>
                      <td>${escapeHtml(inv.issueDate)}</td>
                      <td><strong>${escapeHtml(formatMoney(inv.total, inv.currency))}</strong></td>
                    </tr>
                  `
                    )
                    .join('')}
                </tbody>
              </table>
            `
            }
          </section>
        </div>

        <section class="section-block card-panel">
          <h2>Supervisión de Pedidos y Facturación Manual (${orders.length})</h2>
          <table class="data-table">
            <thead>
              <tr>
                <th>ID Orden</th>
                <th>Comprador</th>
                <th>Estado</th>
                <th>Total</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              ${orders
                .map(
                  (o) => `
                <tr>
                  <td><code>${escapeHtml(o.orderId)}</code></td>
                  <td>${escapeHtml(o.buyerName || o.buyerId)}</td>
                  <td>${renderOrderStatusBadge(o.status)}</td>
                  <td><strong>${escapeHtml(formatMoney(o.total, o.currency))}</strong></td>
                  <td>
                    ${
                      o.status !== 'CART'
                        ? `<button type="button" class="btn btn-secondary btn-xs js-admin-gen-invoice"
                            data-order-id="${escapeHtml(o.orderId)}" data-currency="${escapeHtml(o.currency)}">
                            Emitir Factura
                          </button>`
                        : ''
                    }
                  </td>
                </tr>
              `
                )
                .join('')}
            </tbody>
          </table>
        </section>

        <section class="section-block card-panel">
          <h2>Gestión de Devoluciones (${returnsList.length})</h2>
          ${
            returnsList.length === 0
              ? `<p class="text-muted">No hay solicitudes de devolución registradas.</p>`
              : `
            <table class="data-table">
              <thead>
                <tr>
                  <th>ID Solicitud</th>
                  <th>ID Orden</th>
                  <th>Estado</th>
                  <th>Acciones</th>
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
                    <td>
                      ${
                        r.status === 'REQUESTED'
                          ? `
                        <button type="button" class="btn btn-primary btn-xs js-adm-approve-ret" data-id="${escapeHtml(r.requestId)}">Aprobar</button>
                        <button type="button" class="btn btn-danger btn-xs js-adm-reject-ret" data-id="${escapeHtml(r.requestId)}">Rechazar</button>
                      `
                          : ''
                      }
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

      const sellerForm = container.querySelector('#admin-create-seller-form');
      sellerForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
          await adminApi.registerSeller(
            {
              fullName: sellerForm.fullName.value.trim(),
              email: sellerForm.email.value.trim(),
              documentType: sellerForm.documentType.value,
              documentNumber: sellerForm.documentNumber.value.trim(),
              warehouseName: sellerForm.warehouseName.value.trim(),
              warehouseCapacity: Number(sellerForm.warehouseCapacity.value),
              warehouseAddress: {
                street: 'Parque Industrial Zona Franca',
                city: 'Bogotá',
                stateOrProvince: 'Cundinamarca',
                country: 'Colombia',
                postalCode: '110911',
                reference: 'Bodega Comercial'
              }
            },
            session.userId
          );
          showToast('Vendedor y bodega registrados exitosamente.', 'success');
          await loadAdmin();
        } catch (err) {
          showToast(err.message, 'error');
        }
      });

      const staffForm = container.querySelector('#admin-create-staff-form');
      staffForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
          await adminApi.registerStaff({
            fullName: staffForm.fullName.value.trim(),
            email: staffForm.email.value.trim(),
            documentType: 'CC',
            documentNumber: staffForm.documentNumber.value.trim(),
            role: staffForm.role.value
          });
          showToast('Personal interno registrado correctamente.', 'success');
          await loadAdmin();
        } catch (err) {
          showToast(err.message, 'error');
        }
      });

      container.querySelectorAll('.js-admin-user-status').forEach((sel) => {
        sel.addEventListener('change', async () => {
          try {
            await adminApi.updateUserStatus(sel.dataset.userId, sel.value);
            showToast(`Estado del usuario actualizado a ${sel.value}.`, 'success');
            await loadAdmin();
          } catch (err) {
            showToast(err.message, 'error');
          }
        });
      });

      container.querySelectorAll('.js-admin-gen-invoice').forEach((btn) => {
        btn.addEventListener('click', async () => {
          try {
            await adminApi.generateInvoice(btn.dataset.orderId, 0, btn.dataset.currency || 'COP');
            showToast('Factura fiscal generada correctamente.', 'success');
            await loadAdmin();
          } catch (err) {
            showToast(err.message, 'error');
          }
        });
      });

      container.querySelectorAll('.js-adm-approve-ret').forEach((btn) => {
        btn.addEventListener('click', async () => {
          try {
            await adminApi.approveReturn(btn.dataset.id);
            showToast('Devolución aprobada.', 'success');
            await loadAdmin();
          } catch (err) {
            showToast(err.message, 'error');
          }
        });
      });

      container.querySelectorAll('.js-adm-reject-ret').forEach((btn) => {
        btn.addEventListener('click', async () => {
          try {
            await adminApi.rejectReturn(btn.dataset.id);
            showToast('Devolución rechazada.', 'info');
            await loadAdmin();
          } catch (err) {
            showToast(err.message, 'error');
          }
        });
      });
    } catch (err) {
      container.innerHTML = renderErrorState(err.message);
    }
  }

  await loadAdmin();
}
