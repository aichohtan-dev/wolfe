package com.wolfe.retailer;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class RetailerCoordinateValidationTest {
    @Test void acceptsValidCoordinates() {
        Retailer r = new Retailer("R", "O", "r@example.com", "+919999999999", "A", "Jaipur", "Rajasthan", "302001");
        r.setCoordinates(new BigDecimal("26.912400"), new BigDecimal("75.787300"));
        assertEquals(new BigDecimal("26.912400"), r.getLatitude());
        assertEquals(new BigDecimal("75.787300"), r.getLongitude());
    }

    @Test void rejectsInvalidCoordinates() {
        Retailer r = new Retailer();
        assertThrows(IllegalArgumentException.class, () -> r.setCoordinates(new BigDecimal("91"), BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> r.setCoordinates(BigDecimal.ZERO, new BigDecimal("181")));
    }
}
