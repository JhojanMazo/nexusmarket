/**
 * Componentes de interfaz reutilizables, accesibles y seguros contra XSS.
 */

import {
  formatMoney,
  buildProductIllustrationSvg,
  PRODUCT_TYPE_LABELS,
  PRODUCT_STATUS_LABELS,
  ORDER_STATUS_LABELS
} from '../config/env.js';

export function escapeHtml(str) {
  if (str === null || str === undefined) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

export function showToast(message, type = 'info') {
  if (typeof document === 'undefined') return;
  const container = document.getElementById('toast-container');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.setAttribute('role', type === 'error' ? 'alert' : 'status');
  toast.setAttribute('aria-live', type === 'error' ? 'assertive' : 'polite');
  toast.innerHTML = `
    <span class="toast-message">${escapeHtml(message)}</span>
    <button type="button" class="toast-close" aria-label="Cerrar notificación">&times;</button>
  `;

  toast.querySelector('.toast-close')?.addEventListener('click', () => toast.remove());
  container.appendChild(toast);

  setTimeout(() => {
    if (toast.parentNode) {
      toast.remove();
    }
  }, 4500);
}

export function confirmDialog({ title, message, confirmText = 'Confirmar', cancelText = 'Cancelar', danger = false }) {
  return new Promise((resolve) => {
    if (typeof document === 'undefined') {
      resolve(true);
      return;
    }
    const overlay = document.getElementById('modal-overlay');
    if (!overlay) {
      resolve(window.confirm(message));
      return;
    }

    overlay.innerHTML = `
      <div class="modal-dialog" role="dialog" aria-modal="true" aria-labelledby="modal-title">
        <h3 id="modal-title" class="modal-title">${escapeHtml(title)}</h3>
        <p class="modal-body">${escapeHtml(message)}</p>
        <div class="modal-actions">
          <button type="button" id="modal-cancel-btn" class="btn btn-secondary">${escapeHtml(cancelText)}</button>
          <button type="button" id="modal-confirm-btn" class="btn ${danger ? 'btn-danger' : 'btn-primary'}">${escapeHtml(confirmText)}</button>
        </div>
      </div>
    `;
    overlay.classList.remove('hidden');

    const cleanup = (result) => {
      overlay.classList.add('hidden');
      overlay.innerHTML = '';
      resolve(result);
    };

    const confirmBtn = document.getElementById('modal-confirm-btn');
    const cancelBtn = document.getElementById('modal-cancel-btn');
    confirmBtn?.focus();

    confirmBtn?.addEventListener('click', () => cleanup(true));
    cancelBtn?.addEventListener('click', () => cleanup(false));
  });
}

export function renderLoadingState(message = 'Cargando información desde NexusMarket...') {
  return `
    <div class="state-box state-loading" role="status" aria-live="polite">
      <div class="spinner" aria-hidden="true"></div>
      <p>${escapeHtml(message)}</p>
    </div>
  `;
}

export function renderErrorState(message = 'No fue posible cargar la información.', retryId = '') {
  return `
    <div class="state-box state-error" role="alert">
      <h3>Ocurrió un inconveniente</h3>
      <p>${escapeHtml(message)}</p>
      ${retryId ? `<button type="button" id="${escapeHtml(retryId)}" class="btn btn-secondary">Reintentar</button>` : ''}
    </div>
  `;
}

export function renderEmptyState(title, description, actionHtml = '') {
  return `
    <div class="state-box state-empty">
      <h3>${escapeHtml(title)}</h3>
      <p>${escapeHtml(description)}</p>
      ${actionHtml}
    </div>
  `;
}

export function renderOrderStatusBadge(status) {
  const label = ORDER_STATUS_LABELS[status] || status;
  const cls = String(status || '').toLowerCase().replace(/_/g, '-');
  return `<span class="badge badge-order-${escapeHtml(cls)}">${escapeHtml(label)}</span>`;
}

export function renderProductStatusBadge(status) {
  const label = PRODUCT_STATUS_LABELS[status] || status;
  const cls = String(status || '').toLowerCase();
  return `<span class="badge badge-status-${escapeHtml(cls)}">${escapeHtml(label)}</span>`;
}

export function renderProductCard(product, { showAddToCart = true } = {}) {
  const imgUrl = buildProductIllustrationSvg(product);
  const typeLabel = PRODUCT_TYPE_LABELS[product.type] || product.type;
  const formattedPrice = formatMoney(product.price, product.currency);

  return `
    <article class="product-card" data-product-id="${escapeHtml(product.id)}">
      <a href="#/product/${escapeHtml(product.id)}" class="product-card-media-link" aria-label="Ver detalle de ${escapeHtml(product.name)}">
        <img src="${imgUrl}" alt="${escapeHtml(product.name)}" class="product-card-img" loading="lazy" />
      </a>
      <div class="product-card-body">
        <div class="product-card-tags">
          <span class="badge badge-type-${escapeHtml(String(product.type).toLowerCase())}">${escapeHtml(typeLabel)}</span>
          ${renderProductStatusBadge(product.status)}
        </div>
        <h3 class="product-card-title">
          <a href="#/product/${escapeHtml(product.id)}">${escapeHtml(product.name)}</a>
        </h3>
        <p class="product-card-variant">
          <strong>${escapeHtml(product.variantAttribute || 'Variante')}:</strong> ${escapeHtml(product.variantValue || 'Estándar')}
        </p>
        <div class="product-card-footer">
          <span class="product-card-price">${escapeHtml(formattedPrice)}</span>
          <div class="product-card-actions">
            <a href="#/product/${escapeHtml(product.id)}" class="btn btn-outline btn-sm">Detalle</a>
            ${
              showAddToCart && product.status === 'PUBLISHED'
                ? `<button type="button" class="btn btn-primary btn-sm js-add-to-cart"
                    data-product-id="${escapeHtml(product.id)}"
                    data-product-name="${escapeHtml(product.name)}"
                    data-product-price="${escapeHtml(product.price)}"
                    data-product-currency="${escapeHtml(product.currency)}">
                    + Carrito
                  </button>`
                : ''
            }
          </div>
        </div>
      </div>
    </article>
  `;
}

export function renderPagination({ page = 0, totalPages = 1, totalElements = 0 }, prefixId = 'catalog') {
  if (totalPages <= 1) {
    return `<div class="pagination-summary">Mostrando ${totalElements} resultado(s)</div>`;
  }
  const hasPrev = page > 0;
  const hasNext = page + 1 < totalPages;

  return `
    <nav class="pagination-bar" aria-label="Paginación de resultados">
      <span class="pagination-summary">
        Página <strong>${page + 1}</strong> de <strong>${totalPages}</strong> (${totalElements} registros)
      </span>
      <div class="pagination-controls">
        <button type="button" class="btn btn-secondary btn-sm" id="${escapeHtml(prefixId)}-prev-page" ${hasPrev ? '' : 'disabled'}>
          &larr; Anterior
        </button>
        <button type="button" class="btn btn-secondary btn-sm" id="${escapeHtml(prefixId)}-next-page" ${hasNext ? '' : 'disabled'}>
          Siguiente &rarr;
        </button>
      </div>
    </nav>
  `;
}
