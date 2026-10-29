/**
 * Vistas públicas del Marketplace NexusMarket:
 * - Página de Inicio (#/)
 * - Catálogo con búsqueda, filtros por tipo y precio, ordenamiento y paginación (#/catalog)
 * - Detalle de Producto con consulta de stock real (#/product/:id)
 * - Inicio de Sesión (#/login)
 * - Registro de Comprador (#/register)
 * - Información del Marketplace (#/about)
 * - Página 404 (#/404)
 */

import { authApi, buyerApi } from '../api/marketplaceApi.js';
import { sessionStore } from '../state/sessionStore.js';
import {
  formatMoney,
  buildProductIllustrationSvg,
  PRODUCT_TYPE_LABELS,
  ROLE_HOME_ROUTES,
  ROLE_LABELS
} from '../config/env.js';
import {
  escapeHtml,
  showToast,
  renderLoadingState,
  renderErrorState,
  renderEmptyState,
  renderProductCard,
  renderPagination,
  renderProductStatusBadge
} from '../components/uiComponents.js';

export async function addProductToActiveBuyerCart(productId, quantity = 1, unitPrice = null, currency = 'COP') {
  const session = sessionStore.getSession();
  if (!session) {
    showToast('Inicie sesión con su cuenta de comprador para agregar productos al carrito.', 'info');
    window.location.hash = '#/login';
    return;
  }
  if (session.role !== 'BUYER' && session.role !== 'ADMINISTRATOR') {
    showToast('Solo las cuentas con rol Comprador pueden gestionar un carrito de compras.', 'error');
    return;
  }

  try {
    let cartId = sessionStore.getActiveCartId();
    if (!cartId) {
      const cart = await buyerApi.getOrCreateCart(session.userId);
      cartId = cart.cartId;
      sessionStore.setActiveCartId(cartId);
    }

    const updatedCart = await buyerApi.addCartItem(cartId, {
      productId,
      quantity: Number(quantity) || 1,
      unitPrice: unitPrice !== null ? Number(unitPrice) : undefined,
      currency
    });

    const totalItems = (updatedCart.items || []).reduce((acc, i) => acc + Number(i.quantity || 0), 0);
    sessionStore.setCartCount(totalItems);
    showToast('Producto agregado al carrito exitosamente.', 'success');
  } catch (err) {
    // Si el carrito anterior ya fue consumido en una orden, crear uno nuevo automáticamente
    if (err.status === 404) {
      sessionStore.setActiveCartId(null);
      const freshCart = await buyerApi.getOrCreateCart(session.userId);
      sessionStore.setActiveCartId(freshCart.cartId);
      const updated = await buyerApi.addCartItem(freshCart.cartId, {
        productId,
        quantity: Number(quantity) || 1,
        unitPrice: unitPrice !== null ? Number(unitPrice) : undefined,
        currency
      });
      const totalItems = (updated.items || []).reduce((acc, i) => acc + Number(i.quantity || 0), 0);
      sessionStore.setCartCount(totalItems);
      showToast('Producto agregado al carrito exitosamente.', 'success');
      return;
    }
    showToast(err.message || 'No se pudo agregar el producto al carrito.', 'error');
  }
}

export function bindAddToCartButtons(rootElement) {
  rootElement.querySelectorAll('.js-add-to-cart').forEach((btn) => {
    btn.addEventListener('click', async () => {
      btn.disabled = true;
      await addProductToActiveBuyerCart(
        btn.dataset.productId,
        1,
        btn.dataset.productPrice,
        btn.dataset.productCurrency
      );
      btn.disabled = false;
    });
  });
}

export async function renderHomeView(container) {
  const session = sessionStore.getSession();
  container.innerHTML = `
    <section class="hero-banner">
      <div class="hero-content">
        <span class="hero-kicker">PLATAFORMA COMERCIAL MULTI-ACTOR</span>
        <h1>Bienvenido a NexusMarket</h1>
        <p>
          Explore tecnología empresarial, hardware certificado y licencias digitales con reserva de inventario en tiempo real,
          facturación automática y trazabilidad logística de extremo a extremo.
        </p>
        <div class="hero-actions">
          <a href="#/catalog" class="btn btn-primary btn-lg">Explorar Catálogo</a>
          ${
            !session
              ? `<a href="#/register" class="btn btn-outline-light btn-lg">Crear Cuenta de Comprador</a>`
              : `<a href="${ROLE_HOME_ROUTES[session.role] || '#/catalog'}" class="btn btn-outline-light btn-lg">
                  Ir a mi Panel (${escapeHtml(ROLE_LABELS[session.role] || session.role)})
                </a>`
          }
        </div>
      </div>
      <div class="hero-highlights">
        <div class="highlight-card">
          <strong>Productos Físicos y Digitales</strong>
          <span>Entrega inmediata en licencias digitales y despacho coordinado desde bodegas certificadas.</span>
        </div>
        <div class="highlight-card">
          <strong>Stock Protegido en Tiempo Real</strong>
          <span>Control de concurrencia optimista (@Version) y liberación automática de reservas expiradas.</span>
        </div>
        <div class="highlight-card">
          <strong>Seguridad y Auditoría Continua</strong>
          <span>Autenticación criptográfica JWT HMAC-SHA256 y bitácora documental en MongoDB.</span>
        </div>
      </div>
    </section>

    <section class="section-block" aria-labelledby="home-categories-heading">
      <div class="section-header">
        <h2 id="home-categories-heading">Categorías por Tipo de Producto</h2>
        <a href="#/catalog" class="link-arrow">Ver todo el catálogo &rarr;</a>
      </div>
      <div class="category-grid">
        <a href="#/catalog?type=PHYSICAL" class="category-card">
          <span class="category-icon" aria-hidden="true">📦</span>
          <div>
            <h3>Hardware y Equipos Físicos</h3>
            <p>Estaciones de trabajo, monitores y periféricos con despacho desde bodegas verificadas.</p>
          </div>
        </a>
        <a href="#/catalog?type=DIGITAL" class="category-card">
          <span class="category-icon" aria-hidden="true">☁️</span>
          <div>
            <h3>Software y Licencias Digitales</h3>
            <p>Suscripciones empresariales y licencias cloud con activación automática tras el pago.</p>
          </div>
        </a>
      </div>
    </section>

    <section class="section-block" aria-labelledby="home-featured-heading">
      <div class="section-header">
        <h2 id="home-featured-heading">Productos Destacados Disponibles</h2>
        <a href="#/catalog" class="link-arrow">Buscar y filtrar &rarr;</a>
      </div>
      <div id="home-featured-products">
        ${renderLoadingState('Consultando productos publicados en el catálogo...')}
      </div>
    </section>
  `;

  const featuredBox = container.querySelector('#home-featured-products');
  try {
    const pageData = await authApi.getCatalog({ page: 0, size: 6 });
    const items = pageData?.content || [];
    if (items.length === 0) {
      featuredBox.innerHTML = renderEmptyState(
        'Catálogo sin productos publicados',
        'Aún no se han publicado productos en la plataforma.'
      );
      return;
    }
    featuredBox.innerHTML = `
      <div class="product-grid">
        ${items.map((p) => renderProductCard(p)).join('')}
      </div>
    `;
    bindAddToCartButtons(featuredBox);
  } catch (err) {
    featuredBox.innerHTML = renderErrorState(err.message, 'home-retry-btn');
    featuredBox.querySelector('#home-retry-btn')?.addEventListener('click', () => renderHomeView(container));
  }
}

export async function renderCatalogView(container, { query = {} } = {}) {
  const state = {
    page: Number(query.page || 0),
    size: Number(query.size || 6),
    search: query.search || '',
    type: query.type || '',
    minPrice: query.minPrice || '',
    maxPrice: query.maxPrice || '',
    sortBy: query.sortBy || 'name',
    direction: query.direction || 'ASC'
  };

  container.innerHTML = `
    <section class="page-header">
      <div>
        <h1>Catálogo de Productos</h1>
        <p class="text-muted">Explore productos reales publicados en NexusMarket con precios y disponibilidad verificados.</p>
      </div>
    </section>

    <form id="catalog-filter-form" class="filter-panel" aria-label="Filtros y búsqueda de productos">
      <div class="filter-row">
        <div class="form-group filter-search">
          <label for="filter-search-input">Buscar por nombre o variante</label>
          <input type="search" id="filter-search-input" name="search" class="form-control"
            placeholder="Ej. MacBook, Monitor, Licencia, SSD..." value="${escapeHtml(state.search)}" />
        </div>
        <div class="form-group">
          <label for="filter-type-select">Categoría / Tipo</label>
          <select id="filter-type-select" name="type" class="form-control">
            <option value="">Todos los tipos</option>
            <option value="PHYSICAL" ${state.type === 'PHYSICAL' ? 'selected' : ''}>Físico (Envío a domicilio)</option>
            <option value="DIGITAL" ${state.type === 'DIGITAL' ? 'selected' : ''}>Digital (Entrega inmediata)</option>
          </select>
        </div>
        <div class="form-group">
          <label for="filter-min-price">Precio Mínimo</label>
          <input type="number" id="filter-min-price" name="minPrice" class="form-control" min="0" step="1000"
            placeholder="0" value="${escapeHtml(state.minPrice)}" />
        </div>
        <div class="form-group">
          <label for="filter-max-price">Precio Máximo</label>
          <input type="number" id="filter-max-price" name="maxPrice" class="form-control" min="0" step="1000"
            placeholder="Sin límite" value="${escapeHtml(state.maxPrice)}" />
        </div>
        <div class="form-group">
          <label for="filter-sort-select">Ordenar por</label>
          <select id="filter-sort-select" name="sort" class="form-control">
            <option value="name:ASC" ${state.sortBy === 'name' && state.direction === 'ASC' ? 'selected' : ''}>Nombre (A - Z)</option>
            <option value="name:DESC" ${state.sortBy === 'name' && state.direction === 'DESC' ? 'selected' : ''}>Nombre (Z - A)</option>
            <option value="price:ASC" ${state.sortBy === 'price' && state.direction === 'ASC' ? 'selected' : ''}>Precio (Menor a Mayor)</option>
            <option value="price:DESC" ${state.sortBy === 'price' && state.direction === 'DESC' ? 'selected' : ''}>Precio (Mayor a Menor)</option>
          </select>
        </div>
      </div>
      <div class="filter-actions">
        <button type="submit" class="btn btn-primary">Aplicar Filtros</button>
        <button type="button" id="filter-reset-btn" class="btn btn-secondary">Limpiar</button>
      </div>
    </form>

    <div id="catalog-results-container">
      ${renderLoadingState('Buscando productos en el catálogo...')}
    </div>
  `;

  const resultsBox = container.querySelector('#catalog-results-container');
  const form = container.querySelector('#catalog-filter-form');

  async function loadCatalogPage() {
    resultsBox.innerHTML = renderLoadingState('Cargando catálogo...');
    try {
      const response = await authApi.getCatalog(state);
      const items = response?.content || [];
      if (items.length === 0) {
        resultsBox.innerHTML = renderEmptyState(
          'No se encontraron productos',
          'No hay productos publicados que coincidan con los filtros seleccionados.',
          `<button type="button" id="empty-clear-filters" class="btn btn-secondary">Restablecer filtros</button>`
        );
        resultsBox.querySelector('#empty-clear-filters')?.addEventListener('click', () => {
          form.reset();
          state.page = 0;
          state.search = '';
          state.type = '';
          state.minPrice = '';
          state.maxPrice = '';
          state.sortBy = 'name';
          state.direction = 'ASC';
          loadCatalogPage();
        });
        return;
      }

      resultsBox.innerHTML = `
        <div class="product-grid">
          ${items.map((p) => renderProductCard(p)).join('')}
        </div>
        ${renderPagination(response, 'catalog')}
      `;

      bindAddToCartButtons(resultsBox);

      resultsBox.querySelector('#catalog-prev-page')?.addEventListener('click', () => {
        if (state.page > 0) {
          state.page -= 1;
          loadCatalogPage();
        }
      });
      resultsBox.querySelector('#catalog-next-page')?.addEventListener('click', () => {
        if (state.page + 1 < (response.totalPages || 1)) {
          state.page += 1;
          loadCatalogPage();
        }
      });
    } catch (err) {
      resultsBox.innerHTML = renderErrorState(err.message, 'catalog-retry-btn');
      resultsBox.querySelector('#catalog-retry-btn')?.addEventListener('click', loadCatalogPage);
    }
  }

  form.addEventListener('submit', (e) => {
    e.preventDefault();
    state.page = 0;
    state.search = form.search.value.trim();
    state.type = form.type.value;
    state.minPrice = form.minPrice.value;
    state.maxPrice = form.maxPrice.value;
    const [sortBy, direction] = (form.sort.value || 'name:ASC').split(':');
    state.sortBy = sortBy;
    state.direction = direction;
    loadCatalogPage();
  });

  container.querySelector('#filter-reset-btn')?.addEventListener('click', () => {
    form.reset();
    state.page = 0;
    state.search = '';
    state.type = '';
    state.minPrice = '';
    state.maxPrice = '';
    state.sortBy = 'name';
    state.direction = 'ASC';
    loadCatalogPage();
  });

  await loadCatalogPage();
}

export async function renderProductDetailView(container, { params = {} } = {}) {
  const productId = params.id;
  if (!productId) {
    container.innerHTML = renderErrorState('Identificador de producto no especificado.');
    return;
  }

  container.innerHTML = renderLoadingState('Cargando detalle y disponibilidad del producto...');

  try {
    const [product, stockInfo] = await Promise.all([
      authApi.getProductById(productId),
      authApi.getProductStock(productId).catch(() => ({ availableQuantity: 0, inStock: true, productType: 'PHYSICAL' }))
    ]);

    const imgUrl = buildProductIllustrationSvg(product);
    const typeLabel = PRODUCT_TYPE_LABELS[product.type] || product.type;
    const formattedPrice = formatMoney(product.price, product.currency);
    const isDigital = product.type === 'DIGITAL';
    const availableUnits = Number(stockInfo?.availableQuantity ?? 0);
    const inStock = isDigital || availableUnits > 0;

    container.innerHTML = `
      <nav class="breadcrumb" aria-label="Ruta de navegación">
        <a href="#/catalog">&larr; Volver al Catálogo</a>
      </nav>

      <div class="product-detail-layout">
        <div class="product-detail-media">
          <img src="${imgUrl}" alt="${escapeHtml(product.name)}" class="product-detail-img" />
        </div>
        <div class="product-detail-info">
          <div class="product-card-tags">
            <span class="badge badge-type-${escapeHtml(String(product.type).toLowerCase())}">${escapeHtml(typeLabel)}</span>
            ${renderProductStatusBadge(product.status)}
            <span class="badge ${inStock ? 'badge-stock-ok' : 'badge-stock-out'}">
              ${isDigital ? 'Entrega Digital Instantánea' : inStock ? `En Stock (${availableUnits} disp.)` : 'Sin Stock Disponible'}
            </span>
          </div>

          <h1 class="product-detail-title">${escapeHtml(product.name)}</h1>
          <p class="product-detail-price">${escapeHtml(formattedPrice)}</p>

          <dl class="product-specs">
            <div>
              <dt>Atributo de Variante</dt>
              <dd>${escapeHtml(product.variantAttribute || 'Estándar')}</dd>
            </div>
            <div>
              <dt>Especificación</dt>
              <dd>${escapeHtml(product.variantValue || 'N/A')}</dd>
            </div>
            <div>
              <dt>Modalidad de Entrega</dt>
              <dd>${isDigital ? 'Activación automática al confirmar el pago (sin envío físico)' : 'Despacho físico desde bodega con seguimiento logístico'}</dd>
            </div>
            <div>
              <dt>ID de Catálogo</dt>
              <dd><code>${escapeHtml(product.id)}</code></dd>
            </div>
          </dl>

          <form id="detail-add-cart-form" class="detail-purchase-box">
            <div class="form-group">
              <label for="detail-quantity-input">Cantidad</label>
              <input type="number" id="detail-quantity-input" name="quantity" class="form-control"
                min="1" max="${isDigital ? 50 : Math.max(1, availableUnits)}" value="1" required ${!inStock ? 'disabled' : ''} />
            </div>
            <button type="submit" class="btn btn-primary btn-lg" ${!inStock ? 'disabled' : ''}>
              ${inStock ? 'Agregar al Carrito' : 'Producto Agotado'}
            </button>
          </form>
        </div>
      </div>
    `;

    const addForm = container.querySelector('#detail-add-cart-form');
    addForm?.addEventListener('submit', async (e) => {
      e.preventDefault();
      const qty = Number(addForm.quantity.value || 1);
      if (qty <= 0) {
        showToast('Seleccione una cantidad mayor a 0.', 'error');
        return;
      }
      if (!isDigital && availableUnits > 0 && qty > availableUnits) {
        showToast(`Solo hay ${availableUnits} unidades disponibles en inventario.`, 'error');
        return;
      }
      await addProductToActiveBuyerCart(product.id, qty, product.price, product.currency);
    });
  } catch (err) {
    container.innerHTML = renderErrorState(err.message);
  }
}

export async function renderLoginView(container) {
  container.innerHTML = `
    <div class="auth-wrapper">
      <div class="auth-card">
        <h1>Iniciar Sesión</h1>
        <p class="text-muted">Ingrese con su correo electrónico registrado en NexusMarket.</p>

        <form id="login-form" novalidate>
          <div class="form-group">
            <label for="login-email">Correo Electrónico *</label>
            <input type="email" id="login-email" name="email" class="form-control"
              placeholder="usuario@nexusmarket.com" required autocomplete="email" />
          </div>
          <div class="form-group">
            <label for="login-password">Contraseña *</label>
            <input type="password" id="login-password" name="password" class="form-control"
              placeholder="Ingrese su contraseña" required autocomplete="current-password" />
          </div>
          <div id="login-error-msg" class="form-error hidden" role="alert"></div>
          <button type="submit" id="login-submit-btn" class="btn btn-primary btn-block btn-lg">
            Ingresar a NexusMarket
          </button>
        </form>

        <div class="demo-accounts-box">
          <h2>Cuentas Semilla para Demostración por Rol</h2>
          <p class="text-small text-muted">Seleccione un actor para autocompletar credenciales pre-sembradas por <code>DatabaseSeeder</code>:</p>
          <div class="demo-buttons-grid">
            <button type="button" class="btn btn-outline btn-sm js-demo-login" data-email="buyer@nexusmarket.com">
              🛒 Comprador
            </button>
            <button type="button" class="btn btn-outline btn-sm js-demo-login" data-email="seller@nexusmarket.com">
              🏪 Vendedor
            </button>
            <button type="button" class="btn btn-outline btn-sm js-demo-login" data-email="logistics@nexusmarket.com">
              🚚 Logística
            </button>
            <button type="button" class="btn btn-outline btn-sm js-demo-login" data-email="supervisor@nexusmarket.com">
              🛡️ Supervisor
            </button>
            <button type="button" class="btn btn-outline btn-sm js-demo-login" data-email="admin@nexusmarket.com">
              ⚙️ Administrador
            </button>
          </div>
        </div>

        <p class="auth-switch">
          ¿Aún no tiene cuenta de comprador? <a href="#/register">Regístrese aquí</a>
        </p>
      </div>
    </div>
  `;

  const form = container.querySelector('#login-form');
  const errorBox = container.querySelector('#login-error-msg');
  const submitBtn = container.querySelector('#login-submit-btn');

  container.querySelectorAll('.js-demo-login').forEach((btn) => {
    btn.addEventListener('click', () => {
      form.email.value = btn.dataset.email;
      form.password.value = 'NexusMarket2026!';
      errorBox.classList.add('hidden');
    });
  });

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    errorBox.classList.add('hidden');

    const email = form.email.value.trim();
    const password = form.password.value;

    if (!email || !email.includes('@')) {
      errorBox.textContent = 'Ingrese un correo electrónico válido.';
      errorBox.classList.remove('hidden');
      return;
    }
    if (!password || password.trim().length < 4) {
      errorBox.textContent = 'La contraseña es obligatoria.';
      errorBox.classList.remove('hidden');
      return;
    }

    submitBtn.disabled = true;
    submitBtn.textContent = 'Verificando credenciales...';

    try {
      const loginResp = await authApi.login(email, password);
      const session = sessionStore.setSession(loginResp);

      if (session.role === 'BUYER') {
        try {
          const cart = await buyerApi.getOrCreateCart(session.userId);
          sessionStore.setActiveCartId(cart.cartId);
          const count = (cart.items || []).reduce((acc, i) => acc + Number(i.quantity || 0), 0);
          sessionStore.setCartCount(count);
        } catch {
          // Ignorar si no tiene carrito previo
        }
      }

      showToast(`Sesión iniciada como ${ROLE_LABELS[session.role] || session.role} (${session.email})`, 'success');
      window.location.hash = ROLE_HOME_ROUTES[session.role] || '#/catalog';
    } catch (err) {
      errorBox.textContent = err.message || 'Credenciales inválidas. Verifique su correo y contraseña.';
      errorBox.classList.remove('hidden');
    } finally {
      submitBtn.disabled = false;
      submitBtn.textContent = 'Ingresar a NexusMarket';
    }
  });
}

export async function renderRegisterView(container) {
  container.innerHTML = `
    <div class="auth-wrapper">
      <div class="auth-card auth-card-wide">
        <h1>Registro de Nuevo Comprador</h1>
        <p class="text-muted">Complete sus datos de identidad y dirección principal de entrega para comprar en NexusMarket.</p>

        <form id="register-buyer-form" novalidate>
          <div class="form-grid-2">
            <div class="form-group">
              <label for="reg-fullname">Nombre Completo *</label>
              <input type="text" id="reg-fullname" name="fullName" class="form-control"
                placeholder="Ej. Ana María Gómez" required />
            </div>
            <div class="form-group">
              <label for="reg-email">Correo Electrónico *</label>
              <input type="email" id="reg-email" name="email" class="form-control"
                placeholder="ana.gomez@empresa.com" required />
            </div>
            <div class="form-group">
              <label for="reg-doc-type">Tipo de Documento *</label>
              <select id="reg-doc-type" name="documentType" class="form-control" required>
                <option value="CC">Cédula de Ciudadanía (CC)</option>
                <option value="CE">Cédula de Extranjería (CE)</option>
                <option value="NIT">NIT Empresarial</option>
                <option value="PASSPORT">Pasaporte</option>
              </select>
            </div>
            <div class="form-group">
              <label for="reg-doc-number">Número de Documento *</label>
              <input type="text" id="reg-doc-number" name="documentNumber" class="form-control"
                placeholder="Ej. 1020304050" required />
            </div>
          </div>

          <fieldset class="address-fieldset">
            <legend>Dirección Principal de Entrega</legend>
            <div class="form-grid-2">
              <div class="form-group">
                <label for="reg-street">Dirección / Calle *</label>
                <input type="text" id="reg-street" name="street" class="form-control"
                  placeholder="Ej. Carrera 15 # 85-24" required />
              </div>
              <div class="form-group">
                <label for="reg-city">Ciudad *</label>
                <input type="text" id="reg-city" name="city" class="form-control"
                  placeholder="Ej. Bogotá" required />
              </div>
              <div class="form-group">
                <label for="reg-state">Departamento / Estado *</label>
                <input type="text" id="reg-state" name="stateOrProvince" class="form-control"
                  placeholder="Ej. Cundinamarca" required />
              </div>
              <div class="form-group">
                <label for="reg-country">País *</label>
                <input type="text" id="reg-country" name="country" class="form-control"
                  value="Colombia" required />
              </div>
              <div class="form-group">
                <label for="reg-postal">Código Postal *</label>
                <input type="text" id="reg-postal" name="postalCode" class="form-control"
                  placeholder="Ej. 110221" required />
              </div>
              <div class="form-group">
                <label for="reg-ref">Referencia Adicional (Opcional)</label>
                <input type="text" id="reg-ref" name="reference" class="form-control"
                  placeholder="Ej. Edificio Quantum Oficina 502" />
              </div>
            </div>
          </fieldset>

          <div id="register-error-msg" class="form-error hidden" role="alert"></div>

          <button type="submit" id="register-submit-btn" class="btn btn-primary btn-block btn-lg">
            Completar Registro e Iniciar Sesión
          </button>
        </form>

        <p class="auth-switch">
          ¿Ya tiene una cuenta registrada? <a href="#/login">Inicie sesión aquí</a>
        </p>
      </div>
    </div>
  `;

  const form = container.querySelector('#register-buyer-form');
  const errorBox = container.querySelector('#register-error-msg');
  const submitBtn = container.querySelector('#register-submit-btn');

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    errorBox.classList.add('hidden');

    const payload = {
      fullName: form.fullName.value.trim(),
      email: form.email.value.trim(),
      documentType: form.documentType.value,
      documentNumber: form.documentNumber.value.trim(),
      primaryAddress: {
        street: form.street.value.trim(),
        city: form.city.value.trim(),
        stateOrProvince: form.stateOrProvince.value.trim(),
        country: form.country.value.trim(),
        postalCode: form.postalCode.value.trim(),
        reference: form.reference.value.trim() || null
      }
    };

    if (
      !payload.fullName ||
      !payload.email ||
      !payload.documentNumber ||
      !payload.primaryAddress.street ||
      !payload.primaryAddress.city ||
      !payload.primaryAddress.stateOrProvince ||
      !payload.primaryAddress.country ||
      !payload.primaryAddress.postalCode
    ) {
      errorBox.textContent = 'Todos los campos marcados con (*) son obligatorios.';
      errorBox.classList.remove('hidden');
      return;
    }

    submitBtn.disabled = true;
    submitBtn.textContent = 'Registrando comprador...';

    try {
      await authApi.registerBuyer(payload);
      const loginResp = await authApi.login(payload.email, 'NexusMarket2026!');
      sessionStore.setSession(loginResp);
      showToast('Cuenta creada y sesión iniciada correctamente.', 'success');
      window.location.hash = '#/catalog';
    } catch (err) {
      errorBox.textContent = err.message || 'No se pudo completar el registro. Verifique los datos ingresados.';
      errorBox.classList.remove('hidden');
    } finally {
      submitBtn.disabled = false;
      submitBtn.textContent = 'Completar Registro e Iniciar Sesión';
    }
  });
}

export async function renderAboutView(container) {
  container.innerHTML = `
    <section class="page-header">
      <h1>Guía del Marketplace y Políticas Operativas</h1>
      <p class="text-muted">Arquitectura, reglas de negocio verificadas y ciclo de vida de compras en NexusMarket.</p>
    </section>

    <div class="info-cards-grid">
      <article class="info-card">
        <h2>1. Recorrido de Compra y Reserva de Stock</h2>
        <p>
          Al confirmar un pedido desde el carrito, la orden se registra con sus ítems relacionales (<code>OrderItem</code>).
          Durante el pago, el motor de inventario valida el stock disponible en bodega y reserva las unidades físicamente,
          emitiendo de forma asíncrona la factura fiscal correspondiente.
        </p>
      </article>
      <article class="info-card">
        <h2>2. Productos Físicos vs. Digitales</h2>
        <p>
          Los productos con tipo <strong>DIGITAL</strong> completan su entrega automáticamente al estado
          <code>DELIVERED_COMPLETED</code> tras aprobarse el pago. Los productos <strong>PHYSICAL</strong> pasan al flujo
          de despacho y confirmación de entrega por parte de los Operadores Logísticos.
        </p>
      </article>
      <article class="info-card">
        <h2>3. Expiración Temporal (TTL) y Cancelaciones</h2>
        <p>
          Las órdenes en espera de pago están supervisadas por <code>ExpiredOrderScheduler</code>. Si superan el tiempo
          de vida configurado o son canceladas por el comprador antes del envío, el stock reservado se libera automáticamente.
        </p>
      </article>
      <article class="info-card">
        <h2>4. Devoluciones, Disputas y Reembolsos</h2>
        <p>
          Para órdenes entregadas, el comprador puede radicar solicitudes de devolución. Los Supervisores y Administradores
          evalúan la solicitud, aprueban o rechazan la devolución y ejecutan el reembolso financiero compensatorio.
        </p>
      </article>
    </div>
  `;
}

export async function renderNotFoundView(container) {
  container.innerHTML = `
    <div class="state-box state-empty">
      <h1>404 — Página No Encontrada</h1>
      <p>La ruta solicitada no existe en NexusMarket o fue movida.</p>
      <a href="#/catalog" class="btn btn-primary">Ir al Catálogo de Productos</a>
    </div>
  `;
}
