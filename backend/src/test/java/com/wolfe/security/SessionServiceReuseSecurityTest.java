package com.wolfe.security;

import com.wolfe.customer.Customer;
import com.wolfe.customer.CustomerRepository;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SessionServiceReuseSecurityTest {
    @Test void refreshReuseAdvancesSessionVersionAndRevokesFamily() {
        var repo = mock(RefreshTokenRepository.class);
        var customers = mock(CustomerRepository.class);
        var jwt = mock(JwtService.class);
        var service = new SessionService(repo, customers, jwt, 3600L, 5);

        var c = new Customer("Test", "test@example.com", "hash");
        setId(c, 42L);
        var old = new RefreshToken(42L, SessionService.hash("stolen"), Instant.now().plusSeconds(3600));
        old.revoke();

        when(repo.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(old));
        when(customers.findByIdForUpdate(42L)).thenReturn(Optional.of(c));
        when(repo.revokeAllByCustomerId(eq(42L), any(Instant.class))).thenReturn(1);

        assertThrows(SessionService.InvalidRefreshTokenReuseException.class, () -> service.rotate("stolen"));
        assertEquals(1L, c.getSessionVersion());
        verify(repo).revokeAllByCustomerId(eq(42L), any(Instant.class));
        verify(customers).save(c);
    }

    private static void setId(Customer c, Long id) {
        try {
            var f = Customer.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(c, id);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }
}