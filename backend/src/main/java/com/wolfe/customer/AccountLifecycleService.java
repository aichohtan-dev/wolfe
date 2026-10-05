package com.wolfe.customer;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.wolfe.security.SessionService;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountLifecycleService {
    private static final Logger log = LoggerFactory.getLogger(AccountLifecycleService.class);
    private final AccountTokenRepository tokens; private final CustomerRepository customers; private final org.springframework.beans.factory.ObjectProvider<JavaMailSender> mailProvider; private final SessionService sessions; private final String from; private final String publicBaseUrl; private final SecureRandom random = new SecureRandom();
    public AccountLifecycleService(AccountTokenRepository tokens, CustomerRepository customers, org.springframework.beans.factory.ObjectProvider<JavaMailSender> mailProvider, SessionService sessions,
            @Value("${WOLFE_MAIL_FROM:no-reply@wolfe.local}") String from, @Value("${WOLFE_PUBLIC_BASE_URL:http://localhost}") String publicBaseUrl) {
        this.tokens=tokens; this.customers=customers; this.mailProvider=mailProvider; this.sessions=sessions; this.from=from; this.publicBaseUrl=publicBaseUrl;
    }
    @org.springframework.scheduling.annotation.Scheduled(fixedDelayString = "PT6H")
    @Transactional
    public void purgeExpiredAccountTokens() {
        tokens.deleteByExpiresAtBefore(Instant.now());
    }

    @Transactional
    public void sendVerification(Long customerId) { if (customerId == null) return; issueAndSend(customerId,"VERIFY_EMAIL","Verify your Wolfe email","/verify-email"); }
    @Transactional
    public void requestPasswordReset(String email) {
        customers.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT)).filter(Customer::isEnabled).ifPresent(c -> issueAndSend(c.getId(),"PASSWORD_RESET","Reset your Wolfe password","/reset-password"));
    }
    @Transactional
    public boolean verifyEmail(String raw) {
        String tokenHash = hash(raw);
        AccountToken probe = tokens.findByTokenHashAndType(tokenHash, "VERIFY_EMAIL").orElse(null);
        if (probe == null) return false;
        Customer c = customers.findByIdForUpdate(probe.getCustomerId()).orElse(null);
        if (c == null) return false;
        AccountToken t = tokens.findByTokenHashAndTypeForUpdate(tokenHash, "VERIFY_EMAIL").orElse(null);
        if (t == null || !t.active()) return false;
        c.markEmailVerified();
        customers.save(c);
        t.use();
        tokens.save(t);
        return true;
    }
    @Transactional
    public boolean resetPassword(String raw, String newHash) {
        String tokenHash = hash(raw);
        AccountToken probe = tokens.findByTokenHashAndType(tokenHash, "PASSWORD_RESET").orElse(null);
        if (probe == null) return false;
        // Establish customer -> token lock order. Token confirmation must not lock the token
        // first and then wait for the customer because issuance also locks customer -> tokens.
        Customer c = customers.findByIdForUpdate(probe.getCustomerId()).orElse(null);
        if (c == null || !c.isEnabled()) return false;
        AccountToken t = tokens.findByTokenHashAndTypeForUpdate(tokenHash, "PASSWORD_RESET").orElse(null);
        if (t == null || !t.active()) return false;
        c.changePasswordHash(newHash);
        c.incrementSessionVersion();
        customers.save(c);
        sessions.revokeAll(c.getId());
        t.use();
        tokens.save(t);
        tokens.deleteByCustomerIdAndType(c.getId(), "PASSWORD_RESET");
        return true;
    }
    private void issueAndSend(Long customerId,String type,String subject,String path){
        Customer c=customers.findByIdForUpdate(customerId).orElse(null); if(c==null) return; tokens.deleteByCustomerIdAndType(customerId,type); String raw=randomToken(); tokens.save(new AccountToken(customerId,type,hash(raw),Instant.now().plus(Duration.ofMinutes(30))));
        String link=publicBaseUrl.replaceAll("/$","")+path+"?token="+java.net.URLEncoder.encode(raw,StandardCharsets.UTF_8);
        try {
            SimpleMailMessage m=new SimpleMailMessage(); m.setFrom(from); m.setTo(c.getEmail()); m.setSubject(subject); m.setText("Use this secure Wolfe link within 30 minutes: "+link);
            JavaMailSender sender = mailProvider.getIfAvailable();
            if (sender == null) {
                log.error("Account lifecycle email delivery unavailable for type={} customerId={}", type, customerId);
                throw new IllegalStateException("Account email delivery is unavailable");
            }
            sender.send(m);
        } catch (RuntimeException ex) {
            log.error("Account lifecycle email delivery failed for type={} customerId={}", type, customerId, ex);
            throw ex;
        }
    }
    private String randomToken(){byte[] b=new byte[32];random.nextBytes(b);return Base64.getUrlEncoder().withoutPadding().encodeToString(b);}
    private static String hash(String raw){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest((raw==null?"":raw).getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
}
