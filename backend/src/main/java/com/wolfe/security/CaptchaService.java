package com.wolfe.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.Map;

@Service
public class CaptchaService {
    private final boolean required;
    private final String secret;
    private final RestClient client=RestClient.create("https://challenges.cloudflare.com");
    public CaptchaService(@Value("${WOLFE_CAPTCHA_REQUIRED:false}") boolean required, @Value("${WOLFE_CAPTCHA_SECRET:}") String secret){this.required=required;this.secret=secret;}
    public boolean verify(String token,String remoteIp){
        if(!required) return true;
        if(secret==null||secret.isBlank()||token==null||token.isBlank()) return false;
        try { Map<?,?> r=client.post().uri("/turnstile/v0/siteverify").body(Map.of("secret",secret,"response",token,"remoteip",remoteIp==null?"":remoteIp)).retrieve().body(Map.class); return Boolean.TRUE.equals(r==null?null:r.get("success")); }
        catch(Exception e){ return false; }
    }
    public boolean isRequired(){return required;}
}
