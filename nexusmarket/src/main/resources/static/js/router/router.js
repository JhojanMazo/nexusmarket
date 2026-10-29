/**
 * Enrutador SPA con soporte de parámetros dinámicos (:id), guardas de autenticación
 * y control de acceso basado en roles (RBAC).
 */

import { sessionStore } from '../state/sessionStore.js';
import { ROLE_HOME_ROUTES } from '../config/env.js';

export function matchRoutePattern(pattern, path) {
  const cleanPattern = pattern.replace(/^#/, '') || '/';
  const cleanPath = (path.replace(/^#/, '').split('?')[0]) || '/';

  const patternParts = cleanPattern.split('/').filter(Boolean);
  const pathParts = cleanPath.split('/').filter(Boolean);

  if (patternParts.length !== pathParts.length) {
    return null;
  }

  const params = {};
  for (let i = 0; i < patternParts.length; i++) {
    const pPart = patternParts[i];
    const uPart = pathParts[i];
    if (pPart.startsWith(':')) {
      params[pPart.slice(1)] = decodeURIComponent(uPart);
    } else if (pPart !== uPart) {
      return null;
    }
  }
  return params;
}

export function evaluateRouteAccess(routeConfig, session) {
  if (!routeConfig) {
    return { allowed: false, redirectTo: '#/404', reason: 'Ruta no encontrada' };
  }
  if (routeConfig.requiresAuth && !session) {
    return {
      allowed: false,
      redirectTo: '#/login',
      reason: 'Inicie sesión para acceder a esta sección'
    };
  }
  if (Array.isArray(routeConfig.roles) && routeConfig.roles.length > 0) {
    if (!session || (!routeConfig.roles.includes(session.role) && session.role !== 'ADMINISTRATOR')) {
      const fallback = session ? (ROLE_HOME_ROUTES[session.role] || '#/') : '#/login';
      return {
        allowed: false,
        redirectTo: fallback,
        reason: 'No cuenta con permisos para acceder a este panel'
      };
    }
  }
  return { allowed: true, redirectTo: null, reason: null };
}

export class SpaRouter {
  constructor(routes, onUnauthorizedRedirect) {
    this.routes = routes;
    this.onUnauthorizedRedirect = onUnauthorizedRedirect;
  }

  resolve(hash) {
    const rawHash = hash || '#/';
    const [pathPart, queryString = ''] = rawHash.replace(/^#/, '').split('?');
    const normalizedPath = pathPart.startsWith('/') ? pathPart : `/${pathPart}`;
    const query = Object.fromEntries(new URLSearchParams(queryString).entries());

    for (const route of this.routes) {
      const params = matchRoutePattern(route.path, normalizedPath);
      if (params !== null) {
        return { route, params, query, path: normalizedPath };
      }
    }

    const notFoundRoute = this.routes.find((r) => r.path === '/404');
    return { route: notFoundRoute, params: {}, query, path: normalizedPath };
  }

  async navigateToCurrentHash(container) {
    if (typeof window === 'undefined' || !container) return;
    const currentHash = window.location.hash || '#/';
    const { route, params, query } = this.resolve(currentHash);
    const session = sessionStore.getSession();

    const access = evaluateRouteAccess(route, session);
    if (!access.allowed) {
      if (typeof this.onUnauthorizedRedirect === 'function' && access.reason) {
        this.onUnauthorizedRedirect(access.reason);
      }
      window.location.hash = access.redirectTo;
      return;
    }

    await route.render(container, { params, query, session });
    container.focus({ preventScroll: true });
  }
}
