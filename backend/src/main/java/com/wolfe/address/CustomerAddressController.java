package com.wolfe.address;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import static com.wolfe.security.CustomerAccess.requireCustomer;

@RestController
@RequestMapping("/api/v1/customers/{customerId}/addresses")
public class CustomerAddressController {
    private final CustomerAddressRepository repo;
    public CustomerAddressController(CustomerAddressRepository repo) {
        this.repo = repo;
    }
    public record AddressRequest(@NotBlank String label, @NotBlank String recipientName, @NotBlank @Size(min = 7, max = 30) String phone,
    @NotBlank @Size(max = 1000) String address, @NotBlank String city, @NotBlank String state, @NotBlank @Size(min = 4, max = 20) String pincode,
    boolean isDefault) {
    }
    @GetMapping public List<CustomerAddress> list(@PathVariable Long customerId, Authentication auth) {
        requireCustomer(auth, customerId);
        return repo.findByCustomerIdOrderByIsDefaultDescIdDesc(customerId);
    }
    @PostMapping
    @Transactional
    @ResponseStatus(HttpStatus.CREATED) public CustomerAddress create(@PathVariable Long customerId, @Valid @RequestBody AddressRequest r,
    Authentication auth) {
        requireCustomer(auth, customerId);
        boolean makeDefault = r.isDefault() || repo.countByCustomerId(customerId) == 0;
        if (makeDefault)repo.findByCustomerIdOrderByIsDefaultDescIdDesc(customerId).forEach(a -> a.update(a.getLabel(), a.getRecipientName(), a.getPhone(),
        a.getAddress(), a.getCity(), a.getState(), a.getPincode(),
        false));
        return repo.save(new CustomerAddress(customerId, r.label(), r.recipientName(), r.phone(), r.address(), r.city(), r.state(), r.pincode(), makeDefault));
    }
    @PutMapping("/{id}")
    @Transactional public CustomerAddress update(@PathVariable Long customerId, @PathVariable Long id, @Valid @RequestBody AddressRequest r,
    Authentication auth) {
        requireCustomer(auth, customerId);
        CustomerAddress a = repo.findById(id).filter(x -> x.getCustomerId().equals(customerId)).orElseThrow(() -> new NoSuchElementException("Address not found"));
        if (r.isDefault())repo.findByCustomerIdOrderByIsDefaultDescIdDesc(customerId).forEach(x -> {
            if (!x.getId().equals(id))x.update(x.getLabel(), x.getRecipientName(), x.getPhone(), x.getAddress(), x.getCity(), x.getState(), x.getPincode(),
            false);
        }
        );
        a.update(r.label(), r.recipientName(), r.phone(), r.address(), r.city(), r.state(), r.pincode(), r.isDefault());
        return repo.save(a);
    }
    @DeleteMapping("/{id}")
    @Transactional
    @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long customerId, @PathVariable Long id, Authentication auth) {
        requireCustomer(auth, customerId);
        CustomerAddress a = repo.findById(id).filter(x -> x.getCustomerId().equals(customerId)).orElseThrow(() -> new NoSuchElementException("Address not found"));
        repo.delete(a);
        if (a.isDefault()) {
            repo.findByCustomerIdOrderByIsDefaultDescIdDesc(customerId).stream().filter(x -> !x.getId().equals(id)).findFirst().ifPresent(x -> x.update(x.getLabel(), x.getRecipientName(), x.getPhone(), x.getAddress(), x.getCity(), x.getState(), x.getPincode(),
            true));
        }
    }
}
