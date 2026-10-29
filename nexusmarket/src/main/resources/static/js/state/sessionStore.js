/**
 * Almacén seguro de sesión y estado reactivo de autenticación / carrito.
 * Utiliza sessionStorage (con respaldo en memoria para entornos de pruebas Node.js)
 * evitando persistir tokens JWT de forma indefinida en localStorage.
 */

const STORAGE_KEY = 'nexusmarket.auth.session';
const CART_CACHE_KEY = 'nexusmarket.buyer.cartId';

const memoryStorage = new Map();

function getStorage() {
  if (typeof window !== 'undefined' && window.sessionStorage) {
    return window.sessionStorage;
  }
  return {
    getItem: (k) => memoryStorage.get(k) ?? null,
    setItem: (k, v) => memoryStorage.set(k, String(v)),
    removeItem: (k) => memoryStorage.delete(k)
  };
}

export function decodeJwtPayload(token) {
  if (!token || typeof token !== 'string') return null;
  const parts = token.split('.');
  if (parts.length !== 3) return null;
  try {
    const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
    const padded = base64 + '='.repeat((4 - (base64.length % 4)) % 4);
    const json = typeof atob === 'function'
      ? atob(padded)
      : Buffer.from(padded, 'base64').toString('utf8');
    return JSON.parse(json);
  } catch {
    return null;
  }
}

export function isJwtExpired(token) {
  const payload = decodeJwtPayload(token);
  if (!payload || typeof payload.exp !== 'number') {
    return false;
  }
  const nowSeconds = Math.floor(Date.now() / 1000);
  return nowSeconds > payload.exp;
}

class SessionStore {
  constructor() {
    this.listeners = new Set();
    this.cartCount = 0;
  }

  getSession() {
    const raw = getStorage().getItem(STORAGE_KEY);
    if (!raw) return null;
    try {
      const parsed = JSON.parse(raw);
      if (!parsed || !parsed.token || isJwtExpired(parsed.token)) {
        this.clearSession();
        return null;
      }
      return parsed;
    } catch {
      this.clearSession();
      return null;
    }
  }

  setSession(loginResponse) {
    if (!loginResponse || !loginResponse.token) {
      throw new Error('Respuesta de autenticación inválida: falta token JWT');
    }
    const session = {
      token: loginResponse.token,
      tokenType: loginResponse.tokenType || 'Bearer',
      userId: loginResponse.userId,
      email: loginResponse.email,
      role: loginResponse.role,
      authenticatedAt: new Date().toISOString()
    };
    getStorage().setItem(STORAGE_KEY, JSON.stringify(session));
    this.notify();
    return session;
  }

  clearSession() {
    getStorage().removeItem(STORAGE_KEY);
    getStorage().removeItem(CART_CACHE_KEY);
    this.cartCount = 0;
    this.notify();
  }

  isAuthenticated() {
    return this.getSession() !== null;
  }

  getToken() {
    return this.getSession()?.token || null;
  }

  getUserId() {
    return this.getSession()?.userId || null;
  }

  getRole() {
    return this.getSession()?.role || null;
  }

  hasRole(...roles) {
    const currentRole = this.getRole();
    if (!currentRole) return false;
    return roles.includes(currentRole);
  }

  getActiveCartId() {
    return getStorage().getItem(CART_CACHE_KEY);
  }

  setActiveCartId(cartId) {
    if (cartId) {
      getStorage().setItem(CART_CACHE_KEY, String(cartId));
    } else {
      getStorage().removeItem(CART_CACHE_KEY);
    }
  }

  setCartCount(count) {
    this.cartCount = Math.max(0, Number(count || 0));
    this.notify();
  }

  getCartCount() {
    return this.cartCount;
  }

  subscribe(listener) {
    this.listeners.add(listener);
    return () => this.listeners.delete(listener);
  }

  notify() {
    const current = this.getSession();
    for (const listener of this.listeners) {
      try {
        listener(current, this.cartCount);
      } catch {
        // Ignore listener error
      }
    }
  }
}

export const sessionStore = new SessionStore();
