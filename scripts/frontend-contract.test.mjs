import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
const api=fs.readFileSync('src/api.ts','utf8');
const app=fs.readFileSync('src/App.tsx','utf8');

test('frontend uses cookie credentials and never stores access tokens',()=>{
  assert.match(api,/credentials:\s*['"]include['"]/);
  assert.doesNotMatch(api,/localStorage\.(getItem|setItem).*token/i);
  assert.doesNotMatch(app,/localStorage\.(getItem|setItem).*token/i);
});

test('frontend maps backend errors to safe user-facing messages',()=>{
  assert.match(api,/401:'Please sign in again\.'/);
  assert.match(api,/403:'You do not have permission for this action\.'/);
  assert.ok(api.includes("500:'Something went wrong."));
});

test('account security endpoints are wired',()=>{
  assert.match(api,/customers\/sessions/);
  assert.match(api,/customers\/password/);
  assert.match(api,/customers\/me/);
});

test('storage consent gates persisted user state',()=>{
  const app=fs.readFileSync('src/App.tsx','utf8');
  assert.match(app,/wolfe_storage_consent.*accepted/);
  assert.match(app,/localStorage\.setItem\('wolfe_storage_consent'/);
});

test('SEO prerender pipeline exists and generates canonical metadata',()=>{
  const pkg=JSON.parse(fs.readFileSync('package.json','utf8'));
  const script=fs.readFileSync('scripts/prerender-seo.mjs','utf8');
  assert.match(pkg.scripts.build,/prerender-seo/);
  assert.match(script,/canonical/);
  assert.match(script,/og:title/);
});

test('strict security headers scope inline styles to attributes only',()=>{
  const nginx=fs.readFileSync('nginx.conf.template','utf8');
  assert.match(nginx,/style-src 'self'; style-src-attr 'unsafe-inline'; style-src-elem 'self'/);
  assert.match(nginx,/object-src 'none'/);
  assert.match(nginx,/Cross-Origin-Opener-Policy/);
});

test('catalog DTO boundaries remain explicit and entity IDs have separate version fields',()=>{
  const product=fs.readFileSync('backend/src/main/java/com/wolfe/catalog/Product.java','utf8');
  const retailer=fs.readFileSync('backend/src/main/java/com/wolfe/retailer/Retailer.java','utf8');
  const productApi=fs.readFileSync('backend/src/main/java/com/wolfe/catalog/ProductController.java','utf8');
  const related=fs.readFileSync('backend/src/main/java/com/wolfe/catalog/ProductRelatedController.java','utf8');
  const wishlist=fs.readFileSync('backend/src/main/java/com/wolfe/wishlist/WishlistController.java','utf8');
  assert.match(product,/private Long id;\s*\n\s*@Version\s*\n\s*private long version;/);
  assert.match(retailer,/private Long id;\s*\n\s*@Version\s*\n\s*private long version;/);
  assert.match(productApi,/public List<ProductPublicView> list\(/);
  assert.doesNotMatch(related,/List<Product> related/);
  assert.doesNotMatch(wishlist,/List<Product> get\(/);
});

test('SEO social image is generated as an absolute public URL',()=>{
  const script=fs.readFileSync('scripts/prerender-seo.mjs','utf8');
  assert.match(script,/og:image/);
  assert.match(script,/\$\{base\}\/wolfe-logo\.png/);
});
