package com.wolfe.experience;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wolfe.catalog.Product;
import com.wolfe.catalog.ProductRepository;
import com.wolfe.visual.AccessoryOption;
import com.wolfe.visual.AccessoryOptionRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ConfigurationService {
    private static final Set<String> ALLOWED_FIELDS = Set.of(
            "accessoryId", "room", "accessorySku", "accessoryName");
    private static final Set<String> ALLOWED_ROOMS = Set.of("light", "warm", "dark");

    private final ProductRepository products;
    private final ConfigurationRepository configurations;
    private final AccessoryOptionRepository accessories;
    private final ObjectMapper objectMapper;

    public ConfigurationService(ProductRepository products,
                                ConfigurationRepository configurations,
                                AccessoryOptionRepository accessories,
                                ObjectMapper objectMapper) {
        this.products = products;
        this.configurations = configurations;
        this.accessories = accessories;
        this.objectMapper = objectMapper;
    }

    public ProductConfiguration create(Long customerId, Long productId, Long accessoryId, String configJson) {
        Product product = products.findById(productId)
                .filter(Product::isActive)
                .orElseThrow(() -> new java.util.NoSuchElementException("Product not found"));

        Long selectedAccessoryId = null;
        long addon = 0;
        if (accessoryId != null) {
            AccessoryOption accessory = accessories.findById(accessoryId)
                    .filter(AccessoryOption::isActive)
                    .orElseThrow(() -> new java.util.NoSuchElementException("Accessory not found"));
            if (!accessory.getProduct().getId().equals(product.getId()))
                throw new IllegalArgumentException("Accessory does not belong to product");
            selectedAccessoryId = accessory.getId();
            addon = toPaise(accessory.getPrice());
        }

        String normalized = sanitizeConfigJson(configJson);
        JsonNode normalizedNode = parse(normalized);
        JsonNode jsonAccessory = normalizedNode.get("accessoryId");
        if (selectedAccessoryId != null) {
            if (jsonAccessory != null && !jsonAccessory.isNull()
                    && jsonAccessory.asLong() != selectedAccessoryId)
                throw new IllegalArgumentException(
                        "configuration accessoryId does not match selected accessory");
        } else if (jsonAccessory != null && !jsonAccessory.isNull()) {
            throw new IllegalArgumentException(
                    "configuration accessoryId requires selected accessory");
        }

        long base = toPaise(product.getPrice());
        return configurations.save(new ProductConfiguration(
                UUID.randomUUID().toString().replace("-", ""),
                customerId, productId, selectedAccessoryId, addon, base, normalized));
    }

    public ProductConfiguration resolve(String token) {
        if (token == null || !token.matches("[A-Za-z0-9]{16,64}"))
            throw new java.util.NoSuchElementException("Configuration not found");
        ProductConfiguration configuration = configurations.findByShareToken(token)
                .orElseThrow(() -> new java.util.NoSuchElementException("Configuration not found"));
        if (configuration.getCreatedAt().plus(Duration.ofDays(configurationTtlDays())).isBefore(Instant.now()))
            throw new java.util.NoSuchElementException("Configuration not found");
        return configuration;
    }

    private int configurationTtlDays() {
        return Integer.parseInt(System.getenv().getOrDefault("WOLFE_CONFIGURATION_TTL_DAYS", "30"));
    }

    public ProductConfiguration resolveForOrder(String token, Long customerId, Long productId) {
        ProductConfiguration configuration = resolve(token);
        if (!java.util.Objects.equals(configuration.getProductId(), productId))
            throw new IllegalArgumentException("configuration does not belong to product");
        if (configuration.getCustomerId() != null && !java.util.Objects.equals(configuration.getCustomerId(), customerId))
            throw new org.springframework.security.access.AccessDeniedException("configuration belongs to another customer");
        return configuration;
    }

    public long currentAddonPrice(ProductConfiguration configuration, Long productId) {
        if (!java.util.Objects.equals(configuration.getProductId(), productId))
            throw new IllegalArgumentException("configuration does not belong to product");
        if (configuration.getSelectedAccessoryId() == null) return 0;
        Product product = products.findById(productId)
                .filter(Product::isActive)
                .orElseThrow(() -> new java.util.NoSuchElementException("Product not found"));
        AccessoryOption accessory = accessories.findById(configuration.getSelectedAccessoryId())
                .filter(AccessoryOption::isActive)
                .filter(a -> a.getProduct().getId().equals(product.getId()))
                .orElseThrow(() -> new java.util.NoSuchElementException("Configuration accessory not found"));
        return toPaise(accessory.getPrice());
    }

    public CurrentPricing currentPricing(ProductConfiguration configuration) {
        Product product = products.findById(configuration.getProductId())
                .filter(Product::isActive)
                .orElseThrow(() -> new java.util.NoSuchElementException("Product not found"));

        long addon = currentAddonPrice(configuration, product.getId());
        return new CurrentPricing(toPaise(product.getPrice()), addon);
    }

    public String sanitizeConfigJson(String raw) {
        try {
            JsonNode node = objectMapper.readTree(raw);
            if (!node.isObject())
                throw new IllegalArgumentException("configuration must be a JSON object");
            ObjectNode source = (ObjectNode) node;
            var fields = source.fieldNames();
            while (fields.hasNext()) {
                String name = fields.next();
                if (!ALLOWED_FIELDS.contains(name))
                    throw new IllegalArgumentException("unsupported configuration field: " + name);
            }
            JsonNode accessoryId = source.get("accessoryId");
            if (accessoryId != null && !accessoryId.isNull() && !accessoryId.canConvertToLong())
                throw new IllegalArgumentException("accessoryId must be numeric");
            JsonNode room = source.get("room");
            if (room != null && !room.isNull()
                    && (!room.isTextual() || !ALLOWED_ROOMS.contains(room.asText())))
                throw new IllegalArgumentException("room is invalid");
            for (String field : List.of("accessorySku", "accessoryName")) {
                JsonNode value = source.get(field);
                if (value != null && !value.isNull()
                        && (!value.isTextual() || value.asText().length() > 200))
                    throw new IllegalArgumentException(field + " is invalid");
            }
            return objectMapper.writeValueAsString(source);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("configuration JSON is invalid");
        }
    }

    private JsonNode parse(String normalized) {
        try {
            return objectMapper.readTree(normalized);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid configuration JSON", e);
        }
    }

    private static long toPaise(java.math.BigDecimal amount) {
        return amount == null ? 0 : amount.movePointRight(2).longValueExact();
    }

    public record CurrentPricing(long basePrice, long addonPrice) {}
}
