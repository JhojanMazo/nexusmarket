/**
 * Configuración centralizada de entorno, etiquetas en español, formatos de moneda
 * y generador determinista de ilustraciones SVG para productos de NexusMarket.
 */

export const ENV = {
  API_BASE_URL: (typeof window !== 'undefined' && window.NEXUSMARKET_API_BASE_URL) || '/api',
  REQUEST_TIMEOUT_MS: 12000,
  DEFAULT_PAGE_SIZE: 6
};

export const ROLE_LABELS = {
  BUYER: 'Comprador',
  SELLER: 'Vendedor',
  LOGISTICS_OPERATOR: 'Operador Logístico',
  SUPERVISOR: 'Supervisor',
  ADMINISTRATOR: 'Administrador'
};

export const ROLE_HOME_ROUTES = {
  BUYER: '#/catalog',
  SELLER: '#/seller',
  LOGISTICS_OPERATOR: '#/logistics',
  SUPERVISOR: '#/supervisor',
  ADMINISTRATOR: '#/admin'
};

export const ORDER_STATUS_LABELS = {
  CART: 'En Carrito',
  PENDING_PAYMENT: 'Pendiente de Pago',
  PAID: 'Pagado',
  SHIPPED: 'Despachado / En Tránsito',
  DELIVERED_COMPLETED: 'Entregado y Completado',
  CANCELLED: 'Cancelado'
};

export const PRODUCT_STATUS_LABELS = {
  PUBLISHED: 'Publicado',
  DRAFT: 'Borrador',
  SUSPENDED: 'Suspendido',
  RETIRED: 'Retirado'
};

export const PRODUCT_TYPE_LABELS = {
  PHYSICAL: 'Producto Físico',
  DIGITAL: 'Producto Digital'
};

export const USER_STATUS_LABELS = {
  ACTIVE: 'Activo',
  BLOCKED: 'Bloqueado',
  INACTIVE: 'Inactivo'
};

export function formatMoney(amount, currency = 'COP') {
  const num = Number(amount ?? 0);
  if (Number.isNaN(num)) {
    return `${amount} ${currency}`;
  }
  try {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: currency || 'COP',
      maximumFractionDigits: currency === 'COP' ? 0 : 2
    }).format(num);
  } catch {
    return `${num.toLocaleString('es-CO')} ${currency}`;
  }
}

/**
 * Genera una imagen SVG en Data URI determinista a partir del nombre y tipo del producto,
 * garantizando visualización nítida sin depender de servidores de imágenes externos.
 */
export function buildProductIllustrationSvg(product) {
  const name = String(product?.name || 'Producto NexusMarket');
  const type = String(product?.type || 'PHYSICAL');
  const variant = product?.variantAttribute
    ? `${product.variantAttribute}: ${product.variantValue || ''}`
    : type;

  const isDigital = type === 'DIGITAL';
  const bgStart = isDigital ? '#1e1b4b' : '#0f172a';
  const bgEnd = isDigital ? '#4338ca' : '#0369a1';
  const badgeColor = isDigital ? '#818cf8' : '#38bdf8';
  const iconSymbol = isDigital ? '☁️ LICENCIA DIGITAL' : '📦 ENVÍO GARANTIZADO';
  const safeTitle = name.replace(/[<>&"']/g, '').slice(0, 26);
  const safeVariant = String(variant).replace(/[<>&"']/g, '').slice(0, 32);

  const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 480 300" width="480" height="300" role="img" aria-label="${safeTitle}">
    <defs>
      <linearGradient id="bg" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" stop-color="${bgStart}" />
        <stop offset="100%" stop-color="${bgEnd}" />
      </linearGradient>
    </defs>
    <rect width="480" height="300" rx="16" fill="url(#bg)" />
    <circle cx="410" cy="60" r="85" fill="${badgeColor}" fill-opacity="0.14" />
    <circle cx="70" cy="250" r="95" fill="${badgeColor}" fill-opacity="0.1" />
    <rect x="32" y="32" width="180" height="30" rx="15" fill="${badgeColor}" fill-opacity="0.22" />
    <text x="46" y="52" fill="#f8fafc" font-family="system-ui, -apple-system, sans-serif" font-size="11" font-weight="700">${iconSymbol}</text>
    <text x="32" y="155" fill="#ffffff" font-family="system-ui, -apple-system, sans-serif" font-size="23" font-weight="800">${safeTitle}</text>
    <text x="32" y="190" fill="#cbd5e1" font-family="system-ui, -apple-system, sans-serif" font-size="14" font-weight="500">${safeVariant}</text>
    <text x="32" y="258" fill="#94a3b8" font-family="system-ui, -apple-system, sans-serif" font-size="12" font-weight="600">NEXUSMARKET VERIFIED CATALOG</text>
  </svg>`;

  return `data:image/svg+xml;utf8,${encodeURIComponent(svg)}`;
}
