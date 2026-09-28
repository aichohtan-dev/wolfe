package com.wolfe.catalog;

import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductSeed {
    private static final SeedProduct[] PRODUCTS = {
        new SeedProduct("w-01", "Arc Pull Handle", "4200", "Handles", "Brushed Brass", "A quiet architectural pull with a softly considered profile."),
        new SeedProduct("w-02", "Noir Round Knob", "4600", "Knobs", "Matte Black", "Compact, tactile and understated for modern cabinetry."),
        new SeedProduct("w-03", "Linear Cabinet Pull", "5100", "Handles", "Satin Nickel", "Minimal geometry designed for contemporary interiors."),
        new SeedProduct("w-04", "Atelier Hook", "4400", "Hooks", "Antique Brass", "A sculptural wall hook that works beautifully in multiples."),
        new SeedProduct("w-05", "Edge Cup Pull", "5600", "Handles", "Bronze", "A refined cup pull with a strong but restrained silhouette."),
        new SeedProduct("w-06", "Studio Knob", "4900", "Knobs", "Polished Chrome", "A versatile circular form for kitchens, wardrobes and furniture.")
    };

    @Bean
    CommandLineRunner seed(ProductRepository repo, com.wolfe.inventory.InventoryRepository inventoryRepo) {
        return args -> {
            for (int i = 0; i < PRODUCTS.length; i++) {
                SeedProduct seed = PRODUCTS[i];
                Product product = repo.findBySlug(seed.slug()).orElseGet(() -> {
                    Product p = repo.save(new Product(seed.slug(), seed.name(), new BigDecimal(seed.price()), seed.category(), seed.finish(), seed.description()));
                    p.setImageUrl("/catalog/brass-0" + (index1(seed.slug())) + ".jpg");
                    return repo.save(p);
                });
                inventoryRepo.insertDefault(product.getId(), 50);
            }
        };
    }

    private static String index1(String slug) {
        return switch (slug) {
            case "w-01" -> "1";
            case "w-02" -> "2";
            case "w-03" -> "3";
            case "w-04" -> "4";
            case "w-05" -> "5";
            case "w-06" -> "6";
            default -> "1";
        };
    }

    private record SeedProduct(String slug, String name, String price, String category, String finish, String description) {}
}
