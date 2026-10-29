import test from 'node:test';
import assert from 'node:assert/strict';

import { formatMoney, buildProductIllustrationSvg } from '../public/js/config/env.js';
import { sessionStore, decodeJwtPayload, isJwtExpired } from '../public/js/state/sessionStore.js';
import { apiRequest, ApiError } from '../public/js/api/httpClient.js';
import { authApi } from '../public/js/api/marketplaceApi.js';
import { matchRoutePattern, evaluateRouteAccess } from '../public/js/router/router.js';
import { escapeHtml, renderProductCard, renderPagination } from '../public/js/components/uiComponents.js';

function createSampleJwt(payloadObj) {
  const header = Buffer.from(JSON.stringify({ alg: 'HS256', typ: 'JWT' })).toString('base64url');
  const payload = Buffer.from(JSON.stringify(payloadObj)).toString('base64url');
  return `${header}.${payload}.signature`;
}

test('sessionStore gestiona ciclo de vida de sesión JWT, expiración y roles', () => {
  sessionStore.clearSession();
  assert.equal(sessionStore.isAuthenticated(), false);

  const futureExp = Math.floor(Date.now() / 1000) + 3600;
  const validToken = createSampleJwt({
    sub: '11111111-2222-3333-4444-555555555555',
    email: 'buyer@nexusmarket.com',
    role: 'BUYER',
    exp: futureExp
  });

  const decoded = decodeJwtPayload(validToken);
  assert.equal(decoded.email, 'buyer@nexusmarket.com');
  assert.equal(isJwtExpired(validToken), false);

  sessionStore.setSession({
    token: validToken,
    tokenType: 'Bearer',
    userId: '11111111-2222-3333-4444-555555555555',
    email: 'buyer@nexusmarket.com',
    role: 'BUYER'
  });

  assert.equal(sessionStore.isAuthenticated(), true);
  assert.equal(sessionStore.getRole(), 'BUYER');
  assert.equal(sessionStore.hasRole('BUYER'), true);
  assert.equal(sessionStore.hasRole('ADMINISTRATOR'), false);

  // Token expirado limpia la sesión automáticamente
  const expiredToken = createSampleJwt({
    sub: '11111111-2222-3333-4444-555555555555',
    exp: Math.floor(Date.now() / 1000) - 120
  });
  assert.equal(isJwtExpired(expiredToken), true);
  sessionStore.setSession({
    token: expiredToken,
    userId: '11111111-2222-3333-4444-555555555555',
    email: 'buyer@nexusmarket.com',
    role: 'BUYER'
  });
  assert.equal(sessionStore.getSession(), null);
});

test('router evalúa parámetros dinámicos y guardas de autorización RBAC', () => {
  const match = matchRoutePattern('/product/:id', '/product/abc-123');
  assert.deepEqual(match, { id: 'abc-123' });
  assert.equal(matchRoutePattern('/product/:id', '/catalog'), null);

  const buyerRoute = { path: '/cart', requiresAuth: true, roles: ['BUYER'] };
  const adminRoute = { path: '/admin', requiresAuth: true, roles: ['ADMINISTRATOR'] };

  // Usuario no autenticado -> redirigido a #/login
  const unauthResult = evaluateRouteAccess(buyerRoute, null);
  assert.equal(unauthResult.allowed, false);
  assert.equal(unauthResult.redirectTo, '#/login');

  // Comprador autenticado -> permitido en /cart, denegado en /admin
  const buyerSession = { role: 'BUYER', userId: 'u1' };
  assert.equal(evaluateRouteAccess(buyerRoute, buyerSession).allowed, true);
  const deniedAdmin = evaluateRouteAccess(adminRoute, buyerSession);
  assert.equal(deniedAdmin.allowed, false);
  assert.equal(deniedAdmin.redirectTo, '#/catalog');

  // Administrador autenticado -> permitido en cualquier ruta
  const adminSession = { role: 'ADMINISTRATOR', userId: 'admin1' };
  assert.equal(evaluateRouteAccess(adminRoute, adminSession).allowed, true);
  assert.equal(evaluateRouteAccess(buyerRoute, adminSession).allowed, true);
});

test('httpClient inyecta Bearer token, construye parámetros de consulta y maneja errores HTTP', async () => {
  const futureExp = Math.floor(Date.now() / 1000) + 3600;
  sessionStore.setSession({
    token: createSampleJwt({ sub: 'u1', exp: futureExp }),
    userId: 'u1',
    email: 'buyer@nexusmarket.com',
    role: 'BUYER'
  });

  let capturedUrl = '';
  let capturedHeaders = {};

  const mockFetchOk = async (url, options) => {
    capturedUrl = url;
    capturedHeaders = options.headers;
    return {
      ok: true,
      status: 200,
      text: async () => JSON.stringify({ content: [{ id: 'p1', name: 'MacBook Pro' }], totalPages: 1, totalElements: 1 })
    };
  };

  const catalogResp = await authApi.getCatalog(
    { page: 0, size: 6, search: 'MacBook', type: 'PHYSICAL', sortBy: 'price', direction: 'DESC' },
    { fetchImpl: mockFetchOk }
  );

  assert.equal(catalogResp.content.length, 1);
  assert.ok(capturedUrl.includes('search=MacBook'));
  assert.ok(capturedUrl.includes('type=PHYSICAL'));
  assert.ok(capturedUrl.includes('sortBy=price'));
  assert.ok(capturedHeaders.Authorization.startsWith('Bearer '));

  // Error 401 limpia sesión activa y lanza ApiError
  const mockFetch401 = async () => ({
    ok: false,
    status: 401,
    text: async () => JSON.stringify({ message: 'Token expirado' })
  });

  await assert.rejects(
    () => apiRequest('/buyers/orders', { fetchImpl: mockFetch401 }),
    (err) => err instanceof ApiError && err.status === 401 && err.message === 'Token expirado'
  );
  assert.equal(sessionStore.isAuthenticated(), false);
});

test('uiComponents previene XSS y renderiza tarjetas de producto y paginación accesible', () => {
  const malicious = '<script>alert("xss")</script>';
  const escaped = escapeHtml(malicious);
  assert.equal(escaped, '&lt;script&gt;alert(&quot;xss&quot;)&lt;/script&gt;');

  const cardHtml = renderProductCard({
    id: 'prod-1',
    name: 'Monitor 4K <script>',
    variantAttribute: 'Panel',
    variantValue: 'OLED',
    price: 4500000,
    currency: 'COP',
    status: 'PUBLISHED',
    type: 'PHYSICAL'
  });

  assert.ok(cardHtml.includes('Monitor 4K &lt;script&gt;'));
  assert.ok(!cardHtml.includes('<script>'));
  assert.ok(cardHtml.includes('js-add-to-cart'));

  const paginationHtml = renderPagination({ page: 0, totalPages: 3, totalElements: 18 }, 'catalog');
  assert.ok(paginationHtml.includes('Página <strong>1</strong> de <strong>3</strong>'));

  const svgUri = buildProductIllustrationSvg({ name: 'Laptop', type: 'PHYSICAL' });
  assert.ok(svgUri.startsWith('data:image/svg+xml;utf8,'));
  assert.ok(formatMoney(1500000, 'COP').length > 0);
});
