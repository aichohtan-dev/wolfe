package com.wolfe.address;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CustomerAddressControllerSecurityTest {
    private CustomerAddressRepository repo;
    private CustomerAddressController controller;
    private Authentication owner;

    @BeforeEach
    void setUp() {
        repo = mock(CustomerAddressRepository.class);
        controller = new CustomerAddressController(repo);
        var authenticated = new UsernamePasswordAuthenticationToken("customer@example.com", null, List.of());
        authenticated.setDetails(7L);
        owner = authenticated;
    }

    @Test
    void customerCannotUpdateAnotherCustomersAddressById() {
        var foreignAddress = new CustomerAddress(99L, "Home", "Other Customer", "+919876543210",
                "1 Example Road", "Jodhpur", "Rajasthan", "342001", true);
        when(repo.findById(42L)).thenReturn(Optional.of(foreignAddress));

        assertThrows(java.util.NoSuchElementException.class,
                () -> controller.update(7L, 42L, null, owner));

        verify(repo, never()).save(any());
    }

    @Test
    void customerCannotDeleteAnotherCustomersAddressById() {
        var foreignAddress = new CustomerAddress(99L, "Home", "Other Customer", "+919876543210",
                "1 Example Road", "Jodhpur", "Rajasthan", "342001", true);
        when(repo.findById(42L)).thenReturn(Optional.of(foreignAddress));

        assertThrows(java.util.NoSuchElementException.class,
                () -> controller.delete(7L, 42L, owner));

        verify(repo, never()).delete(any());
    }

    @Test
    void customerCannotListAnotherCustomersAddresses() {
        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> controller.list(99L, owner));

        verifyNoInteractions(repo);
    }
}
