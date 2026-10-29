/**
 * Punto de entrada principal de la SPA NexusMarket.
 * Conecta el estado de sesión, la barra de navegación reactiva y el enrutador SPA.
 */

import { sessionStore } from './state/sessionStore.js';
import { authApi, buyerApi } from './api/marketplaceApi.js';
import { ROLE_LABELS } from './config/env.js';
import { escapeHtml, showToast } from './components/uiComponents.js';
import { SpaRouter } from './router/router.js';
import {
  renderHomeView,
  renderCatalogView,
  renderProductDetailView,
  renderLoginView,
  renderRegisterView,
  renderAboutView,
  renderNotFoundView
} from './views/publicViews.js';
import {
  renderCartView,
  renderCheckoutView,
  renderBuyerOrdersView,
  renderBuyerProfileView
} from './views/buyerViews.js';
import { renderSellerDashboardView } from './views/sellerViews.js';
import {
  renderLogisticsDashboardView,
  renderSupervisorDashboardView,
  renderAdminDashboardView
} from './views/operationsViews.js';

export const APP_ROUTES = [
  { path: '/', render: renderHomeView, requiresAuth: false },
  { path: '/catalog', render: renderCatalogView, requiresAuth: false },
  { path: '/product/:id', render: renderProductDetailView, requiresAuth: false },
  { path: '/login', render: renderLoginView, requiresAuth: false },
  { path: '/register', render: renderRegisterView, requiresAuth: false },
  { path: '/about', render: renderAboutView, requiresAuth: false },
  { path: '/cart', render: renderCartView, requiresAuth: true, roles: ['BUYER'] },
  { path: '/checkout', render: renderCheckoutView, requiresAuth: true, roles: ['BUYER'] },
  { path: '/orders', render: renderBuyerOrdersView, requiresAuth: true, roles: ['BUYER'] },
  { path: '/profile', render: renderBuyerProfileView, requiresAuth: true, roles: ['BUYER'] },
  { path: '/seller', render: renderSellerDashboardView, requiresAuth: true, roles: ['SELLER'] },
  { path: '/logistics', render: renderLogisticsDashboardView, requiresAuth: true, roles: ['LOGISTICS_OPERATOR'] },
  { path: '/supervisor', render: renderSupervisorDashboardView, requiresAuth: true, roles: ['SUPERVISOR'] },
  { path: '/admin', render: renderAdminDashboardView, requiresAuth: true, roles: ['ADMINISTRATOR'] },
  { path: '/404', render: renderNotFoundView, requiresAuth: false }
];

function renderHeaderNav(session, cartCount) {
  const navLinksEl = document.getElementById('main-nav-links');
  const authActionsEl = document.getElementById('nav-auth-actions');
  if (!navLinksEl || !authActionsEl) return;

  const links = [
    `<a href="#/" class="nav-link">Inicio</a>`,
    `<a href="#/catalog" class="nav-link">Catálogo</a>`,
    `<a href="#/about" class="nav-link">Guía</a>`
  ];

  if (session) {
    if (session.role === 'BUYER' || session.role === 'ADMINISTRATOR') {
      links.push(`
        <a href="#/cart" class="nav-link nav-cart-link" aria-label="Carrito de compras (${cartCount} unidades)">
          🛒 Carrito <span class="cart-badge">${cartCount}</span>
        </a>
      `);
      links.push(`<a href="#/orders" class="nav-link">Mis Pedidos</a>`);
      links.push(`<a href="#/profile" class="nav-link">Mi Perfil</a>`);
    }
    if (session.role === 'SELLER' || session.role === 'ADMINISTRATOR') {
      links.push(`<a href="#/seller" class="nav-link">Panel Vendedor</a>`);
    }
    if (session.role === 'LOGISTICS_OPERATOR' || session.role === 'ADMINISTRATOR') {
      links.push(`<a href="#/logistics" class="nav-link">Panel Logística</a>`);
    }
    if (session.role === 'SUPERVISOR' || session.role === 'ADMINISTRATOR') {
      links.push(`<a href="#/supervisor" class="nav-link">Panel Supervisor</a>`);
    }
    if (session.role === 'ADMINISTRATOR') {
      links.push(`<a href="#/admin" class="nav-link">Administración</a>`);
    }
  }

  navLinksEl.innerHTML = links.join('');

  if (!session) {
    authActionsEl.innerHTML = `
      <a href="#/login" class="btn btn-outline-light btn-sm">Iniciar Sesión</a>
      <a href="#/register" class="btn btn-primary btn-sm">Registrarse</a>
    `;
  } else {
    const roleLabel = ROLE_LABELS[session.role] || session.role;
    authActionsEl.innerHTML = `
      <span class="user-pill" title="${escapeHtml(session.email)}">
        <strong>${escapeHtml(roleLabel)}</strong> &bull; ${escapeHtml(session.email)}
      </span>
      <button type="button" id="logout-btn" class="btn btn-outline-light btn-sm">Cerrar Sesión</button>
    `;

    document.getElementById('logout-btn')?.addEventListener('click', async () => {
      try {
        await authApi.logout();
      } catch {
        // Ignorar fallo de red al cerrar sesión
      }
      sessionStore.clearSession();
      showToast('Sesión cerrada correctamente.', 'info');
      window.location.hash = '#/login';
    });
  }
}

export async function bootstrapApp() {
  if (typeof window === 'undefined' || typeof document === 'undefined') return;

  const mainContainer = document.getElementById('app-main');
  const router = new SpaRouter(APP_ROUTES, (reason) => showToast(reason, 'info'));

  sessionStore.subscribe((session, cartCount) => {
    renderHeaderNav(session, cartCount);
  });

  // Restaurar sesión previa si sigue vigente al recargar la página
  const initialSession = sessionStore.getSession();
  renderHeaderNav(initialSession, sessionStore.getCartCount());

  if (initialSession && initialSession.role === 'BUYER') {
    buyerApi
      .getOrCreateCart(initialSession.userId)
      .then((cart) => {
        sessionStore.setActiveCartId(cart.cartId);
        const count = (cart.items || []).reduce((acc, i) => acc + Number(i.quantity || 0), 0);
        sessionStore.setCartCount(count);
      })
      .catch(() => {});
  }

  window.addEventListener('hashchange', () => {
    router.navigateToCurrentHash(mainContainer);
  });

  await router.navigateToCurrentHash(mainContainer);
}

if (typeof window !== 'undefined' && typeof document !== 'undefined') {
  window.addEventListener('DOMContentLoaded', () => {
    bootstrapApp();
  });
}
