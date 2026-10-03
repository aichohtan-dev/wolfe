import { useEffect, useRef } from 'react';
declare global { interface Window { turnstile?: { render: (el: HTMLElement, opts: { sitekey: string; callback: (token:string)=>void; 'expired-callback'?: ()=>void; 'error-callback'?: ()=>void }) => void } } }
export function TurnstileWidget({ onToken }: { onToken: (token: string) => void }) {
  const ref=useRef<HTMLDivElement>(null); const siteKey=(import.meta.env.VITE_TURNSTILE_SITE_KEY || '').trim();
  useEffect(()=>{ if(!siteKey || !ref.current) return; const render=()=>{if(window.turnstile&&ref.current)window.turnstile.render(ref.current,{sitekey:siteKey,callback:onToken,'expired-callback':()=>onToken(''),'error-callback':()=>onToken('')});}; if(window.turnstile)render(); else {const script=document.createElement('script');script.src='https://challenges.cloudflare.com/turnstile/v0/api.js';script.async=true;script.defer=true;script.onload=render;document.head.appendChild(script);return()=>{script.onload=null;};}},[siteKey,onToken]);
  if(!siteKey)return null; return <div ref={ref} aria-label="Human verification" />;
}
