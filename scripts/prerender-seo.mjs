import fs from 'node:fs';
import path from 'node:path';
const base=(process.env.VITE_PUBLIC_BASE_URL||'https://localhost').replace(/\/$/,'');
const routes={
  '/':'Wolfe — The Jewel of Villa|Premium architectural hardware for considered spaces.',
  '/shop':'Shop Wolfe Architectural Hardware|Handles, knobs, hooks and considered architectural hardware.',
  '/bundles':'Wolfe Bundles | Considered Combinations|Curated architectural hardware bundles with automated savings.',
  '/catalogue':'Wolfe Catalogue|Explore the full Wolfe architectural hardware catalogue.',
  '/consultation':'Design Consultation | Wolfe|Book a complimentary architectural hardware design consultation.',
  '/quote':'Project Quote | Wolfe|Request a tailored architectural hardware project quote.',
  '/custom-design':'Custom Design | Wolfe|Submit bespoke hardware requirements and custom design briefs.',
  '/journal':'Wolfe Journal|Ideas, materials and architectural hardware inspiration.',
  '/privacy-policy':'Privacy Policy | Wolfe|How Wolfe handles personal data and browser storage.',
  '/terms':'Terms & Conditions | Wolfe|Wolfe commerce and service terms.'
};
const template=fs.readFileSync('dist/index.html','utf8');
for(const [route,meta] of Object.entries(routes)){
  const [title,description]=meta.split('|');
  const html=template.replace(/<title>[^<]*<\/title>/,`<title>${title}</title>`)
    .replace(/<meta name="description" content="[^"]*"\/>/,`<meta name="description" content="${description}"/>`)
    .replace(/<link rel="canonical" href="[^"]*"\s*\/>/,`<link rel="canonical" href="${base}${route}"/>`)
    .replace(/<meta property="og:image" content="[^"]*"\s*\/>/,`<meta property="og:image" content="${base}/wolfe-logo.png"/>`)
    .replace('</head>',`<meta property="og:title" content="${title}"/><meta property="og:description" content="${description}"/><meta property="og:url" content="${base}${route}"/></head>`);
  const dir=route==='/'?'dist':path.join('dist',route.replace(/^\//,''));
  fs.mkdirSync(dir,{recursive:true}); fs.writeFileSync(path.join(dir,'index.html'),html);
}
console.log(`prerender-seo: generated ${Object.keys(routes).length} routes`);
