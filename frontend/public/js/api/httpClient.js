/**
 * Cliente HTTP centralizado para comunicación con la API REST de NexusMarket.
 * Incluye inyección automática de Bearer JWT, control de timeout, deduplicación de GET concurrentes
 * y traducción clara de errores HTTP.
 */

import { ENV } from '../config/env.js';
import { sessionStore } from '../state/sessionStore.js';

const inflightGetRequests = new Map();

export class ApiError extends Error {
  constructor(status, message, details = null) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.details = details;
  }
}

export async function apiRequest(endpoint, options = {}) {
  const {
    method = 'GET',
    body = undefined,
    params = undefined,
    headers = {},
    timeoutMs = ENV.REQUEST_TIMEOUT_MS,
    fetchImpl = typeof fetch === 'function' ? fetch : null
  } = options;

  if (!fetchImpl) {
    throw new Error('No hay implementación de fetch disponible en este entorno');
  }

  const normalizedEndpoint = endpoint.startsWith('/') ? endpoint : `/${endpoint}`;
  let url = `${ENV.API_BASE_URL}${normalizedEndpoint}`;

  if (params && typeof params === 'object') {
    const searchParams = new URLSearchParams();
    for (const [key, value] of Object.entries(params)) {
      if (value !== undefined && value !== null && String(value).trim() !== '') {
        searchParams.append(key, String(value).trim());
      }
    }
    const qs = searchParams.toString();
    if (qs) {
      url += (url.includes('?') ? '&' : '?') + qs;
    }
  }

  const requestHeaders = {
    Accept: 'application/json',
    ...headers
  };

  const token = sessionStore.getToken();
  if (token && !requestHeaders.Authorization) {
    requestHeaders.Authorization = `Bearer ${token}`;
  }

  let serializedBody;
  if (body !== undefined && body !== null) {
    requestHeaders['Content-Type'] = 'application/json';
    serializedBody = typeof body === 'string' ? body : JSON.stringify(body);
  }

  const upperMethod = method.toUpperCase();
  const dedupeKey = upperMethod === 'GET' ? `${url}|${token || 'anon'}` : null;
  if (dedupeKey && inflightGetRequests.has(dedupeKey)) {
    return inflightGetRequests.get(dedupeKey);
  }

  const executePromise = (async () => {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), timeoutMs);

    try {
      const response = await fetchImpl(url, {
        method: upperMethod,
        headers: requestHeaders,
        body: serializedBody,
        signal: controller.signal
      });

      if (response.status === 204) {
        return null;
      }

      const text = await response.text();
      let payload = null;
      if (text) {
        try {
          payload = JSON.parse(text);
        } catch {
          payload = { message: text };
        }
      }

      if (!response.ok) {
        if (response.status === 401 && sessionStore.isAuthenticated()) {
          sessionStore.clearSession();
        }
        const message =
          payload?.message ||
          payload?.error ||
          `Error HTTP ${response.status} al procesar la solicitud`;
        throw new ApiError(response.status, message, payload);
      }

      return payload;
    } catch (err) {
      if (err?.name === 'AbortError') {
        throw new ApiError(408, 'La solicitud excedió el tiempo máximo de espera. Verifique su conexión.');
      }
      if (err instanceof ApiError) {
        throw err;
      }
      throw new ApiError(0, err?.message || 'Error de conexión con el servidor NexusMarket');
    } finally {
      clearTimeout(timer);
      if (dedupeKey) {
        inflightGetRequests.delete(dedupeKey);
      }
    }
  })();

  if (dedupeKey) {
    inflightGetRequests.set(dedupeKey, executePromise);
  }

  return executePromise;
}
