import fs from 'node:fs';
import path from 'node:path';

const root = path.resolve('dist/assets');
const maxBytes = Number(process.env.WOLFE_MAX_JS_BUNDLE_BYTES ?? 3_000_000);
if (!Number.isSafeInteger(maxBytes) || maxBytes <= 0) throw new Error('WOLFE_MAX_JS_BUNDLE_BYTES must be a positive integer');
if (!fs.existsSync(root)) throw new Error('dist/assets does not exist; run the production build first');

const jsFiles = fs.readdirSync(root).filter(f => f.endsWith('.js'));
const total = jsFiles.reduce((sum, f) => sum + fs.statSync(path.join(root, f)).size, 0);
if (total > maxBytes) {
  throw new Error(`JavaScript bundle budget exceeded: ${total} bytes > ${maxBytes} bytes`);
}
console.log(`JavaScript bundle budget: ${total}/${maxBytes} bytes`);
