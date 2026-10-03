import fs from 'node:fs';
import path from 'node:path';
const roots=['src','backend/src/main/java','backend/src/main/resources','nginx.conf.template','docker-compose.yml'];
const files=[];
function walk(p){const st=fs.statSync(p); if(st.isDirectory()){for(const x of fs.readdirSync(p))walk(path.join(p,x));} else files.push(p);}
for(const r of roots) if(fs.existsSync(r)) walk(r);
const text=files.filter(f=>!f.includes('/test/')).map(f=>[f,fs.readFileSync(f,'utf8')]);
const bad=[];
for(const [f,s] of text){
 if(/-----BEGIN (RSA|EC|OPENSSH) PRIVATE KEY-----/.test(s)) bad.push(`${f}: private key material`);
 if(/AKIA[0-9A-Z]{16}/.test(s)) bad.push(`${f}: AWS access key pattern`);
 if(/sk-[A-Za-z0-9]{20,}/.test(s)) bad.push(`${f}: API secret pattern`);
 if(/localStorage\.(setItem|getItem)\([^)]*(access|refresh)[-_]?token/i.test(s)) bad.push(`${f}: browser token storage`);
}
if(bad.length){console.error(bad.join('\n')); process.exit(1);}
console.log(`security-scan: ${files.length} files checked`);
