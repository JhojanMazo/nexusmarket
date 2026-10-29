import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const PUBLIC_DIR = path.join(__dirname, 'public');
const PORT = Number(process.env.FRONTEND_PORT || 3000);
const BACKEND_ORIGIN = process.env.BACKEND_ORIGIN || 'http://localhost:8080';

const MIME_TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.svg': 'image/svg+xml',
  '.png': 'image/png',
  '.ico': 'image/x-icon'
};

export function createStaticServer() {
  return http.createServer((req, res) => {
    const reqUrl = new URL(req.url || '/', `http://${req.headers.host || 'localhost'}`);

    // Proxy /api requests to backend when running standalone on port 3000
    if (reqUrl.pathname.startsWith('/api/')) {
      const targetUrl = new URL(reqUrl.pathname + reqUrl.search, BACKEND_ORIGIN);
      const proxyReq = http.request(
        targetUrl,
        {
          method: req.method,
          headers: { ...req.headers, host: targetUrl.host }
        },
        (proxyRes) => {
          res.writeHead(proxyRes.statusCode || 502, proxyRes.headers);
          proxyRes.pipe(res, { end: true });
        }
      );
      proxyReq.on('error', (err) => {
        res.writeHead(502, { 'Content-Type': 'application/json; charset=utf-8' });
        res.end(JSON.stringify({
          status: 502,
          error: 'Bad Gateway',
          message: `No se pudo conectar con el backend en ${BACKEND_ORIGIN}: ${err.message}`
        }));
      });
      req.pipe(proxyReq, { end: true });
      return;
    }

    let safePath = path.normalize(decodeURIComponent(reqUrl.pathname)).replace(/^(\.\.(\/|\\|$))+/, '');
    if (safePath === '/' || safePath === '\\') {
      safePath = '/index.html';
    }

    const filePath = path.join(PUBLIC_DIR, safePath);
    if (!filePath.startsWith(PUBLIC_DIR)) {
      res.writeHead(403);
      res.end('Forbidden');
      return;
    }

    fs.stat(filePath, (err, stats) => {
      if (!err && stats.isFile()) {
        const ext = path.extname(filePath).toLowerCase();
        res.writeHead(200, { 'Content-Type': MIME_TYPES[ext] || 'application/octet-stream' });
        fs.createReadStream(filePath).pipe(res);
      } else {
        // SPA Fallback to index.html
        const indexHtml = path.join(PUBLIC_DIR, 'index.html');
        res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
        fs.createReadStream(indexHtml).pipe(res);
      }
    });
  });
}

if (process.argv[1] && path.resolve(process.argv[1]) === __filename) {
  const server = createStaticServer();
  server.listen(PORT, () => {
    console.log(`NexusMarket Frontend disponible en http://localhost:${PORT} (Proxy API -> ${BACKEND_ORIGIN})`);
  });
}
